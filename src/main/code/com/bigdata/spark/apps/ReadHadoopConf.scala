package com.bigdata.spark.apps

import com.bigdata.spark.apps.submit.SparkSubmitUtil
import org.apache.hadoop.conf.Configuration
import org.apache.spark.sql.SparkSession

/**
  * @author liulv 
  * @date 2020/2/1
  * @time 14:15
  * @description
  */
object ReadHadoopConf {

  def main(args: Array[String]): Unit = {
    val sparkBuilder = SparkSession
      .builder
      .master("yarn")
      .appName("Spk Pi")

    val conf = new Configuration()
    import scala.collection.JavaConverters._
    for (c <- conf.iterator().asScala){
      sparkBuilder.config(c.getKey, c.getValue)
    }

    val spark = sparkBuilder.enableHiveSupport().getOrCreate()

    SparkSubmitUtil.setJobHistory(spark)
  }

}
