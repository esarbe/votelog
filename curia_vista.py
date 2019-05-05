#!/usr/bin/env python3
import logging
import sys
import re
import xml.etree.ElementTree as ET

from toposort import toposort

logger = logging.getLogger(__name__)
handler = logging.StreamHandler(sys.stderr)
logger.setLevel(logging.INFO)
formatter = logging.Formatter('%(asctime)s %(name)s %(levelname)s %(message)s')
handler.setFormatter(formatter)
logger.addHandler(handler)

NS = '{http://schemas.microsoft.com/ado/2009/11/edm}'

NAMESPACES = {
    'edm': "{http://schemas.microsoft.com/ado/2009/11/edm}"  # CSDL version 3.0
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
        self._parse_facets()

    def __str__(self):
        return self.name

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

    def _parse_facets(self):
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
            if attr in ('Unicode', 'Precision'):
                logger.debug("Unhandled facet in Property {}: {}".format(self.name, attr))
                continue
            raise RuntimeError("Unhandled facet in Property {}: {}".format(self.name, attr))


class EntityType:
    def __init__(self, root):
        self.root = root
        self.name = self.root.attrib['Name']
        self.table_name = _to_snake_case(self.name)
        self.properties = [Property(p) for p in self.root.iter(NS + 'Property')]
        keys = list(self.root.iter(NS + 'Key'))
        assert len(keys) == 1
        self.key = Key(keys[0])

    def __str__(self):
        return self.name

    def to_schema(self):
        res = "CREATE TABLE {} (\n  ".format(self.table_name)
        res += ",\n  ".join([p.to_schema() for p in self.properties]) + ","
        res += "\n  {}".format(self.key.to_schema())
        res += "\n);"
        return res


class End:
    def __init__(self, root):
        self.root = root
        self.role = self.root.attrib['Role']
        self.table_name = _to_snake_case(self.role)
        self.multiplicity = _to_snake_case(self.root.attrib['Multiplicity'])
        assert self.multiplicity in ('*', '1', '0..1')

    def __str__(self):
        return self.role


class PrincipalOrDependent:
    def __init__(self, root):
        self.root = root
        self.role = root.attrib['Role']
        self.table_name = root.attrib['Role']
        self.property_ref = [_to_snake_case(t.attrib['Name']) for t in self.root.iter(NS + 'PropertyRef')]

    def to_schema(self):
        return ", ".join(self.property_ref)


class ReferentialConstraint:
    def __init__(self, root):
        self.root = root
        self.principal = PrincipalOrDependent(self.root.find(NS + 'Principal'))
        self.dependent = PrincipalOrDependent(self.root.find(NS + 'Dependent'))

    def to_schema(self):
        pass


class Association:
    BROKEN_REFERENTIAL_CONSTRAINTS = {
        'session_meeting': "id_session, language",
        'session_business': "submission_session, language",
        'session_vote': "id_session, language",
    }

    def __init__(self, root):
        self.root = root
        self.name = self.root.attrib['Name']
        self.constrain_name = _to_snake_case(self.name)
        self.principal = End(self.root[0])
        self.dependent = End(self.root[1])
        referential_constraint = self.root.find(NS + 'ReferentialConstraint')
        if not referential_constraint:
            raise RuntimeError("Missing ReferentialConstraint on Association {}".format(self.name))
        self.referential_constraint = ReferentialConstraint(referential_constraint)

    def __str__(self):
        return self.name

    def to_schema(self):
        if self.principal.multiplicity == '0..1' and self.dependent.multiplicity == '*':
            return None
        elif self.principal.multiplicity == '1' and self.dependent.multiplicity == '*':
            dependent = Association.BROKEN_REFERENTIAL_CONSTRAINTS[
                self.constrain_name] if self.constrain_name in Association.BROKEN_REFERENTIAL_CONSTRAINTS else self.referential_constraint.dependent.to_schema()
            return "ALTER TABLE {}\n".format(self.dependent.table_name) + \
                   "ADD CONSTRAINT {} FOREIGN KEY ({}) REFERENCES {} ({});".format(self.constrain_name,
                                                                                   dependent,
                                                                                   self.principal.table_name,
                                                                                   self.referential_constraint.principal.to_schema())
        else:
            raise RuntimeError(
                "Not implemented Association: Principal='{}', Dependent='{}'".format(self.principal.multiplicity,
                                                                                     self.dependent.multiplicity))


class OData:
    def __init__(self, root):
        self.root = root
        self.schema = self.root[0][0]
        self.entity_types = []
        self.associations = []

        for entity_type in self.schema.iter(NS + 'EntityType'):
            self.entity_types.append(EntityType(entity_type))

        for association in self.schema.iter(NS + 'Association'):
            self.associations.append(Association(association))

    def __str__(self):
        return self.schema.attrib['Namespace']

    def get_dependencies(self, entity_type, recursive=True):
        """
        Return EntityTypes to which entity_type refers to on either directly or indirectly.
        """
        entities = set()
        for association in self.associations:
            if association.dependent.role == entity_type.name:
                et = self.get_entity_type_by_name(association.principal.role)
                if et not in entities:
                    entities.add(et)
                    if recursive:
                        entities |= self.get_dependencies(et)
        return entities

    def get_dependants(self, entity_type):
        """
        Return EntityTypes which refer to entity_type either directly or indirectly.
        """
        entities = set()
        for association in self.associations:
            if association.principal.role == entity_type.name:
                et = self.get_entity_type_by_name(association.dependent.role)
                if et not in entities:
                    entities.add(et)
                    entities |= self.get_dependants(et)
        return entities

    def get_entity_type_by_name(self, name):
        for entity_type in self.entity_types:
            if entity_type.name == name:
                return entity_type
        raise RuntimeError("Could not find EntityType {}".format(name))

    def _get_association_by_name(self, name):
        for association in self.associations:
            if association.name == name:
                return association
        raise RuntimeError("Could not find Association {}".format(name))

    def to_schema(self):
        sections = []
        if self.entity_types:
            sections.append('\n\n'.join([e.to_schema() for e in self.entity_types]))
        if self.associations:
            sections.append('\n\n'.join(a.to_schema() for a in self.associations if a.to_schema()))

        return "\n\n".join(sections)

    def _build_dependencies(self, entity_types, map_of_dependencies):
        for entity_type in entity_types:
            map_of_dependencies[entity_type] = self.get_dependencies(entity_type, recursive=False)
            self._build_dependencies(map_of_dependencies[entity_type], map_of_dependencies)

    def get_topology(self, root_entity_types):
        map_of_dependencies = {}
        self._build_dependencies(root_entity_types, map_of_dependencies)
        return list(toposort(map_of_dependencies))


def create_parser(metadata: str):
    root = ET.fromstring(metadata)
    return OData(root)


def schema_to_sql(metadata: str):
    """
    Parse an XML string of an Open Data Protocol (OData) schema and return the SQL query to construct the respective
    table in a SQL database.
    """
    return create_parser(metadata).to_schema()
