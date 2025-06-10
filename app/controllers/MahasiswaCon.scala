package controllers

import models.*
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

//  import mahasiswaData.mahasiswaFormat

//  implicit val mahasiswaFormat: Writes[Mahasiswa] = Json.writes[Mahasiswa]


  def addMahasiswa(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val param = request.body.asJson.get // Ini untuk body raw json

//    val param = request.body.asMultipartFormData  // Ini untuk body form-data biasanya kalau form ada gambarnya
//    val param = request.body.asFormUrlEncoded     // Ini untuk body x-www-form-encoded
//    val param = request.getQueryString("name").getOrElse("-")

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
      println(res)
      println(res.get.mahasiswa_id)
      println(Json.toJson[Mahasiswa](res.get)(Json.writes[Mahasiswa]))

//      Ok("")
      res match
        case Some(mhs) =>
          Ok(Json.obj(
            "message" -> "Get Detail Mahasiswa berhasil",
//            "data"    -> Json.toJson[Mahasiswa](mhs)(Json.writes[Mahasiswa])
          ))
        case None => NotFound(Json.obj("message" -> "Data tidak ditemukan"))
    }

  }
}
