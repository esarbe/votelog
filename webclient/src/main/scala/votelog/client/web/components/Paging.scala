package votelog.client.web.components

import mhtml.{Rx, Var}
import votelog.client.web.components.Paging.Configuration
import votelog.client.web.components.html.StaticSelect
import votelog.client.web.components.html.tools.{set, update}
import votelog.domain.authorization.Component
import votelog.domain.crudi.ReadOnlyStoreAlg.QueryParameters.{Offset, PageSize}

import scala.xml.Elem

object Paging {
  case class Configuration(
    defaultPageSize: PageSize = PageSize(20),
    pageSizes: Seq[PageSize] = Seq(PageSize(20), PageSize(50), PageSize(100), PageSize(200)),
    defaultOffset: Offset = Offset(0),
  )

}

trait Paging {
  implicit val component: Component
  val configuration: Configuration = Configuration()
  val pageSize: Var[PageSize] = Var(configuration.defaultPageSize)
  val initialPage: Int = 0
  val page: Var[Int] = Var(initialPage)
  val offset: Rx[Offset] =
    for {
      page <- page.keepIf(_ > 0)(initialPage)
      pageSize <- pageSize
    } yield Offset((page - 1) * pageSize.value )

  val pageSizeSelect =
    new StaticSelect[PageSize](
      legend = "page size",
      options = configuration.pageSizes,
      default = configuration.defaultPageSize,
      clazz = "pageSize",
      id = id("pageSize")
    )

  val view: Elem = {
    <fieldset>
      <dl class="page">
        <dt><label for={ id("page")} >Offset</label></dt>
        <dd><button onclick={update(page)(_ - 10)}>{"<<"}</button></dd>
        <dd><button onclick={update(page)(_ - 1)}>{"<"}</button></dd>
        <dd>
          <input
            id={ id("page") }
            type="number"
            value={page.map(_.toString)}
            onchange={set(page)}
          > </input>
        </dd>
        <dd><button onclick={update(page)(_ + 1)}>{">"}</button></dd>
        <dd><button onclick={update(page)(_ + 10)}>{">>"}</button></dd>
      </dl>
      { pageSizeSelect.view }
    </fieldset>
  }
}
