package votelog

object implicits
  extends encoders.IdentityEncoders
    with syntax.EncoderSyntax
    with encoders.VotumEncoder