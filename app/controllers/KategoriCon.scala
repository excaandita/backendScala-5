package controllers

import models.{Kategori, KategoriData}
import play.api.*
import play.api.libs.json.*
import play.api.mvc.*
import play.api.mvc.Results.Ok

import javax.inject.*

@Singleton
class KategoriCon @Inject()(
  val controllerComponents: ControllerComponents,
  kategoriData: KategoriData
) extends BaseController {

  import kategoriData.kategoriFormat

  def addKategori(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val param = request.body.asJson.get // Ini untuk body raw json

    val kategoriParam = Kategori(
      kategori_id  = -1,
      name         = (param \ "name").asOpt[String].getOrElse("")
    )

    val res: (Option[Long], String) = kategoriData.addKategori(kategoriParam)

    res match
      case (Some(id), msg) =>
        Ok(Json.obj(
          "message" -> msg,
          "data" -> Json.obj(
            "id" -> id
          )
        ))

      case (None, msg) =>
        BadRequest(Json.obj(
          "message" -> msg
        ))

  }

  def editKategori(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val param = request.body.asJson.getOrElse(Json.obj())

    val kategoriParam = Kategori(
      kategori_id  = (param \ "id").asOpt[Long].getOrElse(-1),
      name         = (param \ "name").asOpt[String].getOrElse("")
    )

    if (kategoriParam.kategori_id == -1) {
     BadRequest(Json.obj("message" -> "ID Kategori tidak valid"))
    } else {
      val res: (Option[Long], String) = kategoriData.updateKategori(kategoriParam)

      res match {
        case (Some(id), msg) =>
          Ok(Json.obj(
            "message" -> msg,
            "data" -> Json.obj("id" -> id)
          ))

        case (None, msg) =>
          BadRequest(Json.obj("message" -> msg))
      }
    }
  }

  def getDetailKategori(id: Long): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    if (id == -1) {
      BadRequest(Json.obj("message" -> "ID Kategori tidak valid"))
    } else {
      val res: Option[Kategori] = kategoriData.getKategoriById(id)

      res match
        case Some(ktg) =>
          Ok(Json.obj(
            "message" -> "Get Detail Kategori berhasil",
            "data"    -> Json.toJson(ktg)
          ))
        case None => NotFound(Json.obj("message" -> "Data tidak ditemukan"))
    }
  }

  def getListKategori(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>

    val search: Map[String, Any] = Map(
      "name"      -> request.getQueryString("name").getOrElse(""),
      "is_delete" -> request.getQueryString("is_delete").getOrElse("false").toBoolean
    )

    val (listKategori, total) = kategoriData.getListKategori(search)

    Ok(Json.obj(
      "message" -> "Berhasil Mendapatkan data",
      "data"    -> Json.toJson[List[Kategori]](listKategori),
      "total"   -> total
    ))
  }
}
