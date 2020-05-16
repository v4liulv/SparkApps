package com.bigdata.spark.apps

import com.bigdata.spark.apps.submit.SparkSubmitUtil
import org.apache.hadoop.conf.Configuration
import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}
import org.slf4j.LoggerFactory

/**
  * @author liulv
  * @date 2019/8/21
  * @time 4:39
  * @description 本地模式Spark初始化
  */
object SparkUtil {
  private val LOG = LoggerFactory.getLogger(SparkUtil.getClass)

  /**
    * 本地模式-初始化Spark
    *
    * @return 返回SparkSession
    */
  private[spark] def sparkInitLocal(appName : String) : SparkSession = {
    var sparkAppName = appName
    val appNameDefault = "sparkTest"
    if(sparkAppName == null || sparkAppName.trim.equals("")){
      sparkAppName = appNameDefault
      LOG.warn("------初始化Spark默认appName为空，使用默认名:{}", appNameDefault)
    }

    val spark = SparkSession.builder()
      .appName(sparkAppName)
      .master("local[2]")
      .enableHiveSupport()
      .getOrCreate()

    spark
  }

  /**
    * 停掉底层的 `SparkContext`.
    *
    * @param spark SparkSession
    */
  private[spark] def stop(spark: SparkSession) : Unit = {
    if(spark != null)
      spark.stop()
  }

  //设置本地模式
  private val defaultMaster = "local[2]"

  def getConf(appName: String): SparkConf = {
    getConf(appName, defaultMaster)
  }

  def getConf(appName: String, master: String): SparkConf = {
    val sparkConf = new SparkConf
    sparkConf.setAppName(appName)
    sparkConf.setMaster(master)
    val conf = new Configuration
    import scala.collection.JavaConversions._
    for (cMap <- conf) {
      sparkConf.set(cMap.getKey, cMap.getValue)
    }

    SparkSubmitUtil.setJobHistory(sparkConf)
    sparkConf
  }

  def getSC(appName: String, master: String): SparkContext = {
    new SparkContext(getConf(appName, master))
  }

  def getSC(appName: String): SparkContext = getSC(appName, defaultMaster)

  def getSpark(appName: String): SparkSession = getSpark(appName, defaultMaster, false)

  def getSpark(appName: String, enableHiveSupport: Boolean): SparkSession = getSpark(appName, defaultMaster, enableHiveSupport)

  def getSpark(appName: String, master: String, enableHiveSupport: Boolean): SparkSession = {
    val sparkBuilder = SparkSession.builder.master(master).appName(appName)
    import scala.collection.JavaConversions._
    for (cMap <- new Configuration) {
      sparkBuilder.config(cMap.getKey, cMap.getValue)
    }
    SparkSubmitUtil.setJobHistory(sparkBuilder)
    if (enableHiveSupport) sparkBuilder.enableHiveSupport.getOrCreate;
    else sparkBuilder.getOrCreate
  }

}
