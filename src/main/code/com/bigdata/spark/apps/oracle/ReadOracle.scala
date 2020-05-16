package com.bigdata.spark.apps.oracle

import java.util.Properties

import com.bigdata.spark.apps.submit.SparkSubmitUtil
import com.sinobest.framework.util.PropertiesUtil
import oracle.net.aso.i
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.execution.datasources.jdbc.JDBCOptions
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @author liulv 
  * @date 2020/1/22
  * @time 18:26
  * @description
  */
object ReadOracle {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .master("yarn-client")
      .appName("ReadOracle")
      .config("spark.driver.cores", "1")
      .getOrCreate()

    SparkSubmitUtil.setJobHistory(spark)

    val property = PropertiesUtil.getProperties("spark-oracle.properties")
    val jdbcDF = spark.read.jdbc(property.getProperty(JDBCOptions.JDBC_URL),
      property.getProperty(JDBCOptions.JDBC_TABLE_NAME),
      property)
    jdbcDF.createOrReplaceTempView("records")
    val result = spark.sql("SELECT * FROM records")
    //result.collect().foreach(println)
    result.show()
    //    一些SQL语句
    result.select("ZJHM").show()
    result.filter("ZJHM='520000000000000003'").show()
    result.groupBy("ZJHM").count().show()

    result.collect().foreach(r =>{
      for (i <- 0 until r.size){
        print(r.schema.fields.apply(i).name + ":")
        print(r.get(i).toString)
      }
      println()
    })

  }

}