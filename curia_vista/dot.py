#!/usr/bin/env python3
import argparse

from odata.odata import create_parser
from odata.topology import get_topology


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--schema", type=argparse.FileType('r'), required=True)
    parser.add_argument("--include", type=str, nargs='*', help="Limit to given entities")
    args = parser.parse_args()

    xml = "".join(args.schema.readlines())
    parser = create_parser(xml)

    if args.include:
        entity_types_to_import = set(parser.get_entity_type_by_name(et) for et in args.include)
    else:
        entity_types_to_import = set(parser.entity_types)

    print("digraph G {")
    print("  rankdir=LR;")
    rank_num = 0
    ranks = get_topology(parser, entity_types_to_import)
    relevant_entities = []
    for rank in ranks:
        print(f"  subgraph rank_{rank_num} {{")
        print("    rank=same;")
        for entity_type in rank:
            print(f"    {entity_type.name};")
            relevant_entities.append(entity_type)
        print("  }")
        rank_num += 1

    for entity_type in relevant_entities:
        for dependency in parser.get_dependencies(entity_type, recursive=False):
            print(f"  {entity_type.name} -> {dependency.name};")
    print("}")


if __name__ == '__main__':
    main()
