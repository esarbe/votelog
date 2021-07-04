#!/usr/bin/env python3
import argparse
import datetime

import pymysql.cursors
import pymysql
import requests
from pymysql import ProgrammingError

from odata.odata import create_parser, logger, _to_snake_case
from odata.topology import get_topology

URL = 'https://ws.parlament.ch/odata.svc'

EPOCH = datetime.datetime.utcfromtimestamp(0)

LANGUAGES = ['DE', 'EN', 'FR', 'IT', 'RM']

SYNC_BY_FK = {
    'Voting': 'Vote'
}


def _parse_date(datestring):
    """
    Convert  "ASP.Net JSON Date" (e.g. '/Date(1293368772797)/') to a timestamp string understood by the SQL driver.
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


def _get_json(url):
    """
    :param url: URL to fetch
    :return: JSON data with the entries results
    """
    logger.debug(f"Fetching from URL {url}")
    # Using header instead of URL to request JSON because __next forgets about it.
    r = requests.get(url, headers={'Accept': 'application/json'})
    if hasattr(r, 'from_cache') and r.from_cache:
        logger.debug(f"Found URL {url} in cache")
    else:
        logger.debug(f"HTTP code {r.status_code} after {r.elapsed.total_seconds()} seconds")
    return r.json()


def _split_response(json):
    try:
        json_data = json['d']
    except KeyError as e:
        logger.error(f"Could no find element 'd' in {json}")
        raise e
    return (
        json_data if 'results' not in json_data else json_data['results'],
        int(json_data['__count']),
        json_data['__next'] if '__next' in json_data else None
    )


def create_entity_type_url(entity_type):
    return f'{URL}/{entity_type.name}?$inlinecount=allpages'


def create_language_filter_url(entity_type, languages=None):
    # Note: Every single entity type in the Curia Vista schema has a Language property
    if languages:
        return f'{create_entity_type_url(entity_type)}&$filter={" or ".join([f"(Language%20eq%20%27{l}%27)" for l in languages])}'
    return create_entity_type_url(entity_type)


def create_filter_url(entity_type, languages=None, fk_value=None):
    filters = []
    # Note: Every single entity type in the Curia Vista schema has a Language property
    if languages:
        filters.append(' or '.join([f'(Language%20eq%20%27{l}%27)' for l in languages]))
    if fk_value:
        filters.append(' and '.join([f'({k}%20eq%20{v})' for k, v in fk_value.items()]))
    return f'{create_entity_type_url(entity_type)}&$filter={" and ".join(filters)}'


def fetch_all_entities(entity_type, fetcher, url):
    """
    Generator which yields tuples in the form of (list of column names, list of rows).

    :param url: Location where to fetch the entities from, most likely via HTTP. Must be understood by fetcher.
    :param fetcher: Callable taking a single argument, URL, returning the data it points to.
    :param entity_type: An OData entity type object. Mainly used to make us independent of the ordering of the elements
                        in the JSON response the fetcher will deliver us.
    :return: Generator object yielding tuples in the form of (list of column names, list of rows) for the.
    """

    done = 0
    while done == 0 or done != total:
        if not url:
            return

        results, total, url = _split_response(fetcher(url))

        if total == 0:
            logger.error(f"Entity type {entity_type.name} has zero values")
            return

        if len(results) == 0:
            """
            As of 2019-05-21, for the entity types BusinessResponsibility and PersonCommunication, the server does not
            return any results.
            """
            logger.error(
                f"Server did not return any entities for entity type {entity_type.name}. Did {done}/{total} before this.")
            return

        header = [_to_snake_case(p.name) for p in entity_type.properties]
        values = []
        for result in results:
            values.append(_results_to_sql_dict(entity_type, result))
        done += len(results)
        yield (header, values)
        logger.info(f"Progress for {entity_type.name}: {done}/{total}")


def fetch_entity_type(entity_type, fetcher, languages=None):
    url = create_language_filter_url(entity_type, languages)
    yield from fetch_all_entities(entity_type, fetcher, url)


def fetch_entities_by_fk(entity_type_to_fetch, fk, fetcher, languages=None):
    assert entity_type_to_fetch.name == 'Voting'
    url = create_filter_url(entity_type_to_fetch, languages, fk)
    yield from fetch_all_entities(entity_type_to_fetch, fetcher, url)


def update_db(connection, table_name, columns, rows):
    statement = f'INSERT INTO {table_name} ({", ".join(columns)}) VALUES ({", ".join(["%s"] * len(columns))}) ON DUPLICATE KEY UPDATE {", ".join([f"{c}=VALUES({c})" for c in columns])};'
    with connection.cursor() as cur:
        def do_many(rows):
            try:
                cur.executemany(statement, rows)
                connection.commit()
                return
            except ProgrammingError as e:
                connection.rollback()
                logger.fatal(e)
                exit(-1)
            except Exception as e:
                connection.rollback()
                if len(rows) == 1:
                    logger.error(f"{str(rows[0])}: {e}")
                    return

            do_many(rows[len(rows) // 2:])
            do_many(rows[:len(rows) // 2])

        do_many(rows)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--schema", type=argparse.FileType('r'), required=True)
    parser.add_argument("--include",
                        type=str,
                        nargs='*',
                        help="Entity types to sync, including relevant dependencies. If not given, sync all.")
    parser.add_argument("--skip", type=str, nargs='*', help="Skip the listed entities")
    parser.add_argument("--languages", choices=LANGUAGES, nargs='+', help="Limit syncing to given languages",
                        default=LANGUAGES)
    parser.add_argument("--cache", type=str,
                        help="Cache HTTP requests in <cache>.sqlite. Useful to speed up development.")
    parser.add_argument("-v", "--verbose",
                        dest="verbose_count",
                        action="count",
                        default=0,
                        help="Increase log verbosity for each occurrence.")
    parser.add_argument("-u", "--user", type=str, required=True)
    parser.add_argument("-p", "--password", type=str, required=True)
    parser.add_argument("-H", "--host", type=str, default="localhost")
    parser.add_argument("-P", "--port", type=int, default=3306)
    parser.add_argument("-d", "--database", type=str)
    args = parser.parse_args()
    log_level = max(3 - args.verbose_count, 0) * 10
    logger.info(f"Setting loglevel to {log_level}")
    logger.setLevel(log_level)

    logger.info(f"Languages to be imported: {', '.join(args.languages)}")

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

    if args.cache:
        import requests_cache
        requests_cache.install_cache(args.cache)

    ranks = get_topology(parser, entity_types_to_import)
    rank_number = 1
    for rank in ranks:
        logger.info(
            f"Rank {rank_number}/{len(ranks)}: {', '.join(str(p) for p in rank)}")
        rank_number += 1

    connection = pymysql.connect(user=args.user, password=args.password, database=args.database, host=args.host,
                                 port=args.port)

    rank_number = 1
    for rank in ranks:
        logger.info(
            f"Starting work on rank {rank_number}/{len(ranks)}, containing {', '.join(str(p) for p in rank)}")
        for entity_type in rank:
            if entity_type in entity_types_to_skip:
                logger.warning(f"Skipping entity type {entity_type.name}")
                continue
            logger.info(f"Starting to import {entity_type.name}")

            if entity_type.name == 'Voting':
                with connection.cursor() as cur:
                    cur.execute('SELECT id FROM vote;')
                    index = 0
                    all_vote_ids = cur.fetchall()
                    for row in all_vote_ids:
                        logger.info(f'Voting by Vote: {index}/{len(all_vote_ids)} done')
                        for (columns, rows) in fetch_entities_by_fk(entity_type, {'IdVote': row[0]}, _get_json,
                                                                    args.languages):
                            update_db(connection, entity_type.table_name, columns, rows)
                        index += 1
            else:
                for (columns, rows) in fetch_entity_type(entity_type, _get_json, args.languages):
                    update_db(connection, entity_type.table_name, columns, rows)

        logger.info(f"Finished work on rank {rank_number}/{len(ranks)}")
        rank_number += 1


if __name__ == '__main__':
    main()
