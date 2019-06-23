#!/usr/bin/env python3
import argparse

from odata.sql import to_schema


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--schema", type=argparse.FileType('r'), required=True)
    args = parser.parse_args()

    crapshot_mariadb_helper_function = """DELIMITER //
CREATE FUNCTION UuidToBin(_uuid BINARY(36))
    RETURNS BINARY(16)
    LANGUAGE SQL  DETERMINISTIC  CONTAINS SQL  SQL SECURITY INVOKER
RETURN
    UNHEX(CONCAT(
        SUBSTR(_uuid, 15, 4),
        SUBSTR(_uuid, 10, 4),
        SUBSTR(_uuid,  1, 8),
        SUBSTR(_uuid, 20, 4),
        SUBSTR(_uuid, 25) ));
//

    """
    xml = "".join(args.schema.readlines())
    print(crapshot_mariadb_helper_function + to_schema(xml))


if __name__ == '__main__':
    main()
