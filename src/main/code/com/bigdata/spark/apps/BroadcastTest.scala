package com.bigdata.spark.apps

import java.net.URLDecoder

import com.bigdata.spark.apps.submit.SparkSubmitUtil
import org.apache.spark.sql.SparkSession

/**
  *  Spark 共享变量-广播变量：将一个只读的变量缓存到每台服务器上。
  *  通过在一个变量 v 上调用 [[org.apache.spark.SparkContext.broadcast()]] 方法来进行创建。
  *  广播变量是 v 的一个 wrapper（包装器），可以通过调用 value 方法来访问它的值。
  *
  * @author liulv 
  * @date 2020/1/30
  * @time 19:55
  */
//noinspection ScalaDocUnknownTag,ScalaDocParserErrorInspection
object BroadcastTest {
  def main(args: Array[String]): Unit = {
    val blockSize = if (args.length > 2) args(2) else "1024"

    val jarPath = URLDecoder.decode("file://" + Thread.currentThread.getContextClassLoader.getResource("").getPath + "spark_apps.jar", "UTF-8")

    println("运行jar包路径：" + jarPath)

    val spark = SparkSession
      .builder()
      .master("yarn-client")
      .appName("Broadcast Test")
      .config("spark.driver.cores", "1")
      .config("spark.broadcast.blockSize", blockSize)
      .config("spark.jars", jarPath)
      .getOrCreate()

    SparkSubmitUtil.setJobHistory(spark)

    val sc = spark.sparkContext

    val slices = if (args.length > 0) args(0).toInt else 2
    val num = if (args.length > 1) args(1).toInt else 1000000

    val arr1 = (0 until num).toArray
    val barr1 = sc.broadcast(arr1)
    for (i <- 0 until 3) {
      println(s"Iteration $i")
      println("===========")
      val startTime = System.nanoTime
      val observedSizes = sc.parallelize(1 to 10, slices).map(_ => barr1.value.length)
      observedSizes.collect().foreach(i => println(i))
      println("Iteration %d took %.0f milliseconds".format(i, (System.nanoTime - startTime) / 1E6))
    }

    spark.stop()
  }
}
