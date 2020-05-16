/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// scalastyle:off println
package com.bigdata.spark.apps.sql.hive

//import com.google.common.io.{ByteStreams, Files}

import com.bigdata.spark.apps.submit.SparkSubmitUtil
import org.apache.spark.sql.{Row, SaveMode}
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{SparkConf, SparkContext}

object HiveFromSpark {
  case class Record(key: Int, value: String)

  // Copy kv1.txt file from classpath to temporary directory
  //val kv1Stream = HiveFromSpark.getClass.getResourceAsStream("/kv1.txt")
  //val kv1File = File.createTempFile("kv1", "txt")
  //kv1File.deleteOnExit()
  //ByteStreams.copy(kv1Stream, Files.newOutputStreamSupplier(kv1File))

  def main(args: Array[String]) {
    //System.setProperty("HADOOP_USER_NAME", "root")
    //System.setProperty("user.name", "root")
    //System.setProperty("SPARK_YARN_MODE", "true")
    //System.setProperty("HADOOP_CONF_DIR", "file:///etc/hadoop/conf")
    //System.setProperty("YARN_CONF_DIR", "file:///etc/hadoop/conf")
    //System.setProperty("SPARK_JAR", "hdfs://hadoop01:8020/user/root/jars/spark-assembly-1.6.0-cdh5.7.1-hadoop2.6.0-cdh5.7.1.jar")
    //System.setProperty("SPARK_YARN_APP_JAR", "file:///home/hadoop/apps/scala-spark/scala-spark.jar")

    //conf.set("mapred.remote.os", "Linux")
    //conf.set("mapreduce.app-submission.cross-platform", "true")

    val sparkConf = new SparkConf()
      .setAppName("HiveFromSpark")
      .setMaster("spark://hadoop01:7077") //yarn-client
      .set("deploy-mode", "cluster")
      //.set("spark.yarn.jars", "hdfs://hadoop01:8020/user/root/jars/spark-yn-sjzl.jar")  //集群的jars包,是你自己上传上去的
      //.setJars(List("file:///E:/Workspace/BigData-大数据/Spark-yy/ScalaSpark/target/scala-spark.jar")) //这是sbt打包后的文件
      .setJars(List("file:///home/hadoop/apps/scala-spark/scala-spark.jar")) //这是sbt打包后的文件
      //.set("spark.driver.host", "192.168.1.104") //设置你自己的ip

    SparkSubmitUtil.setJobHistory(sparkConf)

    val sc = new SparkContext(sparkConf)

    // A hive context adds support for finding tables in the MetaStore and writing queries
    // using HiveQL. Users who do not have an existing Hive deployment can still create a
    // HiveContext. When not configured by the hive-site.xml, the context automatically
    // creates metastore_db and warehouse in the current directory.
    val hiveContext = new HiveContext(sc)
    //hiveContext.setConf("hadoop01", "9083")
    import hiveContext.implicits._
    import hiveContext.sql

    //sql("CREATE TABLE IF NOT EXISTS src (key INT, value STRING)")
    //sql("LOAD DATA LOCAL INPATH '/tmp/kv1.txt' INTO TABLE src")

    // Queries are expressed in HiveQL
    println("Result of 'SELECT *': ")
    sql("SELECT * FROM default.src LIMIT 10").collect().foreach(println)


    val ehrHis = sql("SELECT * FROM default.src LIMIT 10")
    val count1 = ehrHis.count()

    val ehrHis2 = sql("SELECT * FROM default.src LIMIT 10")
    val count2 = ehrHis.count()

    val unall = ehrHis.union(ehrHis2)

    unall.write.mode(SaveMode.Overwrite).saveAsTable("zhr")


    // Aggregation queries are also supported.
    val count = sql("SELECT COUNT(*) FROM default.src").collect().head.getLong(0)
    println(s"COUNT(*): $count")

    // The results of SQL queries are themselves RDDs and support all normal RDD functions.  The
    // items in the RDD are of type Row, which allows you to access each column by ordinal.
    val rddFromSql = sql("SELECT key, value FROM default.src WHERE key < 10 ORDER BY key")

    println("Result of RDD.map:")
    val rddAsStrings = rddFromSql.map {
      case Row(key: Int, value: String) => s"Key: $key, Value: $value"
    }

    // You can also register RDDs as temporary tables within a HiveContext.
    val rdd = sc.parallelize((1 to 100).map(i => Record(i, s"val_$i")))
    rdd.toDF().registerTempTable("records")

    // Queries can then join RDD data with data stored in Hive.
    println("Result of SELECT *:")
    sql("SELECT * FROM records r JOIN default.src s ON r.key = s.key").collect().foreach(println)

    sc.stop()



  }
}
