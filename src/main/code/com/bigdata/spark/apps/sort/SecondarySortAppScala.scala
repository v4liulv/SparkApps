package com.bigdata.spark.apps.sort

import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by liulv on 2017/5/16.
  *
  *  Spark scala对数据的二次排序
  */
object SecondarySortAppScala {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    conf.setAppName("SecondarySortApp for Scala")
    conf.setMaster("local") //本地模式
    val sc = new SparkContext(conf)

    val lines = sc.textFile("file:/G://文档//Spark//源码//spark1.6.0//spark-1.6.0-bin-hadoop2.6//sort.txt", 1)

    val pairWithSortKey = lines.map(line => (new SecondarySortKey(line.split(" ")(0).toInt, line.split(" ")(1).toInt), line))

    val sorted = pairWithSortKey.sortByKey(ascending = false)

    val sortedResult = sorted.map(sortedLine => sortedLine._2)

    sortedResult.collect().foreach(println)
  }
}
