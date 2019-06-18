package votelog.persistence.doobie

import cats.Monad
import cats.effect.Sync
import cats.implicits._
import doobie._
import doobie.implicits._
import votelog.persistence.Schema

class DoobieSchema[F[_]: Sync](transactor: Transactor[F]) extends Schema[F] {

  override def initialize: F[Unit] = {
    val drop =
      sql"""
        drop table if exists votes;
        drop table if exists motions;
        drop table if exists politicians;
        drop table if exists ratings;
        drop table if exists ngos;
        drop table if exists parties;
        drop table if exists permissions;
        drop table if exists users;
         """.update.run

    val createPoliticianTable =
      sql"""
        create table politicians (
          id uuid primary key,
          partyid uuid,
          name varchar not null unique,
          foreign key (partyid) references parties (id)
        )""".update.run

    val createMotionTable =
      sql"""
        create table motions (
          id uuid primary key,
          name varchar not null unique,
          submitter uuid not null,
          foreign key (submitter) references politicians (id)
        )""".update.run

    val createVoteTable =
      sql"""
        create table votes (
          politicianid uuid not null,
          motionid uuid not null,
          votum varchar not null,
          foreign key (politicianid) references politicians (id),
          foreign key (motionid) references motions (id),
          primary key (politicianid, motionid)
        )""".update.run

    val createNgoTable =
      sql"""
        create table ngos (
          id uuid primary key,
          name varchar not null
        )""".update.run

    val createMotionsScoresTable =
      sql"""
        create table motions_scores (
          ngoid uuid not null,
          motionid uuid not null,
          score real not null,
          foreign key (ngoid) references ngos (id),
          foreign key (motionid) references motions (id),
          primary key (ngoid, motionid)
        )
         """.update.run


    val createPartyTable =
      sql"""
        create table parties (
          id uuid primary key,
          name varchar not null
        )
         """.update.run

    val createUserTable =
      sql"""
        create table users (
          id uuid primary key,
          name varchar not null unique,
          email varchar not null unique,
          passwordhash varchar not null
        )
      """.update.run

    val createPermissionTable =
      sql"""
        create table permissions (
          userid uuid not null,
          capability varchar not null,
          component varchar not null,
          foreign key (userid) references users (id) on delete cascade,
          primary key (userid, capability, component)
        )
      """.update.run

    val script =
      drop *>
        createPartyTable *>
        createPoliticianTable *>
        createMotionTable *>
        createVoteTable *>
        createNgoTable *>
        createMotionsScoresTable *>
        createUserTable *>
        createPermissionTable

    script
      .void
      .transact(transactor)
  }
}
