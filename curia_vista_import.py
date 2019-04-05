#!/usr/bin/env python3
import argparse

from curia_vista import create_parser

URL = 'https://ws.parlament.ch/odata.svc/'


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--schema", type=argparse.FileType('r'))
    args = parser.parse_args()

    xml = "".join(args.schema.readlines())
    parser = create_parser(xml)

    for e in parser.entities:
        print(e.table_name)

    # parsed = json.loads(fetch(url))


if __name__ == '__main__':
    main()
