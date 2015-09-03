package automa.watcher

import automa.watcher.common.AppConfig.Watcher.DataSource
import automa.watcher.common.{AppConfig, TownClockEvent}
import awscala._
import awscala.dynamodbv2.{DynamoDB, _}
import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.amazonaws.services.sns.AmazonSNSClient
import com.amazonaws.services.sns.model.PublishRequest
import com.github.nscala_time.time.Imports._
import com.typesafe.scalalogging.LazyLogging
import org.joda.time.{DateTimeConstants, Days, Hours}
import org.json4s._
import org.json4s.native.JsonMethods._

import scala.collection.JavaConverters._

class DataSourcesValidator extends LazyLogging {

  import DataSourcesValidator._

  def validate(event: SNSEvent): Boolean = {
    val record = event.getRecords.asScala.headOption

    record foreach { r =>
      val snsEvent = r.getSNS
      val props = parseOpt(snsEvent.getMessage)

      logger.debug(s"Message: $props")

      if ((props.getOrElse(JNothing) \ "type").values == "chime") {
        runValidationJob(props.get)
      }
    }

    true
  }
}

object DataSourcesValidator extends LazyLogging {

  def runValidationJob(props: JValue): Unit = {
    implicit val formats = DefaultFormats

    val townClockEvent = (props transformField {
      case ("type", x) => ("eventType", x)
    }).extract[TownClockEvent]

    logger.info(s"TownClockEvent: $townClockEvent")

    if (isScheduled(townClockEvent)) {
      val dataSourcesConfigs = AppConfig.Watcher.dataSources
      val dataSourcesState = getDataSourcesState
      validateDataSources(dataSourcesConfigs, dataSourcesState)
    }
  }

  def isScheduled(clockEvent: TownClockEvent): Boolean = {
    val scheduler = AppConfig.Watcher.dataSourceValidationScheduler

    val isHourMatch = scheduler.hour.contains(clockEvent.hour)
    val isMinuteMatch = clockEvent.minute == scheduler.min

    isHourMatch && isMinuteMatch
  }

  def validateDataSources(dataSourcesConfigs: Map[String, DataSource], dataSourcesState: List[DataSourceState]): Unit = {
    val notUpdated = dataSourcesConfigs.map {
      ds => (ds, dataSourcesState.find(state => state.name == ds._1))
    } collect isNotUpdated

    logger.info(s"Validated data sources: ${(dataSourcesConfigs.keys.toList diff notUpdated.toList).mkString("; ")}")

    if (notUpdated.nonEmpty) {
      logger.info("Found non-updated datasources")
      notUpdated.foreach(r => logger.info(r))

      val errorMessage = "Error: " + notUpdated.mkString("; ")
      publishMessage(errorMessage)
    }
  }

  def publishMessage(message: String): Unit = {
    val topicArn = AppConfig.Watcher.notificationTopic
    val snsClient = new AmazonSNSClient()
    val publishRequest = new PublishRequest(topicArn, message)
    snsClient.publish(publishRequest)
  }

  def validateDataSource(ds: (String, DataSource), state: Option[DataSourceState]): Boolean = {
    state exists {
      s =>
        val updated = awscala.DateTime.parse(s.updated)
        isUpdatedInTime(updated, ds._2.policy.hours)
    }
  }

  val isNotUpdated: PartialFunction[((String, DataSource), Option[DataSourceState]), String] = {
    case (ds: (String, DataSource), state: Option[DataSourceState]) if !validateDataSource(ds, state) => ds._1
  }

  def isUpdatedInTime(updated: DateTime, maxInterval: Int): Boolean = {
    val current = awscala.DateTime.now()
    val weekendsHours = weekendsBetween(updated, current) * 24
    val difHours = Hours.hoursBetween(updated, current).getHours - weekendsHours

    difHours < 0 || maxInterval >= difHours
  }

  def weekendsBetween(startDate: DateTime, endDate: DateTime): Int = {
    val daysBetween = Days.daysBetween(startDate, endDate).getDays

    (0 /: (1 until daysBetween)) {
      (r, next) => {
        val day = (startDate + next.days).getDayOfWeek
        if (day == DateTimeConstants.SATURDAY || day == DateTimeConstants.SUNDAY) r + 1 else r
      }
    }
  }

  private def getDataSourcesState = {
    implicit val dynamoDB = DynamoDB().at(Region.US_EAST_1)
    val tableName = AppConfig.Watcher.table
    val table: Table = dynamoDB.table(tableName).get

    val items = table.query(keyConditions = Seq("Type" -> cond.eq("watcher"))).map(item => item.attributes).toList

    items.map(i =>
      DataSourceState(
        name = i.find(_.name == "DataSource").head.value.s.get,
        updated = i.find(_.name == "updated").head.value.s.get,
        file = i.find(_.name == "file").head.value.s.get,
        size = i.find(_.name == "size").head.value.n.getOrElse("0").toInt
      ))
  }
}

