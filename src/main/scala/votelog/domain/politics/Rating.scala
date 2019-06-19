package votelog.domain.politics

import votelog.domain.politics.ScoringStore.Score

case class Rating(ngoId: Ngo.Id, politicianId: Politician.Id, score: Score)
