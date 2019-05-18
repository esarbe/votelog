#!/usr/bin/env python3
import argparse
import datetime

import requests
import requests_cache

from odata.odata import create_parser, logger, _to_snake_case
from odata.topology import get_topology

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

SKIP_WORKAROUNDS = {
    'Voting': 10000  # For the Voting table, the limit is at 1M. Only 50k however can still be served in less than 30s.
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


def create_entity_type_url(entity):
    return '{}/{}?$inlinecount=allpages'.format(URL, entity.name)


def create_language_filter_url(entity, languages=None):
    # Note: Every single Entity in the Curia Vista schema has a Language property
    if languages:
        return '{}&$filter={}'.format(create_entity_type_url(entity),
                                      ' or '.join(['(Language%20eq%20%27{}%27)'.format(l) for l in languages]))
    return create_entity_type_url(entity)


def create_skip_url(entity, done, batch_size, languages=None):
    return '{}&$skip={}&$top={}'.format(create_language_filter_url(entity, languages), done, batch_size)


def fetch_all(entity, fetcher, languages=None, skip_workaround=False, batch_size=1000):
    """
    Generator for JSON objects

    :param skip_workaround:
    :param batch_size: Number of entities to be fetched at once
    :param fetcher: Callable taking a single argument (URL) pointing to the resource to be fetched
    :param entity: An OData entity object
    :param languages: List of languages to fetch, None to fetch all
    :return: Generator object yielding SQL lines
    """

    done = 0
    while done == 0 or done != total:
        if skip_workaround:
            url = create_skip_url(entity, done, batch_size, languages)
        elif done == 0:
            url = create_language_filter_url(entity, languages)
        elif next_url:
            url = next_url
        else:
            return

        results, total, next_url = _split_response(fetcher(url))

        if total == 0:
            logger.warning("Entity {} has zero values".format(entity.name))
            return

        if next_url and skip_workaround:
            raise RuntimeError("Handling of __next combined with skipping not implemented")

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


def fetch_all_with_workaround(entity, fetcher, languages):
    if entity.name in SKIP_WORKAROUNDS:
        return fetch_all(entity, fetcher, languages, True, SKIP_WORKAROUNDS[entity.name])
    return fetch_all(entity, fetcher, languages, False)


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

    # # Requesting certain entity types causes a server side error
    # entity_types_to_import -= {parser.get_entity_type_by_name('PersonCommunication'),
    #                            parser.get_entity_type_by_name('BusinessResponsibility')}

    ranks = get_topology(parser, entity_types_to_import)
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
            for line in fetch_all_with_workaround(entity, _fetch, args.languages):
                print(line)

        logger.info("Finished work on rank {}/{}".format(rank_number, len(ranks)))
        rank_number += 1


if __name__ == '__main__':
    main()
