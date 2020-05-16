package com.bigdata.spark.apps.sql.hive

import com.bigdata.spark.apps.SparkUtil

/**
  * @author liulv
  * @date 2020/1/22
  * @time 14:19
  * @description
  */
object ReadHive {
  def main(args: Array[String]): Unit = {

    val spark = SparkUtil.getSpark("Spark SQL By Hive", enableHiveSupport = true)

    spark.sql("select * from hz_bzk.ods_crj_crjjl_text ").show()

    val count = spark.sql("SELECT COUNT(*) FROM hz_bzk.ods_crj_crjjl_text").collect().head.getLong(0)
    println(s"COUNT(*): $count")

    spark.stop()
  }
}
