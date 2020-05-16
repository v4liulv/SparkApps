package com.bigdata.spark.apps.submit

import com.bigdata.spark.apps.cores.ClassPathUtil
import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.deploy.SparkSubmit
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.SparkSession.Builder

/**
  * @author liulv 
  * @date 2020/2/3
  * @time 16:12
  *
  *       通过Spark Submit提交Spark程序工具
  */
object SparkSubmitUtil {

  val classPath: String = ClassPathUtil.getClassPathFile

  val sparkAppJar: String = ClassPathUtil.getClassPathFile + "spark_apps.jar"

  def run(jobName: String, className: String): Unit = {
    Logger.getLogger("org").setLevel(Level.WARN)
    /**
      * Usage: org.org.apache.spark.deploy.yarn.Client [options]
      * Options:
      * --jar JAR_PATH           Path to your application's JAR file (required in yarn-cluster
      * mode)
      * --class CLASS_NAME       Name of your application's main class (required)
      * --primary-py-file        A main Python file
      * --primary-r-file         A main R file
      * --arg ARG                Argument to be passed to your application's main class.
      * Multiple invocations are possible, each will be passed in order.
      * --num-executors NUM      Number of executors to start (Default: 2)
      * --executor-cores NUM     Number of cores per executor (Default: 1).
      * --driver-memory MEM      Memory for driver (e.g. 1000M, 2G) (Default: 1024 Mb)
      * --driver-cores NUM       Number of cores used by the driver (Default: 1).
      * --executor-memory MEM    Memory per executor (e.g. 1000M, 2G) (Default: 1G)
      * --name NAME              The name of your application (Default: Spark)
      * --queue QUEUE            The hadoop queue to use for allocation requests (Default:
      * 'default')
      * --addJars jars           Comma separated list of local jars that want SparkContext.addJar
      * to work with.
      * --py-files PY_FILES      Comma-separated list of .zip, .egg, or .py files to
      * place on the PYTHONPATH for Python apps.
      * --files files            Comma separated list of files to be distributed with the job.
      * --archives archives      Comma separated list of archives to be distributed with the job.
      */
    val submitConf: Array[String] = Array[String](
      "--master", "yarn",
      "--name", jobName,
      "--class", className,
      sparkAppJar
    )
    System.setProperty("SPARK_YARN_MODE", "true")

    SparkSubmit.main(submitConf)
  }

  def setJobHistory(spark: Object): Unit = {
    spark match {
      case builder: Builder =>
        builder.config("spark.eventLog.enabled", "true")
          .config("spark.eventLog.dir", "hdfs://localhost:8020/user/spark/history")
          .config("spark.history.fs.logDirectory", "hdfs://localhost:8020/user/spark/history")
          .config("spark.yarn.historyServer.address", "http://localhost:18080")
          .config("spark.history.ui.port", "18080")
      case sparkConf: SparkConf =>
        sparkConf.set("spark.eventLog.enabled", "true")
          .set("spark.eventLog.dir", "hdfs://localhost:8020/user/spark/history")
          .set("spark.history.fs.logDirectory", "hdfs://localhost:8020/user/spark/history")
          .set("spark.yarn.historyServer.address", "http://localhost:18080")
          .set("spark.history.ui.port", "18080")
      case _ => throw new RuntimeException("setJobHistory 类型不对必须为Spark Builder、SparkConf其中一种，但是为：" + spark.getClass.getName)
    }
  }

}
