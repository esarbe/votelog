package votelog.persistence.doobie

import cats.effect.Sync
import cats.implicits._
import doobie._
import doobie.implicits._
import votelog.persistence.Schema

class DoobieSchema[F[_]: Sync](transactor: Transactor[F]) extends Schema[F] {

  override def initialize: F[Unit] = {
    val drop =
      sql"""
        drop table if exists ratings;
        drop table if exists ngos;
        drop table if exists permissions;
        drop table if exists users;
         """.update.run


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
          primary key (ngoid, motionid)
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
        createNgoTable *>
        createMotionsScoresTable *>
        createUserTable *>
        createPermissionTable

    script
      .void
      .transact(transactor)
  }
}
