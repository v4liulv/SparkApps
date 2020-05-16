package com.bigdata.spark.apps

import com.bigdata.spark.apps.submit.SparkSubmitUtil
import org.apache.spark.sql.SparkSession

/**
  * @author liulv 
  * @date 2020/1/31
  * @time 0:49
  * @description
  * 按照第一个字段对数据去重，若数据重复，则按照最后两个字段保留最大值，
  * 并且第四和第五个字段根据第六和第七字段取相应的值（比如以上数据中第二条数据的第六个字段为3，是最大的一个，
  * 则第五个字段就去第二条数据中的第五个字段。
  * 第三条数据的第七个字段为3，是最大的一个，则第四个字段就去第三条数据中的第四个字段）。

  */
object UniqExample {

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

    val myRdd = sc.textFile("/spark/qc.txt")

    val step1= myRdd.map(log => {
      val valueItems = log.split("\\|", -1)
      val userId = valueItems(0)
      val stbId = valueItems(3)
      val productCode = valueItems(4)
      val createDate = valueItems(2)
      val areaCode = valueItems(1)
      val userType=valueItems(5)
      val stbType=valueItems(6)
      Some(userId, createDate, stbId, areaCode, productCode, userType, stbType)
    }).filter(item => item.isDefined).map(item => item.get)

    val step2 = step1.map(item=>{
      val userId = item._1
      val createDate = item._2
      val stbId = item._3
      val areaCode = item._4
      val productCode = item._5
      val userType=item._6
      val  stbType=item._7
      (userId,(createDate,stbId,areaCode,productCode,userType,stbType))
    }).groupByKey.map(item=> {
      val userId=item._1
      val value=item._2 .toArray
      val createDate =value(0)._1
      var stbId =value(0)._2
      val areaCode =value(0)._3
      var productCode = value(0)._4
      var userType=value(0)._5
      var stbType=value(0)._6

      for(elem <- value){
        if (elem._6 > stbType) {
          stbId=elem._2
          stbType=elem._6
        }
        if (elem._5 > userType) {
          productCode=elem._4
          userType=elem._5
        }
      }
      (userId,createDate,stbId,areaCode,productCode,userType,stbType)
    })

    val step3 = step2.map(item => {
      val userId = item._1
      val createDate = item._2
      val stbId = item._3
      val areaCode = item._4
      val productCode = item._5
      val userType=item._6
      val  stbType=item._7
      s"${userId} |${createDate}|${stbId}|${areaCode}|${productCode}|${userType}|${stbType}"
    }).repartition(1).saveAsTextFile("/spark/qch")
  }
}
