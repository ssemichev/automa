package automa.watcher.common

import automa.UnitSpec

class AppConfigSpec extends UnitSpec {

  behavior of "AppConfig"

  it should "read list of data sources from the application config" in {
    AppConfig.Watcher.dataSources should not be empty
  }

  it should "assign the default bucket for data source" in {
    val dataSources = AppConfig.Watcher.dataSources
<<<<<<< HEAD
    dataSources("Test01").dataPath should be ("ftp/test01/")
=======
    dataSources("Silverpop").dataPath should be ("ftp/silverpop_partitioned/root/")
>>>>>>> master
  }

  it should "read table property" in {
    AppConfig.Watcher.table should not be empty
  }

  it should "read data-sources-validation-scheduler properties" in {
    val dataSourceValidationScheduler = AppConfig.Watcher.dataSourceValidationScheduler
    dataSourceValidationScheduler.hour should not be empty
    dataSourceValidationScheduler.min should be ("00")
  }

}
