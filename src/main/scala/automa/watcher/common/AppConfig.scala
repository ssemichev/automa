package automa.watcher.common

import com.typesafe.config.{Config, ConfigFactory}
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.ceedubs.ficus.Ficus._

object AppConfig {
  private val config = ConfigFactory.load()
  private val root = config.as[Config]("automa")

  object Watcher {
    case class DataSource(dataPath: String, policy: String = "")

    private val watcher = root.as[Config]("watcher")

    lazy val dataSources = watcher.as[Map[String, DataSource]]("data-sources")

    lazy val table = watcher.as[String]("table")
  }
}
