package votelog.persistence.doobie


import cats._
import cats.implicits._
import doobie._
import doobie.implicits._
import votelog.persistence.Schema

class DoobieSchema extends Schema[ConnectionIO] {
  override def initialize: ConnectionIO[Unit] = {
    val drop =
      sql"""
            drop table if exists politician
        """.update.run

    val createPoliticianTable =
      sql"""
        create table politician (
          id   serial auto_increment,
          name varchar not null unique
        )""".update.run

    val createMotionTable =
      sql"""
        create table motion (
          id serial auto_increment,
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
          unique (politicianid, motionid)
        )""".update.run

    drop *>
      createPoliticianTable *>
      createMotionTable *>
      createVoteTable.map(_ => Unit)
  }
}
