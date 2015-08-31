package automa.watcher.common

case class TownClockEvent(
                           eventType: String = "",
                           timestamp: String = "",
                           year: String = "",
                           month: String = "",
                           day: String = "",
                           hour: String,
                           minute: String,
                           day_of_week: String = "",
                           unique_id: String = "",
                           region: String = "",
                           sns_topic_arn: String = "")
