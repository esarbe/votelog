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
          id   serial primary key,
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
          foreign key (motionid) references motion (id)

        )""".update.run
"-- unique (politicianid, motionid)"

    def insert(n: String, id: Long) =
      sql"insert into politician (name, id) values ($n, $id)".update

    val insertVote =
      List(
        insert("qux", 1).run,
        sql"insert into motion (id, name, submitter) values (1, 'eat the rich', 1);".update.run,
        sql"insert into vote (politicianid, motionid, votum) values (1,1, 'yes');".update.run,
    ).sequence



    drop *>
      createPoliticianTable *>
      createMotionTable *>
      createVoteTable *>
      insertVote.map(_ => Unit)
  }
}
