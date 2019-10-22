package votelog.domain.politics

case class Vote(
  motionId: Motion.Id,
  politicianId: Person.Id,
  votum: Votum
)
