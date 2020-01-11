package votelog.domain.politics

case class Vote(
  motionId: Business.Id,
  politicianId: Person.Id,
  votum: Votum
)
