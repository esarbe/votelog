package votelog

object implicits
  extends syntax.IdentifiedSyntax
    with encoders.IdentityEncoders
    with syntax.EncoderSyntax
    with encoders.VotumEncoder