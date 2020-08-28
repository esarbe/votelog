[![Build Status](https://travis-ci.org/esarbe/votelog.svg?branch=master)](https://travis-ci.org/esarbe/votelog)

# Votelog


**`votelog`** is a http server application that provides a convenient interface for the Swiss parlaments's CuriaVista datase (https://www.parlament.ch/de/ratsbetrieb/curia-vista). Work on the M1 Milestone is ongoing.

For M1 the following features are planned:

- [X] Creation of user accounts
- [X] Protection of resources using permission framework
- [X] Querying parlamentarian by name, by legislative period and by busines
- [X] Querying businesses by name, legislative period and by parlamentarian

- [ ] Rating of businesses by users, assignment of 'prefered' results by users
- [ ] Querying parlamentarian by user rating


The server part of `votelog` is written in Scala and uses `sbt` for building the software. To 
build `votelog` locally, install sbt.

The `votelog` part facing the Curia Vista database is written in Python.

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
| CURIAVISTA_DATABASE_URL | CuriaVista dump database url |
| CURIAVISTA_DATABASE_PASSWORD | CuriaVista dump database password  |
| CURIAVISTA_DATABASE_USER | CuriaVista dump database user |
| SECURITY_PASSWORD_SALT | application security salt |
| SECURITY_SECRET | application security secret |
| VOTELOG_DATABASE_URL | votelog database url |
| VOTELOG_DATABASE_USER | votelog database user |
| VOTELOG_DATABASE_PASSWORD | votelog database password |




## Votelog REST interface
| Method | Path |- Function |
|  --    | --   | -- |
|`GET` | `/api/v0/person/` | get index of parlamentarians |
| `POST` | `/api/v0/person/{id}` | get parlamentarian identified by `id`
...

## Starting local docker for testing
```bash
docker-compose -f testing/docker-compose.yml up postgres
```

# License
This project is licensed under the AGPL 3.0. See [LICENSE.md](LICENSE.md)

This project includes parts that are licensed unter the The MIT License (MIT). 
The corresponding source files have an appropriate license header.
