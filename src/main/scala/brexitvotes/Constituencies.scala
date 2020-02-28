package brexitvotes

import cats.Applicative
import cats.effect.Sync
import cats.implicits._
import io.circe.{Encoder, Decoder, Json, HCursor}
import io.circe.generic.semiauto._
import org.http4s._
import org.http4s.implicits._
import org.http4s.{EntityDecoder, EntityEncoder, Method, Uri, Request}
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.Method._
import org.http4s.circe._

trait Constituencies[F[_]]{
  def get: F[Constituencies.Constituency]
}

object Constituencies {
  def apply[F[_]](implicit ev: Constituencies[F]): Constituencies[F] = ev

  final case class Constituency(constituency: String) extends AnyVal
  object Constituency {
    implicit val constituencyDecoder: Decoder[Constituency] = deriveDecoder[Constituency]
    implicit def constituencyEntityDecoder[F[_]: Sync]: EntityDecoder[F, Constituency] =
      jsonOf
    implicit val constituencyEncoder: Encoder[Constituency] = deriveEncoder[Constituency]
    implicit def constituencyEntityEncoder[F[_]: Applicative]: EntityEncoder[F, Constituency] =
      jsonEncoderOf
  }

  final case class ConstituencyError(e: Throwable) extends RuntimeException

  def impl[F[_]: Sync](C: Client[F]): Constituencies[F] = new Constituencies[F]{
    val dsl = new Http4sClientDsl[F]{}
    import dsl._
    def get: F[Constituencies.Constituency] = {
      C.expect[Constituency](GET(uri"https://api.postcodes.io/"))
        .adaptError{ case t => ConstituencyError(t)} // Prevent Client Json Decoding Failure Leaking
    }
  }
}