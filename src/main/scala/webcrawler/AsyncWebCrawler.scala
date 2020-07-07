package webcrawler

import java.io.{BufferedWriter, FileWriter}
import java.net.ConnectException

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}
import spray.json._
import webcrawler.model.{RequestError, SiteMetadata}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object AsyncWebCrawler {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher
    implicit val browser = JsoupBrowser()

    // TODO: read asynchronously using akka streams
    val fileSource = scala.io.Source.fromFile("example.txt")

    val source: Source[Either[RequestError, SiteMetadata], NotUsed] =
      Source
        .fromIterator(() => fileSource.getLines())
        .mapAsync(1)(parseSitePage)


    import webcrawler.model.JsonFormats._

    val metaWriter = new BufferedWriter(new FileWriter("metadata.json", true))
    val errorsWriter = new BufferedWriter(new FileWriter("errors.json", true))

    val result = source.runForeach {
      case Right(meta) =>
        appendDataToFile(metaWriter, meta)
      case Left(error) =>
        appendDataToFile(errorsWriter, error)
    }

    result.onComplete(_ => {
      metaWriter.close()
      errorsWriter.close()
      system.terminate()
    })

  }

  def appendDataToFile[T](bw: BufferedWriter, data: T)(implicit writer: JsonFormat[T]): Unit = {
    bw.write(s"${data.toJson}")
    bw.newLine()
    bw.flush()
  }

  def parseSitePage(uri: String)
                   (implicit ec: ExecutionContext,
                    mat: ActorMaterializer,
                    ac: ActorSystem,
                    browser: Browser): Future[Either[RequestError, SiteMetadata]] = {
    getContentAsString(uri)
      .recoverWith(_ => getContentAsString(uri, withHttps = true))
      .map(content => Right(contentToSiteMetadata(uri, content)))
      .recover {
        case _: ConnectException =>
          Left(RequestError(uri, "Connection timeout"))
        case ex =>
          Left(RequestError(uri, ex.getMessage))
      }
  }

  def getContentAsString(uri: String, withHttps: Boolean = false)
                        (implicit ec: ExecutionContext, mat: ActorMaterializer, ac: ActorSystem): Future[String] = {

    val absoluteUri = s"${if (withHttps) "https" else "http"}://$uri"

    for {
      request <- Future.fromTry(Try(HttpRequest(uri = absoluteUri)))
      response <- Http().singleRequest(request)
      s <- response.entity.dataBytes.runFold(ByteString(""))(_ ++ _)
    } yield s.utf8String
  }

  def contentToSiteMetadata(site: String, content: String)(implicit browser: Browser): SiteMetadata = {
    import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
    import net.ruippeixotog.scalascraper.dsl.DSL._

    val title = browser.parseString(content) >?> text("title")
    val description =
      browser.parseString(content) >?> attr("content")("meta[name=description]")
    val keywords = browser.parseString(content) >?> attr("content")("meta[name=keywords]")

    SiteMetadata(site, title, description, keywords)
  }
}
