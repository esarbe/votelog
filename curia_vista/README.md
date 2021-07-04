# About

This project copies the Curia Vista data into a (local) database. This allows to run arbitrary queries which would not
be possible using the official API.

## Background

Curia Vista is the database of parliamentary proceedings. It contains details of items of business since the winter
session 1995 (Federal Council dispatches, procedural requests, elections, petitions, etc.).

Source and further reading: [parlament.ch](https://www.parlament.ch/en/ratsbetrieb/curia-vista)

## Setup

### Debian 11

```console
apt install \
  graphviz \
  python3-psycopg2 \
  python3-requests-cache \
  python3-toposort
```

## Example: Secure Database Socket Forwarding

This is optional, but simplifies setting up a secure connection to the database server. Also, the remaining
documentation in this file assumes that the database is accessible on 127.0.0.1:5432.

```console
ssh votelog -N -L 5432:127.0.0.1:5432
```

## Update XML Schema

While not strictly necessary, it makes debugging easier when we have a history of the XML manifest.

```console
curl -sS 'https://ws.parlament.ch/odata.svc/$metadata' | xmllint --format - > doc/$(date +%Y-%m-%d)-metadata.xml
ln -sf $(date +%Y-%m-%d)-metadata.xml doc/metadata.xml
```

## Mirroring: Schema Creation

This tool converts the [OData 3.0](https://www.odata.org/documentation/odata-version-3-0/) based
[metadata description](https://ws.parlament.ch/OData.svc/$metadata) to an SQL schema.

```console
python3 schema.py --schema doc/metadata.xml > schema.sql
psql --host=127.0.0.1 --user curiavista < schema.sql
```

## Mirroring: Dependency Checking

```console
python3 dot.py --schema doc/metadata.xml | dotty -
```

## Mirroring: Data Import

```console
python3 sync.py --schema doc/metadata.xml
```

Custom database user, name, port, etc. can be passed using arguments. The database password needs to be provided using
the ~/.pgpass file.

### Use Cache

Using a cache allows to re-run an import much quicker. Beware of the (undocumented) dragons!

```console
python3 sync.py --cache curia_vista_import_cache --schema doc/metadata.xml
```

### Limit to a Single Language

```console
python3 sync.py --languages DE --schema doc/metadata.xml -u $DB_USER_NAME -p $DB_USER_PASSWORD -d $DB_NAME
```
