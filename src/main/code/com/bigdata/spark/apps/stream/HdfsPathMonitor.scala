package com.bigdata.spark.apps.stream

import org.apache.spark.SparkConf
import org.apache.spark.streaming._

/**
  * 监控指定HDFS目录中创建的新文本文件,并且进行计算打印
  *
  * @author liulv 
  * @date 2020/2/9 0:08
  */
class HdfsPathMonitor {
  def monitorHdfsPath(): Unit ={
    val sparkConf = new SparkConf().setAppName("MonitorHDFS").setMaster("local[2]")
    val ssc = new StreamingContext(sparkConf, Seconds(10))

    val hdfsPath = "/spark/kafka/dataFile"
    val lines = ssc.textFileStream(hdfsPath)
    lines.flatMap(_.split(" ")).map(x => (x, 1)).reduceByKey(_ + _).print()

    ssc.start()
    ssc.awaitTermination();
  }
}

object HdfsPathMonitor {
  def apply(): HdfsPathMonitor = new HdfsPathMonitor()

  def main(args: Array[String]): Unit = {
    apply.monitorHdfsPath()
  }
}
