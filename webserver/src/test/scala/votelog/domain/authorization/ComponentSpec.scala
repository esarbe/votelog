package votelog.domain.authorization

import org.scalatest.{Matchers, WordSpec}

class ComponentSpec extends WordSpec with Matchers {

  "Component" should {

    "contains" in {
      Component("a.b.c.").contains(Component("c")) shouldBe false
      Component("c").contains(Component("a.b.c")) shouldBe false
      Component("a").contains(Component("a.b.c")) shouldBe true
      Component("a.b.c").contains(Component("a.b.c.d")) shouldBe true
      Component("ab.c").contains(Component("a")) shouldBe false
      Component("a.b").contains(Component("a.c.b")) shouldBe false
      Component("a.b").contains(Component("a.b.c.d")) shouldBe true
      Component(".").contains(Component(".a")) shouldBe true
      Component(".").contains(Component(".foo.bar")) shouldBe true
      Component("").contains(Component(".foo.bar")) shouldBe true
    }

    "name" in {
      Component("a.b.c").name shouldBe "c"
      Component("").name shouldBe ""
      Component(".").name shouldBe ""
    }
  }


}
