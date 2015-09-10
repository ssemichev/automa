package automa.watcher

import automa.UnitSpec
import automa.watcher.common.AppConfig.Watcher.DataSource
import automa.watcher.common.TownClockEvent
import com.github.nscala_time.time.Imports._

class DataSourcesValidatorSpec extends UnitSpec {
  import DataSourcesValidator._

  behavior of "DataSourcesValidatorObject"

  it should "calculate isScheduled property" in {
    isScheduled(TownClockEvent(hour = "14", minute = "30")) should be (false)
    isScheduled(TownClockEvent(hour = "14", minute = "00")) should be (true)
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
    val now = awscala.DateTime.now()
    isUpdatedInTime(now, now - 24.hours, maxIntervalDuration) should be (true)
    isUpdatedInTime(now, now - 48.hours, maxIntervalDuration) should be (true)

    //Subtract weekend hours for this test
    isUpdatedInTime(now, now - 49.hours - 48.hours, maxIntervalDuration) should be (false)
  }

  it should "validate datasource" in {
    val dsName = "TestDS"
    val ds = DataSource(dsName)

    val isValid = isDataSourceValid((dsName, ds), state = Some(DataSourceState(dsName, "2015-09-03T17:45:12.350Z", "file.txt", 1)))
    isValid should be (false)
  }
}

