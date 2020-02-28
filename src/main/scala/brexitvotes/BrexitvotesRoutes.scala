package brexitvotes

import cats.effect.Sync
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

object ConstituenciesRoutes {

  def constituenciesRoutes[F[_]: Sync](C: Constituencies[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "postcodes" =>
        for {
          postcode <- C.get
          resp <- Ok(postcode)
        } yield resp
    }
  }
}