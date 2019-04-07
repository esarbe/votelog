#!/usr/bin/env python3
import argparse

from curia_vista import schema_to_sql


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--schema", type=argparse.FileType('r'), required=True)
    args = parser.parse_args()

    xml = "".join(args.schema.readlines())
    print(schema_to_sql(xml))


if __name__ == '__main__':
    main()
