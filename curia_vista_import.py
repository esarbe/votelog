#!/usr/bin/env python3
import argparse
from json import JSONDecodeError

import requests

from curia_vista import create_parser, logger, _to_snake_case

URL = 'https://ws.parlament.ch/odata.svc/'


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--schema", type=argparse.FileType('r'))
    args = parser.parse_args()

    xml = "".join(args.schema.readlines())
    parser = create_parser(xml)

    for entity in parser.entities:
        url = "{}{}?$select={}".format(URL, entity.name, ",".join(p.name for p in entity.properties))
        r = requests.get(url, headers={'Accept': 'application/json'})
        logger.info("HTTP code {} for url {}".format(r.status_code, url))
        try:
            j = r.json()
        except JSONDecodeError as e:
            logger.error("Failed to decode JSON message: {}".format(e))
            return
        for result in j['d']['results']:
            column_names = []
            values = []
            for name, value in result.items():
                if isinstance(value, dict):
                    continue
                column_names.append(_to_snake_case(name))
                values.append(value)

        # command = 'INSERT OR REPLACE INTO %s VALUES (%s)' % (us(table), ', '.join(['?'] * len(column_names)))


if __name__ == '__main__':
    main()
