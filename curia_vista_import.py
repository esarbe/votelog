#!/usr/bin/env python3
import argparse
import datetime

import requests
from toposort import toposort, toposort_flatten

from curia_vista import create_parser, logger, _to_snake_case

URL = 'https://ws.parlament.ch/odata.svc'

EPOCH = datetime.datetime.utcfromtimestamp(0)

FIXUP = {
    'MemberCouncil': [
        {'ID': 830, 'Language': 'DE', 'PersonNumber': 1, 'GenderAsString': 'm'},
        {'ID': 831, 'Language': 'DE', 'PersonNumber': 1, 'GenderAsString': 'm'},
        {'ID': 832, 'Language': 'DE', 'PersonNumber': 1, 'GenderAsString': 'm'},
        {'ID': 833, 'Language': 'DE', 'PersonNumber': 1, 'GenderAsString': 'm'},
        {'ID': 1309, 'Language': 'DE', 'PersonNumber': 1, 'GenderAsString': 'm'},
        {'ID': 3990, 'Language': 'DE', 'PersonNumber': 1, 'GenderAsString': 'm'},
        {'ID': 3991, 'Language': 'DE', 'PersonNumber': 1, 'GenderAsString': 'm'},
        {'ID': 4010, 'Language': 'DE', 'PersonNumber': 1, 'GenderAsString': 'm'},
        {'ID': 4043, 'Language': 'DE', 'PersonNumber': 1, 'GenderAsString': 'm'},
        {'ID': 4127, 'Language': 'DE', 'PersonNumber': 1, 'GenderAsString': 'm'},
        {'ID': 4133, 'Language': 'DE', 'PersonNumber': 1, 'GenderAsString': 'm'},
        {'ID': 4211, 'Language': 'DE', 'PersonNumber': 1, 'GenderAsString': 'm'},
        {'ID': 4231, 'Language': 'DE', 'PersonNumber': 1, 'GenderAsString': 'm'},
        {'ID': 4232, 'Language': 'DE', 'PersonNumber': 1, 'GenderAsString': 'm'},
    ],
}

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


def _result_to_sql_statement_header(entity):
    column_names = [_to_snake_case(p.name) for p in entity.properties]
    yield "INSERT INTO {} ({}) VALUES".format(entity.table_name, ", ".join(column_names))


def _results_to_sql_value_statements(entity, result, last, accept_degenerated=False):
    values = []
    for p in entity.properties:
        if accept_degenerated:
            value = result[p.name] if p.name in result else None
        else:
            value = result[p.name]
        # Parse dates
        if isinstance(value, str) and value.startswith('/Date('):
            value = _parse_date(value)
        values.append(value)

    sql_values = []
    for v in values:
        if v is None:
            sql_values.append('NULL')
        else:
            sql_values.append("E'" + str(v).replace("'", "\\'") + "'")
    yield " ({}){}".format(", ".join(sql_values), ';' if last else ',')


def _fetch(url):
    """
    :param url: URL to fetch
    :return: JSON data with the entries results
    """
    logger.debug("Fetching from URL {}".format(url))
    # Using header instead of URL to request JSON because __next forgets about it.
    r = requests.get(url, headers={'Accept': 'application/json'})
    logger.debug("HTTP code {} after {} seconds".format(r.status_code, url, r.elapsed.total_seconds()))
    return r.json()


def _split_response(json):
    json_data = json['d']
    return (
        json_data if 'results' not in json_data else json_data['results'],
        int(json_data['__count']),
        json_data['__next'] if '__next' in json_data else None
    )


def craft_url(entity, done, batch_size, languages=None):
    url = '{}/{}?$inlinecount=allpages&$select=*'.format(URL, entity.name)

    # Note: Every single Entity in the Curia Vista schema has a Language property
    if languages:
        url += '&$filter='
        url += " or ".join(['(Language%20eq%20%27{}%27)'.format(l) for l in languages])

    return "{}&$skip={}&$top={}".format(url, done, batch_size)


def fetch_all(entity, fetcher, languages=None, batch_size=1000):
    """
    Generator for JSON objects

    :param fetcher: Callable taking a single argument (URL) pointing to the resource to be fetched
    :param entity: An OData entity object
    :param languages: List of languages to fetch, None to fetch all
    :return: Generator object yielding SQL lines
    """

    done = 0
    while done == 0 or done != total:
        url = craft_url(entity, done, batch_size, languages)
        results, total, next_url = _split_response(fetcher(url))

        if total == 0:
            logger.warning("Entity {} has zero values".format(entity.name))
            return

        if next_url:
            raise RuntimeError("Handling of __next not implemented")

        # As of 2019-05-02, some entities in MemberCouncil get referred to, but do not exist.
        # Workaround: Adding dummy entries
        if done == 0 and entity.name in FIXUP:
            yield from _result_to_sql_statement_header(entity)
            for fixup in FIXUP[entity.name]:
                yield from _results_to_sql_value_statements(entity, fixup, fixup == FIXUP[entity.name][-1], True)

        # Yield insert-into statement for first fetched slice only
        if done == 0:
            yield from _result_to_sql_statement_header(entity)

        for result in results:
            done += 1
            yield from _results_to_sql_value_statements(entity, result, done == total)
        logger.info("Progress for {}: {}/{}".format(entity.name, done, total))


def _build_dependencies(parser, entities, map_of_dependencies):
    for entity in entities:
        map_of_dependencies[entity] = parser.get_dependencies(entity, recursive=False)
        _build_dependencies(parser, map_of_dependencies[entity], map_of_dependencies)


def _create_ranks(parser, entities):
    map_of_dependencies = {}
    _build_dependencies(parser, entities, map_of_dependencies)
    return list(toposort(map_of_dependencies))


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--schema", type=argparse.FileType('r'), required=True)
    parser.add_argument("--include", type=str, nargs='*', help="Limit to given entities")
    parser.add_argument("--skip", type=str, nargs='*', help="Skip given entities")
    parser.add_argument("--languages", choices=LANGUAGES, nargs='+', help="Limit to given entities", default=LANGUAGES)
    args = parser.parse_args()

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

    # Requesting certain entity types causes a server side error
    entity_types_to_import -= {parser.get_entity_type_by_name('PersonCommunication'),
                               parser.get_entity_type_by_name('BusinessResponsibility')}

    ranks = _create_ranks(parser, entity_types_to_import)
    rank_number = 1
    for rank in ranks:
        logger.info(
            "Rank {}/{}: {}".format(rank_number, len(ranks), ", ".join(str(p) for p in rank)))
        rank_number += 1

    rank_number = 1
    for rank in ranks:
        logger.info(
            "Starting work on rank {}/{}, containing {}".format(rank_number,
                                                                len(ranks),
                                                                ", ".join(str(p) for p in rank)))
        for entity in rank:
            if entity in entity_types_to_skip:
                logger.warning("Skipping entity type {}".format(entity.name))
                continue
            for line in fetch_all(entity, _fetch, args.languages):
                print(line)

        logger.info("Finished work on rank {}/{}".format(rank_number, len(ranks)))
        rank_number += 1


if __name__ == '__main__':
    main()
