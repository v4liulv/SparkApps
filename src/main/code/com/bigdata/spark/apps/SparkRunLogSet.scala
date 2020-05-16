package com.bigdata.spark.apps

import org.apache.log4j.{Level, Logger}

/**
  * @author liulv 
  * @date 2020/2/1
  * @time 14:17
  * @description
  */
object SparkRunLogSet {
  def main(args: Array[String]): Unit = {
    //Spark日志控制
    Logger.getLogger("org").setLevel(Level.WARN)
  }
}
