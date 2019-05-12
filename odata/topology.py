from toposort import toposort


def _build_dependencies(odata, entity_types, map_of_dependencies):
    for entity_type in entity_types:
        map_of_dependencies[entity_type] = odata.get_dependencies(entity_type, recursive=False)
        _build_dependencies(odata, map_of_dependencies[entity_type], map_of_dependencies)


def get_topology(odata, root_entity_types):
    map_of_dependencies = {}
    _build_dependencies(odata, root_entity_types, map_of_dependencies)
    return list(toposort(map_of_dependencies))
