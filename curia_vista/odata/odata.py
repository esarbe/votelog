#!/usr/bin/env python3
import logging
import sys
import re
import xml.etree.ElementTree as ET

logger = logging.getLogger(__name__)
handler = logging.StreamHandler(sys.stderr)
logger.setLevel(logging.INFO)
formatter = logging.Formatter('%(asctime)s %(name)s %(levelname)s %(message)s')
handler.setFormatter(formatter)
logger.addHandler(handler)

NS = '{http://schemas.microsoft.com/ado/2009/11/edm}'

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
                logger.debug(f"Unhandled facet in Property {self.name}: {attr}")
                continue
            raise RuntimeError(f"Unhandled facet in Property {self.name}: {attr}")


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


class ReferentialConstraint:
    def __init__(self, root):
        self.root = root
        self.principal = PrincipalOrDependent(self.root.find(NS + 'Principal'))
        self.dependent = PrincipalOrDependent(self.root.find(NS + 'Dependent'))


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
            raise RuntimeError(f"Missing ReferentialConstraint on Association {self.name}")
        self.referential_constraint = ReferentialConstraint(referential_constraint)

    def __str__(self):
        return self.name


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
        raise RuntimeError(f"Could not find EntityType {name}")

    def _get_association_by_name(self, name):
        for association in self.associations:
            if association.name == name:
                return association
        raise RuntimeError(f"Could not find Association {name}")


def create_parser(metadata: str):
    root = ET.fromstring(metadata)
    return OData(root)
