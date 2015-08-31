package automa.watcher

import automa.watcher.common.AppConfig.Watcher.DataSource
import automa.watcher.common.{AppConfig, TownClockEvent}
import awscala._
import awscala.dynamodbv2.{DynamoDB, _}
import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.typesafe.scalalogging.LazyLogging
import org.joda.time.{Hours, DateTimeConstants, Days}
import org.json4s._
import org.json4s.native.JsonMethods._
import com.github.nscala_time.time.Imports._

import scala.collection.JavaConverters._

class DataSourcesValidator extends LazyLogging {
  import DataSourcesValidator._

  def validate(event: SNSEvent): Boolean = {
    val record = event.getRecords.asScala.headOption

    record foreach { r =>
      val snsEvent = r.getSNS
      val props = parseOpt(snsEvent.getMessage)

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

    val isHourMatch = scheduler.hour.exists(h => h.toString == clockEvent.hour)
    val isMinuteMatch = clockEvent.minute == scheduler.min.toString

    val isScheduledresult = isHourMatch && isMinuteMatch
    logger.info(s"TEST isScheduled: $isScheduledresult")

    true
  }

  def validateDataSources(dataSourcesConfigs: Map[String, DataSource], dataSourcesState: List[DataSourceState]): Unit = {
    val results = dataSourcesConfigs.map {
      //TODO change to partial function
      ds => validateDataSource(ds, dataSourcesState.find(state => state.name == ds._1))
    }

    //TODO Send error notifications
    results.foreach( r => logger.info(r) )
  }

  def validateDataSource(ds: (String, DataSource), state: Option[DataSourceState]): String = {
    state map {
      s =>
        val updated = awscala.DateTime.parse(s.updated)
        if (isUpdatedInTime(updated, ds._2.policy.hours)) s"Ok: ${ds._1}" else s"Error: ${ds._1}"
    } getOrElse {
      s"Error: ${ds._1}"
    }
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

    //TODO Rename created to updated
    items.map(i => DataSourceState(
      name = i.find(_.name == "DataSource").head.value.s.get,
      updated = i.find(_.name == "created").head.value.s.get,
      file = i.find(_.name == "file").head.value.s.get,
      size = Some(i.find(_.name == "size").head.value.s.get.toInt).getOrElse(0)
    ))
  }
}

