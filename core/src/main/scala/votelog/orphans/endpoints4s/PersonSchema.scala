package votelog.orphans.endpoints4s

import java.time.format.DateTimeFormatter

import endpoints4s.algebra.JsonSchemas
import votelog.domain.crudi.ReadOnlyStoreAlg.Index
import votelog.domain.politics.{Canton, Person}
import votelog.domain.politics.Person.Name

trait PersonSchema {
  self: JsonSchemas =>

  implicit def indexSchema[T: JsonSchema]: JsonSchema[Index[T]] =
    (field[Int]("totalEntities") zip
      field[List[T]]("entities")).xmap((Index.apply[T] _).tupled)(index => (index.totalEntities, index.entities))

  implicit val personIdSchema: JsonSchema[Person.Id] = intJsonSchema.xmap(Person.Id)(_.value)
  implicit val nameSchema: JsonSchema[Name] = defaultStringJsonSchema.xmap(Name.apply)(_.value)
  implicit val cantonSchema: JsonSchema[Canton] = defaultStringJsonSchema.xmap(Canton)(_.value)

  implicit val javaTimeLocalDateSchema: JsonSchema[java.time.LocalDate] =
    defaultStringJsonSchema
      .xmap(java.time.LocalDate.parse(_, DateTimeFormatter.ISO_DATE))(DateTimeFormatter.ISO_DATE.format)

  implicit val personGenderEnumSchema: JsonSchema[Person.Gender] =
    stringEnumeration[Person.Gender](Person.Gender.values.toSeq){
      case Person.Gender.Female => "female"
      case Person.Gender.Male => "male"
    }

  val personSchema: JsonSchema[Person] =
    (field[Person.Id]("id") zip
      field[Name]("firstname") zip
      field[Name]("lastname") zip
      field[Canton]("canton") zip
      field[Person.Gender]("gender") zip
      field[String]("party") zip
      optField[java.time.LocalDate]("dateOfElection") zip
      optField[java.time.LocalDate]("dateOfBirth")
    ).xmap[Person]((Person.apply _).tupled)(p => (p.id, p.firstName, p.lastName, p.canton, p.gender, p.party, p.dateOfElection, p.dateOfBirth))

}
