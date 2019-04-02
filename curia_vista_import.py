#!/usr/bin/env python3
import argparse
import logging
import re
import sys
import xml.etree.ElementTree as ET

logger = logging.getLogger(__name__)

NS = '{http://schemas.microsoft.com/ado/2009/11/edm}'

NAMESPACES = {
    'edm': "{http://schemas.microsoft.com/ado/2009/11/edm}"
}

EDM_TO_SQL_SIMPLE = {
    'Edm.Boolean': 'boolean',
    'Edm.Int16': 'smallint',
    'Edm.Int32': 'integer',
    'Edm.Int64': 'bigint',
    'Edm.DateTime': 'timestamp',
    'Edm.Guid': 'uuid',
    'Edm.DateTimeOffset': 'interval',
}

SQL_KEYWORDS = {
    'start': 'start_',
    'end': 'end_',
}


def _to_snake_case(name):
    """
    Convert a CamelCase string to snake_case.
    Source: https://stackoverflow.com/a/1176023/2200540
    """
    s1 = re.sub('(.)([A-Z][a-z]+)', r'\1_\2', name)
    s2 = re.sub('([a-z0-9])([A-Z])', r'\1_\2', s1).lower()
    if s2 in SQL_KEYWORDS:
        return SQL_KEYWORDS[s2]
    return s2


class Key:
    def __init__(self, root):
        self.root = root
        self.property_refs = [_to_snake_case(k.attrib['Name']) for k in self.root.iter(NS + 'PropertyRef')]

    def to_schema(self):
        return "PRIMARY KEY ({})".format(", ".join(self.property_refs))


class Property:
    def __init__(self, root):
        self.root = root
        self.name = self.root.attrib['Name']
        self.type = self.root.attrib['Type']
        self.nullable = True
        self.fixed_length = False
        self.max_length = None
        self.parse_facets()

    def to_schema(self):
        res = _to_snake_case(self.name)

        if self.type in EDM_TO_SQL_SIMPLE:
            type_sql = EDM_TO_SQL_SIMPLE[self.type]
        else:
            if self.type == "Edm.String":
                if isinstance(self.max_length, int):
                    if self.fixed_length:
                        type_sql = "char({})".format(self.max_length)
                    else:
                        type_sql = "varchar({})".format(self.max_length)
                else:
                    assert self.fixed_length is False
                    type_sql = "TEXT"
            else:
                raise RuntimeError("Unknown type: " + self.type)
        res += " {}".format(type_sql)
        if not self.nullable:
            res += " NOT NULL"
        return res

    def parse_facets(self):
        for attr in self.root.attrib:
            if attr in ['Name', 'Type']:
                continue
            if attr == 'Nullable':
                self.nullable = self.root.attrib[attr] == 'true'
                continue
            if attr == 'FixedLength':
                self.fixed_length = self.root.attrib[attr] == 'true'
                continue
            if attr == 'MaxLength':
                try:
                    self.max_length = int(self.root.attrib[attr])
                except ValueError:
                    pass
                continue
            logger.warning("Unhandled facet in Property {}: {}".format(self.name, attr))


class EntityType:
    def __init__(self, root):
        self.root = root
        self.table_name = _to_snake_case(self.root.attrib['Name'])
        self.properties = [Property(p) for p in self.root.iter(NS + 'Property')]
        keys = list(self.root.iter(NS + 'Key'))
        assert len(keys) == 1
        self.key = Key(keys[0])

    def to_schema(self):
        res = "CREATE TABLE {} (\n  ".format(self.table_name)
        res += ",\n  ".join([p.to_schema() for p in self.properties]) + ","
        res += "\n  {}".format(self.key.to_schema())
        res += "\n);"
        return res


class ODataParser:
    def __init__(self, root):
        self.root = root
        self.schema = self.root[0][0]
        self.entities = []
        self._parse_entity_types()

    def to_schema(self):
        return '\n\n'.join([e.to_schema() for e in self.entities])

    def _parse_entity_types(self):
        for entity_type in self.schema.iter(NS + 'EntityType'):
            self.entities.append(EntityType(entity_type))


def schema_to_sql(metadata: str):
    """
    Parse an XML string of an Open Data Protocol (OData) schema and return the SQL query to construct the respective
    table in a SQL database.
    """
    root = ET.fromstring(metadata)

    parser = ODataParser(root)
    return parser.to_schema()


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("metadatafile", type=argparse.FileType('r'))
    args = parser.parse_args()

    handler = logging.StreamHandler(sys.stdout)
    logger.setLevel(logging.INFO)
    formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')
    handler.setFormatter(formatter)
    logger.addHandler(handler)

    xml = "".join(args.metadatafile.readlines())
    print(schema_to_sql(xml))


if __name__ == '__main__':
    main()
