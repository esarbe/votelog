# About

This project tracks and ranks the voting behavior of politicians in the Swiss parliament.

# Curia Vista

This project needs to copy the Curia Vista database in order to process its content quickly and independently.

## Background
Curia Vista is the database of parliamentary proceedings. It contains details of items of business since the winter
session 1995 (Federal Council dispatches, procedural requests, elections, petitions, etc.).

Source and further reading: [parlament.ch](https://www.parlament.ch/en/ratsbetrieb/curia-vista)

## Mirroring: Schema Creation

This tool converts the [OData 3.0](https://www.odata.org/documentation/odata-version-3-0/) based
[metadata description](https://ws.parlament.ch/OData.svc/$metadata) to an SQL schema.

```console
python3 curia_vista_schema.py --schema doc/metadata.xml > schema.sql
```

## Mirroring: Data Import

```console
python3 curia_vista_import.py --schema doc/metadata.xml > data.sql
```