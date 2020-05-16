package com.bigdata.spark.apps.sql.dataframe

import com.bigdata.spark.apps.SparkUtil
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{StringType, StructField, StructType}

/**
  * Created by Administrator on 2016/11/22 0022.
  *
  *   RDD转换为DataFrame
  *   使用Schema方式，全部添加列的类型
  *   此方式只是我们知道元数据的字段名，不知道类型的情况
  *
  *   不能提前确定（例如，记录的结构是经过编码的字符串，或者一个文本集合将会被解析，不同的字段投影给不同的
  *     用户），一个Schema RDD可以通过三步来创建。
  *
  */

object RDD2DataFrameBySchema {

  def main(args: Array[String]): Unit = {

    val spark = SparkUtil.sparkInitLocal("RDD2DataFrameBySchema")

    // MapPartitionsRDD
    val persionRDD =  spark.sparkContext.textFile("E://persions.txt")
    val schemaString = "id name age"

    // Generate the schema based on the string of schema 基于schema字符串（schemaString）生成schema
    val fields = schemaString.split(" ").map(fieldName => StructField(fieldName, StringType, nullable = true))
    val schema = StructType(fields)

    //根据读取的MapPartitionsRDD生成Row相关的MapPartitionsRDD
    val rowRDD = persionRDD.map(_.split(",")).map(attributes =>
      Row(attributes(0).trim(), attributes(1), attributes(2).trim()))

    //Apply the schema to the RDD(person) 根据Row相关的MapPartitionsRDD和shema创建DataFrame
    val personDF = spark.createDataFrame(rowRDD, schema)

    //根据DataFrame创建
    personDF.createOrReplaceTempView("person")
    val results = spark.sql("select * from person")

    //打印出全部的查询结果
    results.show()

    //读取查询结果中的某一列的全部值，例：name 第二列的值
    //results.map(attributes => "Name: " + attributes(1))

  }

}
