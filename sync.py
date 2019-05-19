#!/usr/bin/env python3
import argparse
import datetime
from collections import OrderedDict

import psycopg2 as psycopg2
import requests
import requests_cache

from odata.odata import create_parser, logger, _to_snake_case
from odata.topology import get_topology

URL = 'https://ws.parlament.ch/odata.svc'

EPOCH = datetime.datetime.utcfromtimestamp(0)

LANGUAGES = ['DE', 'EN', 'FR', 'IT', 'RM']


def _parse_date(datestring):
    """
    Convert  "ASP.Net JSON Date" (e.g. '/Date(1293368772797)/') to a timestamp string understood by PostgreSQL.
    :param datestring: A "ASP.Net JSON Date" string
    :return:
    """
    timepart = datestring.split('(')[1].split(')')[0]
    if "+" in timepart:
        adjustedseconds = int(timepart[:-5]) / 1000 + int(timepart[-5:]) / 100 * 3600
    else:
        adjustedseconds = int(timepart) / 1000

    dt = EPOCH + datetime.timedelta(seconds=adjustedseconds)
    return dt.strftime("%Y-%m-%d %H:%M:%S")


def _result_to_sql_statement_header(entity_type):
    column_names = [_to_snake_case(p.name) for p in entity_type.properties]
    yield "INSERT INTO {} ({}) VALUES".format(entity_type.table_name, ", ".join(column_names))


def _results_to_sql_dict(entity_type, result, accept_degenerated=False):
    values = []
    for p in entity_type.properties:
        if accept_degenerated:
            value = result[p.name] if p.name in result else None
        else:
            value = result[p.name]
        # Parse dates
        if isinstance(value, str) and value.startswith('/Date('):
            value = _parse_date(value)
        values.append(value)
    return values


def _fetch(url):
    """
    :param url: URL to fetch
    :return: JSON data with the entries results
    """
    logger.debug("Fetching from URL {}".format(url))
    # Using header instead of URL to request JSON because __next forgets about it.
    r = requests.get(url, headers={'Accept': 'application/json'})
    if hasattr(r, 'from_cache') and r.from_cache:
        logger.debug("Found URL {} in cache".format(url))
    else:
        logger.debug("HTTP code {} after {} seconds".format(r.status_code, r.elapsed.total_seconds()))
    return r.json()


def _split_response(json):
    try:
        json_data = json['d']
    except KeyError as e:
        logger.error("Could no find element 'd' in {}".format(json))
        raise e
    return (
        json_data if 'results' not in json_data else json_data['results'],
        int(json_data['__count']),
        json_data['__next'] if '__next' in json_data else None
    )


def create_entity_type_url(entity_type):
    return '{}/{}?$inlinecount=allpages'.format(URL, entity_type.name)


def create_language_filter_url(entity_type, languages=None):
    # Note: Every single entity type in the Curia Vista schema has a Language property
    if languages:
        return '{}&$filter={}'.format(create_entity_type_url(entity_type),
                                      ' or '.join(['(Language%20eq%20%27{}%27)'.format(l) for l in languages]))
    return create_entity_type_url(entity_type)


def create_skip_url(entity_type, done, batch_size, languages=None):
    return '{}&$skip={}&$top={}'.format(create_language_filter_url(entity_type, languages), done, batch_size)


def fetch_entity_type(entity_type, fetcher, languages=None):
    """
    Generator for JSON objects

    :param fetcher: Callable taking a single argument (URL) pointing to the resource to be fetched
    :param entity_type: An OData entity type object
    :param languages: List of languages to fetch, None to fetch all
    :return: Generator object yielding SQL lines
    """

    done = 0
    while done == 0 or done != total:
        if done == 0:
            url = create_language_filter_url(entity_type, languages)
        elif next_url:
            url = next_url
        else:
            return

        results, total, next_url = _split_response(fetcher(url))

        if total == 0:
            logger.error("Entity type {} has zero values".format(entity_type.name))
            return

        if len(results) == 0:
            """
            As of 2019-05-21, for the entity types BusinessResponsibility and XXX, the server does not return any
            results.
            """
            logger.error("Server did not return any entities for entity type {}. Did {}/{} before this.".format(
                entity_type.name, done, total))
            return

        header = [_to_snake_case(p.name) for p in entity_type.properties]
        values = []
        for result in results:
            values.append(_results_to_sql_dict(entity_type, result))
        done += len(results)
        yield (header, values)
        logger.info("Progress for {}: {}/{}".format(entity_type.name, done, total))


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--schema", type=argparse.FileType('r'), required=True)
    parser.add_argument("--include", type=str, nargs='*', help="Limit to given entities")
    parser.add_argument("--skip", type=str, nargs='*', help="Skip given entities")
    parser.add_argument("--languages", choices=LANGUAGES, nargs='+', help="Limit to given entities", default=LANGUAGES)
    parser.add_argument("-v", "--verbose",
                        dest="verbose_count",
                        action="count",
                        default=0,
                        help="Increase log verbosity for each occurrence.")
    args = parser.parse_args()
    log_level = max(3 - args.verbose_count, 0) * 10
    logger.info("Setting loglevel to {}".format(log_level))
    logger.setLevel(log_level)

    logger.info("Languages to be imported: {}".format(", ".join(args.languages)))

    xml = "".join(args.schema.readlines())
    parser = create_parser(xml)

    if args.include:
        entity_types_to_import = set(parser.get_entity_type_by_name(et) for et in args.include)
    else:
        entity_types_to_import = set(parser.entity_types)

    if args.skip:
        entity_types_to_skip = set(parser.get_entity_type_by_name(et) for et in args.skip)
    else:
        entity_types_to_skip = []

    requests_cache.install_cache('curia_vista_import')

    ranks = get_topology(parser, entity_types_to_import)
    rank_number = 1
    for rank in ranks:
        logger.info(
            "Rank {}/{}: {}".format(rank_number, len(ranks), ", ".join(str(p) for p in rank)))
        rank_number += 1

    connection = psycopg2.connect("host=localhost dbname=postgres user=postgres password=docker")

    rank_number = 1
    for rank in ranks:
        logger.info(
            "Starting work on rank {}/{}, containing {}".format(rank_number,
                                                                len(ranks),
                                                                ", ".join(str(p) for p in rank)))
        for entity_type in rank:
            if entity_type in entity_types_to_skip:
                logger.warning("Skipping entity type {}".format(entity_type.name))
                continue
            for r in fetch_entity_type(entity_type, _fetch, args.languages):
                (columns, rows) = r
                statement = 'INSERT INTO {} ({}) VALUES ({}) on CONFLICT (id, language) DO NOTHING'.format(
                    entity_type.table_name,
                    ', '.join(columns),
                    ', '.join(['%s'] * len(columns)))
                with connection.cursor() as cur:
                    def do_many(rows):
                        try:
                            cur.executemany(statement, rows)
                            connection.commit()
                            return
                        except psycopg2.errors.ForeignKeyViolation as e:
                            connection.rollback()
                            if len(rows) == 1:
                                logger.error(e)
                                return
                        except psycopg2.errors.IntervalFieldOverflow as e:
                            connection.rollback()
                            if len(rows) == 1:
                                logger.error(e)
                                return
                        except psycopg2.errors.InvalidDatetimeFormat as e:
                            connection.rollback()
                            if len(rows) == 1:
                                logger.error(e)
                                return

                        do_many(rows[len(rows) // 2:])
                        do_many(rows[:len(rows) // 2])

                    do_many(rows)

        logger.info("Finished work on rank {}/{}".format(rank_number, len(ranks)))
        rank_number += 1


if __name__ == '__main__':
    main()
