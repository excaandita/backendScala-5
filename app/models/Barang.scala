package models


import anorm.*
import anorm.SqlParser.{get, scalar}
import anorm.{SQL, Sql}
import org.postgresql.util.PSQLException
import play.api.db.DBApi
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate
import javax.inject.Inject

case class Barang(
   barang_id: Long,
   name: String,
   kategori_id: Option[Long],
   harga: Option[Double],
   stok: Option[Long],
   is_delete: Boolean = false,
   kategori: Option[Kategori] = None
)

class BarangData @Inject()(
  DBApi: DBApi,
  kategoriData: KategoriData
) {

  private val db = DBApi.database("default")

  val barangParser: RowParser[Barang] =
    (get[Long]("barang_id") ~
      get[String]("name") ~
      get[Option[Long]]("kategori_id") ~
      get[Option[Double]]("harga") ~
      get[Option[Long]]("stok") ~
      get[Boolean]("is_delete") ~
      kategoriData.kategoriParser.?).map {
      case barang_id ~ name ~ kategori_id ~ harga ~ stok ~ is_delete ~ kategori =>
        Barang(barang_id, name, kategori_id, harga, stok, is_delete, kategori)
    }

  import kategoriData.kategoriFormat
  implicit val barangFormat: OFormat[Barang] = Json.format[Barang]

  def addBarang(barangMasuk: Barang): (Option[Long], String) = db.withConnection { implicit c =>

    var id: Option[Long] = None
    var message = ""

    val sqlQuery =
      s"""
         |INSERT INTO barang (name, kategori_id, harga, stok)
         |VALUES ({name}, {kategori_id}, {harga}, {stok}) """.stripMargin

    try {
      id = SQL(sqlQuery).on(
        "name"        -> barangMasuk.name,
        "kategori_id" -> barangMasuk.kategori_id,
        "harga"       -> barangMasuk.harga.getOrElse(0.0),
        "stok"        -> barangMasuk.stok.getOrElse(0L)
      ).executeInsert(SqlParser.scalar[Long].singleOpt)

    } catch {
      case e: PSQLException =>
        message = e.toString
        id = None
    }

    (id, message)
  }

  def updateBarang(barang: Barang): (Option[Long], String) = db.withConnection { implicit c =>
    var updatedRows: Option[Long] = None
    var message = ""

    val sqlStatement =
      s""" UPDATE barang
         | SET name = {name},
         |     kategori_id = {kategori_id},
         |     harga = {harga},
         |     stok = {stok},
         |     updated_at = NOW()
         | WHERE barang_id = {id}
       """.stripMargin

    try {
      val count = SQL(sqlStatement).on(
        "name"        -> barang.name,
        "kategori_id" -> barang.kategori_id,
        "harga"       -> barang.harga,
        "stok"        -> barang.stok,
        "id"          -> barang.barang_id
      ).executeUpdate()

      if (count > 0) {
        updatedRows = Some(barang.barang_id)
        message = s"Berhasil memperbarui data Barang dengan ID ${barang.barang_id}"
      } else {
        message = s"Tidak ada data yang diperbarui untuk ID ${barang.barang_id}"
      }
    } catch {
      case e: PSQLException =>
        message = e.toString
        updatedRows = None
    }

    (updatedRows, message)
  }

  def getBarangById(id: Long): Option[Barang] = db.withConnection { implicit c =>
    val sqlQuery =
      s""" SELECT b.*, k.*
         |FROM barang b
         |LEFT JOIN kategori k ON (k.kategori_id = b.kategori_id)
         |WHERE b.barang_id = {id}
         |""".stripMargin

    SQL(sqlQuery).on("id" -> id).as[Option[Barang]](barangParser.singleOpt)
  }

  def getListBarang(search: Map[String, Any]): (List[Barang], Long) = db.withConnection { implicit c =>
    var where = s""
    val sqlQuery: String = s"""SELECT * FROM barang WHERE TRUE """

    if (search.contains("is_delete") && search("is_delete").toString.nonEmpty) {
      where += s"AND is_delete = '${search("is_delete")}' "
    } else {
      where += s"AND is_delete = false "
    }

    if (search.contains("name") && search("name").toString.nonEmpty) {
      where += s"AND name ILIKE '%${search("name")}%' "
    }

    val sqlCount = """SELECT COUNT(*) FROM barang WHERE TRUE """.stripMargin
    val total = SQL(sqlCount + where).as(scalar[Long].single)

    val list = SQL(sqlQuery + where).as(barangParser.*)

    (list, total)

  }

}
