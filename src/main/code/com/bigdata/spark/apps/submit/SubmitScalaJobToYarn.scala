package com.bigdata.spark.apps.submit

import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.Date

import org.apache.hadoop.conf.Configuration
import org.apache.spark.SparkConf

import org.apache.log4j.{Level, Logger}


/**
  * @author liulv 
  * @date 2019/8/10
  * @time 19:47
  *
  * @description 本地开发提交Scala程序作业到远程Spark
  */
//noinspection ScalaDocUnknownTag
object SubmitScalaJobToYarn {

  def main(args: Array[String]): Unit = {
    //Spark日志控制
    Logger.getLogger("org").setLevel(Level.WARN)

    val dateFormat = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss")
    val filename: String = dateFormat.format(new Date())
    var tmp: String = Thread.currentThread.getContextClassLoader.getResource("").getPath
    tmp = tmp.substring(0, tmp.length - 8)
    tmp = URLDecoder.decode(tmp, "UTF-8") //解决中文路径问题

    /**
      * Usage: org.org.apache.spark.deploy.yarn.Client [options]
Options:
  --jar JAR_PATH           Path to your application's JAR file (required in yarn-cluster
                           mode)
  --class CLASS_NAME       Name of your application's main class (required)
  --primary-py-file        A main Python file
  --primary-r-file         A main R file
  --arg ARG                Argument to be passed to your application's main class.
                           Multiple invocations are possible, each will be passed in order.
  --num-executors NUM      Number of executors to start (Default: 2)
  --executor-cores NUM     Number of cores per executor (Default: 1).
  --driver-memory MEM      Memory for driver (e.g. 1000M, 2G) (Default: 1024 Mb)
  --driver-cores NUM       Number of cores used by the driver (Default: 1).
  --executor-memory MEM    Memory per executor (e.g. 1000M, 2G) (Default: 1G)
  --name NAME              The name of your application (Default: Spark)
  --queue QUEUE            The hadoop queue to use for allocation requests (Default:
                           'default')
  --addJars jars           Comma separated list of local jars that want SparkContext.addJar
                           to work with.
  --py-files PY_FILES      Comma-separated list of .zip, .egg, or .py files to
                           place on the PYTHONPATH for Python apps.
  --files files            Comma separated list of files to be distributed with the job.
  --archives archives      Comma separated list of archives to be distributed with the job.
      */
    val arg0: Array[String] = Array[String](
      "--name", "test java submit job to yarn",
      "--class", "Scala_Test",
      "--executor-memory", "1G",
      "--addJars","hdfs://hadoop01:8020/user/root/jars/spark-yn-sjzl.jar",
      "--jar", "file E:\\spark1.6-lib\\lib\\scala-spark.jar",
      "--arg", "hdfs://node101:8020/user/root/log.txt"
    )
      //"--arg", "hdfs://node101:8020/user/root/badLines_yarn_" + filename,
      //"--addJars", "hdfs://hadoop01:8020/user/root/jars/spark-assembly-1.6.0-cdh5.7.1-hadoop2.6.0-cdh5.7.1.jar",
     //"--archives", "hdfs://hadoop01:8020/user/root/jars/spark-assembly-1.6.0-cdh5.7.1-hadoop2.6.0-cdh5.7.1.jar")

    //      SparkSubmit.main(arg0);
    val conf = new Configuration()
    val os: String = System.getProperty("os.name")
    var cross_platform = false
    if (os.contains("Windows")) cross_platform = true
    conf.setBoolean("mapreduce.app-submission.cross-platform", cross_platform) // 配置使用跨平台提交任务

    conf.set("fs.defaultFS", "hdfs://localhost:8020") // 指定namenode
    conf.set("mapreduce.framework.name", "yarn") // 指定使用yarn框架
    conf.set("yarn.resourcemanager.address", "localhost:8034") // 指定resourcemanager
    conf.set("yarn.resourcemanager.scheduler.address", "localhost:8030") // 指定资源分配器
    conf.set("mapreduce.jobhistory.address", "localhost:10020")
    System.setProperty("SPARK_YARN_MODE", "true")

    val sparkConf = new SparkConf()

    import scala.collection.JavaConverters._
    for (c <- conf.iterator().asScala){
      sparkConf.set(c.getKey, c.getValue)
    }

    sparkConf.setMaster("yarn-client").setAppName("test123")
      //.set("spark.yarn.jars", "hdfs:/user/root/jars/spark-assembly-1.6.0-cdh5.7.1-hadoop2.6.0-cdh5.7.1.jar")
  }
}
