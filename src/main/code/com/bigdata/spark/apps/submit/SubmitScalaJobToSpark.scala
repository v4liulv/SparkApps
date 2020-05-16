package com.bigdata.spark.apps.submit

/**
  * @author liulv 
  * @date 2019/8/10
  * @time 19:41
  *
  * @description 本地开发提交Scala程序作业到远程Spark
  */
//noinspection ScalaDocUnknownTag
object SubmitScalaJobToSpark {

  def main(args: Array[String]): Unit = {
    //val dateFormat = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss")
    //val filename = dateFormat.format(new Date())
    //var tmp = Thread.currentThread.getContextClassLoader.getResource("").getPath
    //tmp = tmp.substring(0, tmp.length - 8)
    //tmp = URLDecoder.decode(tmp, "UTF-8")

    val arg0 = Array[String](
      "--master", "spark://hadoop01:7077",
      //"--master", "local",
      //"--deploy-mode", "client",
      "--name", "test java submit job to spark",
      "--class", "com.sinobest.spark.sql.hive.HiveFromSpark",
      "--executor-memory", "1G",
      //"--conf","spark.executor.extraClassPath=/opt/cloudera/parcels/CDH-5.7.1-1.cdh5.7.1.p0.11/lib/hive/lib/*",//必须是集群全部节点都有
      //"--queue","root.default",//设置yarn队列
      "/home/hadoop/apps/scala-spark/scala-spark.jar" //程序包地址
    )

    import org.apache.spark.deploy.SparkSubmit
    SparkSubmit.main(arg0)

  }
}
