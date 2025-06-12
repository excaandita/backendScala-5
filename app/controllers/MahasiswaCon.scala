package controllers

import models.{MahasiswaData, Mahasiswa}
import play.api.*
import play.api.libs.json.*
import play.api.mvc.*
import play.api.mvc.Results.Ok

import java.time.LocalDate
import javax.inject.*

@Singleton
class MahasiswaCon @Inject() (
  val controllerComponents: ControllerComponents,
  mahasiswaData: MahasiswaData
) extends BaseController {

  import mahasiswaData.mahasiswaFormat

  def addMahasiswa(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val param = request.body.asJson.get // Ini untuk body raw json

//    val param = request.body.asMultipartFormData  // Ini untuk body form-data biasanya kalau form ada gambarnya
//    val param = request.body.asFormUrlEncoded     // Ini untuk body x-www-form-encoded?
//    val param: String = request.getQueryString("name").getOrElse("-")

    val mahasiswaParam = Mahasiswa(
      mahasiswa_id  = -1,
      name          = (param \ "name").asOpt[String].getOrElse(""),
      email         = (param \ "email").asOpt[String],
      telephone     = (param \ "telephone").asOpt[String],
      address       = (param \ "address").asOpt[String],
      start_date    = (param \ "start_date").as[LocalDate]
    )

    val res: (Option[Long], String) = mahasiswaData.postMahasiswa(mahasiswaParam)

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

  def editMahasiswa(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val param = request.body.asJson.getOrElse(Json.obj())

    val mahasiswaParam = Mahasiswa(
      mahasiswa_id  = (param \ "id").asOpt[Long].getOrElse(-1),
      name          = (param \ "name").asOpt[String].getOrElse(""),
      email         = (param \ "email").asOpt[String],
      telephone     = (param \ "telephone").asOpt[String],
      address       = (param \ "address").asOpt[String],
      start_date    = (param \ "start_date").asOpt[LocalDate].getOrElse(LocalDate.now)
    )

    if (mahasiswaParam.mahasiswa_id == -1) {
     BadRequest(Json.obj("message" -> "ID mahasiswa tidak valid"))
    } else {
      val res: (Option[Long], String) = mahasiswaData.updateMahasiswa(mahasiswaParam)

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

  def getDetailMahasiswa(id: Long): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    if (id == -1) {
      BadRequest(Json.obj("message" -> "ID mahasiswa tidak valid"))
    } else {
      val res: Option[Mahasiswa] = mahasiswaData.getMahasiswaById(id)

      res match
        case Some(mhs) =>
          Ok(Json.obj(
            "message" -> "Get Detail Mahasiswa berhasil",
            "data"    -> Json.toJson(mhs)
          ))
        case None => NotFound(Json.obj("message" -> "Data tidak ditemukan"))
    }
  }

  def getListMahasiswa(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>

    val search: Map[String, Any] = Map(
      "name"      -> request.getQueryString("name").getOrElse(""),
      "is_delete" -> request.getQueryString("is_delete").getOrElse("false").toBoolean
    )

    val (listMahasiswa, total) = mahasiswaData.getListMahasiswa(search)

    Ok(Json.obj(
      "message" -> "Berhasil Mendapatkan data",
      "data"    -> Json.toJson[List[Mahasiswa]](listMahasiswa),
      "total"   -> total
    ))
  }
}
