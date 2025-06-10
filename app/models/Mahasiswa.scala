package models

import anorm.*
import anorm.SqlParser.get
import org.postgresql.util.PSQLException
import play.api.db.DBApi
import play.api.libs.json.{Json, OFormat, Reads}

import java.time.{LocalDate, LocalDateTime}
import javax.inject.*

case class Mahasiswa(
  mahasiswa_id: Long,
  name: String,
  email: Option[String],
  telephone: Option[String],
  address: Option[String],
  start_date: LocalDate
)

case class Mahasiswa1(
  mahasiswa_id: Long,
  name: String,
  email: Option[String],
  telephone: Option[String],
  address: Option[String],
  start_date: LocalDate
)

@Singleton
class MahasiswaData @Inject() ( DBApi: DBApi ) {

  val mahasiswaParser: RowParser[Mahasiswa] =
    (get[Long]("mahasiswa_id") ~
      get[String]("name") ~
      get[Option[String]]("email") ~
      get[Option[String]]("telephone") ~
      get[Option[String]]("address") ~
      get[LocalDate]("start_date")).map {
      case mahasiswa_id ~ name ~ email ~ telephone ~ address ~ start_date =>
        Mahasiswa(mahasiswa_id, name, email, telephone, address, start_date)
    }

//  implicit val mahasiswaFormat: OFormat[Mahasiswa] = Json.format[Mahasiswa]
  implicit val mahasiswaFormat: Reads[Mahasiswa] = Json.reads[Mahasiswa]

  private val db = DBApi.database("default")

  def postMahasiswa(mahasiswa: Mahasiswa): (Option[Long], String) = db.withConnection { implicit c =>
    var id: Option[Long] = None
    var message = ""

    val sqlStatement =
        s""" INSERT INTO mahasiswa (name, email, telephone, address, start_date)
         | VALUES ({name}, {email}, {telephone}, {address}, {start_date}) """.stripMargin

    try {
      id = SQL(sqlStatement).on(
        "name"        -> mahasiswa.name,
        "email"       -> mahasiswa.email,
        "telephone"   -> mahasiswa.telephone,
        "address"     -> mahasiswa.address,
        "start_date"  -> mahasiswa.start_date,
      ).executeInsert(SqlParser.scalar[Long].singleOpt)

      message = "Berhasil Menambahkan Data"
    } catch {
      case e: PSQLException =>
        message = e.toString
        id = None
    }

    (id, message)
  }

  def updateMahasiswa(mahasiswa: Mahasiswa): (Option[Long], String) = db.withConnection { implicit c =>
    var updatedRows: Option[Long] = None
    var message = ""

    val sqlStatement =
      s""" UPDATE mahasiswa
         | SET name = {name},
         |     email = {email},
         |     telephone = {telephone},
         |     address = {address},
         |     start_date = {start_date},
         |     updated_at = NOW()
         | WHERE mahasiswa_id = {id}
     """.stripMargin

    try {
      val count = SQL(sqlStatement).on(
        "name"       -> mahasiswa.name,
        "email"      -> mahasiswa.email,
        "telephone"  -> mahasiswa.telephone,
        "address"    -> mahasiswa.address,
        "start_date" -> mahasiswa.start_date,
        "id"         -> mahasiswa.mahasiswa_id
      ).executeUpdate()

      if (count > 0) {
        updatedRows = Some(mahasiswa.mahasiswa_id)
        message = s"Berhasil memperbarui data Mahasiswa dengan ID ${mahasiswa.mahasiswa_id}"
      } else {
        message = s"Tidak ada data yang diperbarui untuk ID ${mahasiswa.mahasiswa_id}"
      }
    } catch {
      case e: PSQLException =>
        message = e.toString
        updatedRows = None
    }

    (updatedRows, message)
  }

  def getMahasiswaById(id: Long): Option[Mahasiswa] = db.withConnection { implicit c =>
    val sqlQuery =
      s""" SELECT *
         |FROM mahasiswa
         |WHERE mahasiswa_id = {id}
         |""".stripMargin

    SQL(sqlQuery).on("id" -> id).as[Option[Mahasiswa]](mahasiswaParser.singleOpt)
  }

}