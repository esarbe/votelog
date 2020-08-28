[![Build Status](https://travis-ci.org/esarbe/votelog.svg?branch=master)](https://travis-ci.org/esarbe/votelog)

# Votelog


**`votelog`** is a http server application that provides a convenient interface for the Swiss parlaments's CuriaVista datase (https://www.parlament.ch/de/ratsbetrieb/curia-vista). Work on the M1 Milestone is ongoing, for M1 the following features are planned:

[X] Creation of user accounts
[X] Protection of resources using permission framework
[X] Querying parlamentarian by name, by legislative period and by busines
[X] Querying businesses by name, legislative period and by parlamentarian

[ ] Rating of businesses by users, assignment of 'prefered' results by users
[ ] Querying parlamentarian by user rating


`votelog` is written in the Scala programming language and uses `sbt` for building the software. To 
build `votelog` locally, install sbt.

## Running votelog web server

Clone and checkout repository

```bash
$ git clone git@github.com:esarbe/votelog.git
```
Launch into the sbt build environment

```bash
$ cd votelog
$ sbt
```

Start webserver application
```sbtshell
sbt:votelog> run
```
## Http server configuration
The `votelog` http server application configuration is located at `src/main/resources/application.conf`. It
allows configuration of port and interface the http server listens to. The configuration settings can be
overwritten by setting the corresponding environment variables.

| Environment Variable |  Function|
|   --                 |   -- |
| HTTP_PORT | HTTP port |
| HTTP_INTERFACE | HTTP interface |

## Votelog REST interface
| Method | Path |- Function |
|  --    | --   | -- |
|`GET` | `/api/politican/index` | get politician ids |
| `POST` | `/api/politician/{id}` | get politician identified by `id`
...

## Starting local docker for testing
```bash
docker-compose -f testing/docker-compose.yml up postgres
```

# License
This project is licensed under the AGPL 3.0. See [LICENSE.md](LICENSE.md)

This project includes parts that are licensed unter the The MIT License (MIT). 
The corresponding source files have an appropriate license header.
