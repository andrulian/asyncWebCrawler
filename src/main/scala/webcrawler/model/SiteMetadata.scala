package webcrawler.model

case class SiteMetadata(site: String,
                        title: Option[String],
                        description: Option[String],
                        keywords: Option[String])