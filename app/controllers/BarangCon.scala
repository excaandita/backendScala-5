package controllers

import models.{Barang, BarangData}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents, Request}

import javax.inject.{Inject, Singleton}

@Singleton
class BarangCon @Inject() (
   val controllerComponents: ControllerComponents,
   barangData: BarangData
) extends BaseController {

  import barangData.barangFormat

  def addBarang(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val param = request.body.asJson.get // Ini untuk body raw json

    val barangParam = Barang(
      barang_id   = -1,
      name        = (param \ "name").asOpt[String].getOrElse(""),
      kategori_id = (param \ "kategori_id").asOpt[Long],
      harga       = (param \ "harga").asOpt[Double],
      stok        = (param \ "stok").asOpt[Long]
    )

    val res: (Option[Long], String) = barangData.addBarang(barangParam)

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

  def editBarang(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val param = request.body.asJson.getOrElse(Json.obj())

    val barangParam = Barang(
      barang_id   = (param \ "id").asOpt[Long].getOrElse(-1),
      name        = (param \ "name").asOpt[String].getOrElse(""),
      kategori_id = (param \ "kategori_id").asOpt[Long],
      harga       = (param \ "harga").asOpt[Double],
      stok        = (param \ "stok").asOpt[Long]
    )

    if (barangParam.barang_id == -1) {
      BadRequest(Json.obj("message" -> "ID Barang tidak valid"))
    } else {
      val res: (Option[Long], String) = barangData.updateBarang(barangParam)

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

  def getDetailBarang(id: Long): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    if (id == -1) {
      BadRequest(Json.obj("message" -> "ID mahasiswa tidak valid"))
    } else {
      val res: Option[Barang] = barangData.getBarangById(id)

      res match
        case Some(brg) =>
          Ok(Json.obj(
            "message" -> "Get Detail Barang berhasil",
            "data" -> Json.toJson(brg)
          ))
        case None => NotFound(Json.obj("message" -> "Data tidak ditemukan"))
    }
  }

  def getListBarang(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>

    val search: Map[String, Any] = Map(
      "name" -> request.getQueryString("name").getOrElse(""),
      "is_delete" -> request.getQueryString("is_delete").getOrElse("false").toBoolean
    )

    val (listBarang, total) = barangData.getListBarang(search)

    Ok(Json.obj(
      "message" -> "Berhasil Mendapatkan data",
      "data" -> Json.toJson[List[Barang]](listBarang),
      "total" -> total
    ))
  }
}