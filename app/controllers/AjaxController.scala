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
        routes.javascript.HomeController.searchForm
      )
    ).as("text/javascript")
  }



  //With button get venture page
}
