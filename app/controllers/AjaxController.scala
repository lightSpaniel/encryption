package controllers

import play.api.mvc._
import models._
import play.api.routing._
import javax.inject._
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}

class AjaxController @Inject()() extends Controller{

  def javascriptRoutes = Action { implicit request =>
    Ok(
      JavaScriptReverseRouter("jsRoutes")(
        routes.javascript.AjaxController.searchForm
      )
    ).as("text/javascript")
  }

  val abc = BasicVenture("abc", "hello")
  val xyz = BasicVenture("xyz", "world")
  val efg = BasicVenture("efg", "hello")
  val hij = BasicVenture("xyz", "world")
  val klm = BasicVenture("hij", "hello")
  val nop = BasicVenture("nop", "world")

  val testBasicVentures = Seq(abc, xyz, efg, hij, klm, nop)

  def searchForm = Action {
    Ok(views.html.searchForm("Ventures"))
  }

  def listBasicVentures = Action {
    val justNames = testBasicVentures.map(_.name)
    Ok(Json.toJson(testBasicVentures))
  }

  def updateSearch(string: String) = Action {
    val newList = testBasicVentures.filter(_.name.toLowerCase.contains(string.toLowerCase))
    Ok(Json.toJson(newList))
  }

  //With button get venture page
}






















