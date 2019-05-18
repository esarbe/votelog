package votelog.domain.politics

case class Vote(
  motionId: Motion.Id,
  politicianId: Politician.Id,
  votum: Votum
)
