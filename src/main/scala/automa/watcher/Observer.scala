package automa.watcher

import java.net.URLDecoder

import automa.watcher.common.AppConfig
import awscala._
import awscala.dynamodbv2.DynamoDB
import com.amazonaws.services.lambda.runtime.events.S3Event
import com.typesafe.scalalogging.LazyLogging

import scala.collection.JavaConverters._

class Observer extends LazyLogging {

  import Observer._

  def trackS3ObjectCreatedEvent(event: S3Event): Boolean = {
    val record = event.getRecords.asScala.headOption

    record foreach {
      r => {
        val s3Event = r.getS3
        val bucket = decodeS3Key(s3Event.getBucket.getName)
        val key = decodeS3Key(s3Event.getObject.getKey)
        val size = s3Event.getObject.getSizeAsLong
        val created = r.getEventTime.toString
        val file = s"$bucket/$key"

        logger.info(s"Object name: $file")

        getDataSourceByPath(key) foreach {
          ds => {
            //TODO Move to the helper method
            implicit val dynamoDB = DynamoDB().at(Region.US_EAST_1)
            val table = AppConfig.Watcher.table
            dynamoDB.putItem(tableName = table, "Type" -> "watcher", "DataSource" -> ds,
              "created" -> created, "file" -> file, "size" -> size)
          }
        }
      }
    }

    true
  }
}

object Observer extends LazyLogging {
  def decodeS3Key(key: String): String = {
    URLDecoder.decode(key.replace("+", " "), "utf-8")
  }

  def getDataSourceByPath(path: String): Option[String] = {
    AppConfig.Watcher.dataSources.find({ case (a, b) => path.startsWith(b.dataPath) }) map {
      t => t._1
    }
  }
}
