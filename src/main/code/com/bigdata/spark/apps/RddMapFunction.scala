package com.bigdata.spark.apps

import com.bigdata.spark.apps.submit.SparkSubmitUtil
import org.apache.spark.sql.SparkSession

/**
  * @author liulv 
  * @date 2020/1/31
  * @time 0:13
  *
  * Rdd 转换示例：map 参数外部函数使用
  */
//noinspection ScalaDocUnknownTag,ScalaDocParserErrorInspection
object RddMapFunction {

  object MyFunctions {
    def func1(s: String): String = {
      val arr = s.split(" ")
      arr.mkString(",")
    }
  }

  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .master("local")
      .appName("ReadOracle")
      .config("spark.driver.cores", "1")
      //.config("spark.yarn.jars", "D:\\libs\\maven\\com\\oracle\\ojdbc6\\11.2.0.3\\*.jar")
      .getOrCreate()

    SparkSubmitUtil.setJobHistory(spark)

    val sc = spark.sparkContext
    sc.setLogLevel("ERROR")

    val myRdd = sc.textFile("/spark/README.md")

    myRdd.map(MyFunctions.func1).foreach(t => println(t))
  }


}
