package models

import anorm.*
import anorm.SqlParser.{get, scalar}
import anorm.{SQL, Sql}
import org.postgresql.util.PSQLException
import play.api.db.DBApi
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate
import javax.inject.Inject

case class Kategori(
   kategori_id: Long,
   name: String,
   is_delete: Boolean = false,
)

class KategoriData @Inject()(
  DBApi: DBApi
) {

  implicit val kategoriFormat: OFormat[Kategori] = Json.format[Kategori]

  val kategoriParser: RowParser[Kategori] =
    (get[Long]("kategori_id") ~
      get[String]("name") ~
      get[Boolean]("is_delete")).map {
      case kategori_id ~ name ~ is_delete =>
        Kategori(kategori_id, name, is_delete)
    }

  private val db = DBApi.database("default")

  def addKategori(kategori: Kategori): (Option[Long], String) = db.withConnection { implicit c =>

    var id: Option[Long] = None
    var message = ""

    val sqlQuery =
      s"""
         |INSERT INTO kategori (name)
         |VALUES ({name}) """.stripMargin

    try {
      id = SQL(sqlQuery).on(
        "name"        -> kategori.name
      ).executeInsert(SqlParser.scalar[Long].singleOpt)

    } catch {
      case e: PSQLException =>
        message = e.toString
        id = None
    }

    (id, message)
  }

  def updateKategori(kategori: Kategori): (Option[Long], String) = db.withConnection { implicit c =>
    var updatedRows: Option[Long] = None
    var message = ""

    val sqlStatement =
      s""" UPDATE kategori
         | SET name = {name},
         |     updated_at = NOW()
         | WHERE kategori_id = {id}
       """.stripMargin

    try {
      val count = SQL(sqlStatement).on(
        "name"        -> kategori.name,
        "id"          -> kategori.kategori_id
      ).executeUpdate()

      if (count > 0) {
        updatedRows = Some(kategori.kategori_id)
        message = s"Berhasil memperbarui data Kategori dengan ID ${kategori.kategori_id}"
      } else {
        message = s"Tidak ada data yang diperbarui untuk ID ${kategori.kategori_id}"
      }
    } catch {
      case e: PSQLException =>
        message = e.toString
        updatedRows = None
    }

    (updatedRows, message)
  }

  def getKategoriById(id: Long): Option[Kategori] = db.withConnection { implicit c =>
    val sqlQuery =
      s""" SELECT *
         |FROM kategori
         |WHERE kategori_id = {id}
         |""".stripMargin

    SQL(sqlQuery).on("id" -> id).as[Option[Kategori]](kategoriParser.singleOpt)
  }

  def getListKategori(search: Map[String, Any]): (List[Kategori], Long) = db.withConnection { implicit c =>
    var where = s""
    val sqlQuery: String = s"""SELECT * FROM kategori WHERE TRUE """

    if (search.contains("is_delete") && search("is_delete").toString.nonEmpty) {
      where += s"AND is_delete = '${search("is_delete")}' "
    } else {
      where += s"AND is_delete = false "
    }

    if (search.contains("name") && search("name").toString.nonEmpty) {
      where += s"AND name ILIKE '%${search("name")}%' "
    }

    val sqlCount = """SELECT COUNT(*) FROM kategori WHERE TRUE """.stripMargin
    val total = SQL(sqlCount + where).as(scalar[Long].single)

    val list = SQL(sqlQuery + where).as(kategoriParser.*)

    (list, total)

  }

}
