package votelog.persistence.politician

trait PoliticianTable[F[_]] {
  def inititalize: F[Unit]
}
