package votelog.client.web.components

import mhtml.{Cancelable, Rx, Var}
import votelog.client.web.components.Paging.Configuration
import votelog.client.web.components.html.StaticSelect
import votelog.client.web.components.html.tools.{setAs, update}
import votelog.domain.authorization.Component
import votelog.domain.crudi.ReadOnlyStoreAlg.QueryParameters.{Offset, PageSize}

import scala.scalajs.js
import scala.xml.Elem

object Paging {
  case class Configuration(
    defaultPageSize: PageSize = PageSize(20),
    pageSizes: Seq[PageSize] = Seq(PageSize(20), PageSize(50), PageSize(100), PageSize(200)),
    defaultOffset: Offset = Offset(0),
  )
}

class Paging(component: Component, configuration: Configuration) {

  val initialPage: Int = 1
  val page: Var[Int] = Var(initialPage)
  val validPage: Rx[Int] = page.foldp(initialPage){ case (acc, curr) => if (curr >= initialPage) curr else acc}

  lazy val pageSizeSelect =
    new StaticSelect[PageSize](
      legend = "page size",
      options = configuration.pageSizes,
      default = configuration.defaultPageSize,
      clazz = "pageSize",
      id = id("pageSize")(component)
    )

  val offset: Rx[Offset] =
    for {
      page <- validPage
      pageSize <- pageSizeSelect.model
    } yield Offset((page - 1) * pageSize.value )

  val pageSize = pageSizeSelect.model
  def incIfGtZero(by: Int)(i: Int): Int = (i + by) max 1

  val view: Elem = {
    <fieldset class="control paging">
      <legend>Paging</legend>
      <dl class="page">
        <dt><label for={ id("page")(component) } >Page</label></dt>
        <dd><button onclick={ update(page)(incIfGtZero(-10)) }>{"<<"}</button></dd>
        <dd><button onclick={ update(page)(incIfGtZero(-1)) }>{"<"}</button></dd>
        <dd>
          <input
            id={ id("page")(component) }
            type="number"
            min="1"
            value={validPage.map(_.toString)}
            onchange={ (e: js.Dynamic) => setAs(page)(_.asInstanceOf[String].toInt)(e)
            }
          > </input>
        </dd>
        <dd><button onclick={update(page)((incIfGtZero(1)))}>{">"}</button></dd>
        <dd><button onclick={update(page)((incIfGtZero(10)))}>{">>"}</button></dd>
      </dl>
      { pageSizeSelect.view }
    </fieldset>
  }
}
