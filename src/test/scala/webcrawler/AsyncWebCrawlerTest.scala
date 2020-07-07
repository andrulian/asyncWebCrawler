package webcrawler

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import webcrawler.model.SiteMetadata

class AsyncWebCrawlerTest extends AnyFunSuite with Matchers {

  implicit val browser = JsoupBrowser()

  test("should return fully populated simple meta") {

    val sampleTitle = """Sample Site"""
    val site = "mysite.org"
    val sampleDesc = "sample description"
    val sampleKeywords = "sample keywords"
    val sampleContent =
      s"""
        |<html lang="en">
        |  <head>
        |    <meta charset="utf-8">
        |
        |    <title>$sampleTitle</title>
        |    <meta name="description" content="$sampleDesc">
        |    <meta name="keywords" content="$sampleKeywords">
        |  </head>
        |</html>""".stripMargin

    val expectedMeta = SiteMetadata(site, Some(sampleTitle), Some(sampleDesc), Some(sampleKeywords))
    val actualMeta = AsyncWebCrawler.contentToSiteMetadata(site, sampleContent)

    expectedMeta shouldEqual actualMeta
  }

  test("should return simple meta without title, description, keywords") {

    val site = "mysite.org"

    val sampleContent =
      s"""
         |<html lang="en">
         |  <head>
         |    <meta charset="utf-8">
         |
         |  </head>
         |</html>""".stripMargin

    val expectedMeta = SiteMetadata(site, None, None, None)
    val actualMeta = AsyncWebCrawler.contentToSiteMetadata(site, sampleContent)

    expectedMeta shouldEqual actualMeta
  }

}
