package automa.watcher

import automa.UnitSpec
import automa.watcher.common.TownClockEvent
import com.github.nscala_time.time.Imports._



class DataSourcesValidatorSpec extends UnitSpec {
  import DataSourcesValidator._

  behavior of "DataSourcesValidatorObject"

  it should "calculate isScheduled property" in {
//    isScheduled(TownClockEvent(hour = "14", minute = "30")) should be (false)
    isScheduled(TownClockEvent(hour = "10", minute = "0")) should be (true)
  }

  it should "calculate how many weekends between two dates" in {
    val zeroDays = 0
    val oneDay = 1
    val twoDays = 2
    val fourDays = 4

    weekendsBetween(new DateTime("2015-08-20T18:09:13.254Z"), new DateTime("2015-08-29T18:09:13.254Z")) should be (twoDays)
    weekendsBetween(new DateTime("2015-08-21T18:09:13.254Z"), new DateTime("2015-08-29T18:09:13.254Z")) should be (twoDays)
    weekendsBetween(new DateTime("2015-08-22T18:09:13.254Z"), new DateTime("2015-08-29T18:09:13.254Z")) should be (oneDay)
    weekendsBetween(new DateTime("2015-08-23T18:09:13.254Z"), new DateTime("2015-08-29T18:09:13.254Z")) should be (zeroDays)
    weekendsBetween(new DateTime("2015-08-12T18:09:13.254Z"), new DateTime("2015-08-28T18:09:13.254Z")) should be (fourDays)
    weekendsBetween(new DateTime("2015-08-12T18:09:13.254Z"), new DateTime("2014-08-28T18:09:13.254Z")) should be (zeroDays)
  }

  it should "calculate isUpdatedInTime" in {
    val maxIntervalDuration = 48
    isUpdatedInTime(awscala.DateTime.now() - 24.hours, maxIntervalDuration) should be (true)
    isUpdatedInTime(awscala.DateTime.now() - 48.hours, maxIntervalDuration) should be (true)

    //Subtract weekend hours for this test
    isUpdatedInTime(awscala.DateTime.now() - 49.hours - 48.hours, maxIntervalDuration) should be (false)
  }
}

