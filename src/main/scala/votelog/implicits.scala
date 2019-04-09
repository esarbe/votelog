package votelog

object implicits
  extends syntax.IdentifiedSyntax
    with decoders.IdentityDecoders
    with syntax.EncoderSyntax
    with decoders.VotumEncoder