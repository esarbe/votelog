#!/usr/bin/env python3
import argparse

from odata.sql import to_schema


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--schema", type=argparse.FileType('r'), required=True)
    args = parser.parse_args()
    print(to_schema(args.schema.readlines()))


if __name__ == '__main__':
    main()
