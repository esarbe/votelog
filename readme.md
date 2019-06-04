[![Build Status](https://travis-ci.com/esarbe/votelog.svg?token=p1fS6synApAkBBqLu9y3&branch=master)](https://travis-ci.com/esarbe/votelog)

# Votelog


**`votelog`** is a http server application to record, query and rate the voting behaviour of politicians. 

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
The `votlog` http server application configuration is located at `src/main/resources/application.conf`. It
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