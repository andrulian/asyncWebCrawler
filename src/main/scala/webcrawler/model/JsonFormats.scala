package webcrawler.model

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

object JsonFormats extends SprayJsonSupport with DefaultJsonProtocol{
  implicit val siteMetadataFormat: RootJsonFormat[SiteMetadata] = jsonFormat4(SiteMetadata)
  implicit val requestErrorFormat: RootJsonFormat[RequestError] = jsonFormat2(RequestError)
}
