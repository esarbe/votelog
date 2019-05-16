package votelog.persistence.doobie

import cats.implicits._
import doobie._
import doobie.implicits._
import votelog.persistence.Schema

class DoobieSchema extends Schema[ConnectionIO] {
  override def initialize: ConnectionIO[Unit] = {
    val drop =
      sql"""
        drop table if exists politician;
        drop table if exists motion;
        drop table if exists vote;
        drop table if exists rating;
        drop table if exists ngo;
         """.update.run

    val createPoliticianTable =
      sql"""
        create table politician (
          id serial primary key,
          name varchar not null unique
        )""".update.run

    val createMotionTable =
      sql"""
        create table motion (
          id serial primary key,
          name varchar not null unique,
          submitter serial not null,
          foreign key (submitter) references politician (id)
        )""".update.run

    val createVoteTable =
      sql"""
        create table vote (
          politicianid serial not null,
          motionid serial not null,
          votum varchar not null,
          foreign key (politicianid) references politician (id),
          foreign key (motionid) references motion (id),
          primary key (politicianid, motionid)
        )""".update.run

    val createNgoTable =
      sql"""
        create table ngo (
          id serial primary key,
          name varchar not null,
          password varchar not null
        )""".update.run

    val createRatingTable =
      sql"""
        create table rating (
          politicianid serial not null,
          ngoid serial not null,
          value double not null
        )
         """.update.run

    val createPartyTable =
      sql"""
        create table party (
          id serial primary key,
          name varchar not null
        )
         """.update.run

    val createUserTable =
      sql"""
        create table user (
          id serial primary key,
          name varchar not null unique,
          email varchar not null unique,
          hashedPassword varchar not null
        )
      """.update.run

    val createPermissionTable =
      sql"""
        create table permission (
          userid serial not null,
          capability varchar not null,
          component varchar not null,
          foreign key (userid) references user (id),
          primary key (userid, capability, component)
        )
      """.update.run

    val script =
      drop *>
        createPoliticianTable *>
        createMotionTable *>
        createVoteTable *>
        createNgoTable *>
        createRatingTable *>
        createPartyTable *>
        createUserTable *>
        createPermissionTable

    script.map(_ => Unit)
  }
}
