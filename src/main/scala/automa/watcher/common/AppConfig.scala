package automa.watcher.common

import com.typesafe.config.{Config, ConfigFactory}
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.ceedubs.ficus.Ficus._

object AppConfig {
  private val config = ConfigFactory.load()
  private val root = config.as[Config]("automa")

  object Watcher {
    case class UpdatePolicy(hours: Int)

    private val maxDuration = 48
    case class DataSource(dataPath: String, policy: UpdatePolicy = UpdatePolicy(hours = maxDuration))

    case class DataSourceValidationScheduler(min: Int = 0, hour: List[Int])

    private val watcher = root.as[Config]("watcher")

    lazy val dataSources = watcher.as[Map[String, DataSource]]("data-sources")

    lazy val table = watcher.as[String]("table")

    lazy val dataSourceValidationScheduler = watcher.as[DataSourceValidationScheduler]("data-sources-validation-scheduler")
  }
}
