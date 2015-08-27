package automa.watcher

import java.net.URLDecoder
import automa.watcher.common.AppConfig
import awscala._
import awscala.dynamodbv2.DynamoDB
import com.amazonaws.services.lambda.runtime.events.S3Event

import scala.collection.JavaConverters._

class Observer {

  import Observer._

  def trackS3ObjectCreatedEvent(event: S3Event): Unit = {
    val record = event.getRecords.asScala.headOption

    record foreach {
      r => {
        val s3Event = r.getS3
        val bucket = decodeS3Key(s3Event.getBucket.getName)
        val key = decodeS3Key(s3Event.getObject.getKey)
        val size = s3Event.getObject.getSizeAsLong
        val created = r.getEventTime.toString
        val file = s"$bucket/$key"

        //TODO Add logging to the console
        println(file)

        getDataSourceByPath(getPath(key)) foreach {
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
  }
}

object Observer {
  def decodeS3Key(key: String): String = {
    URLDecoder.decode(key.replace("+", " "), "utf-8")
  }

  def getDataSourceByPath(path: String): Option[String] = {
    AppConfig.Watcher.dataSources.find({ case (a, b) => b.dataPath.startsWith(path) }) map {
      t => t._1
    }
  }

  def getPath(fullPath: String): String = {
    val sep = fullPath.lastIndexOf('/')
    if (sep == -1) fullPath else fullPath.substring(0, sep)
  }
}
