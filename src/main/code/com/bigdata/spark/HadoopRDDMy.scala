package com.bigdata.spark

import com.bigdata.spark.apps.submit.SparkSubmitUtil
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapred.{FileInputFormat, InputFormat, JobConf, TextInputFormat}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.deploy.SparkHadoopUtil
import org.apache.spark.rdd.{HadoopRDD, RDD}

/**
  * @author liulv 
  * @date 2020/2/6
  * @time 11:44
  *
  *  说明：HadoopRDD
  */
class HadoopRDDMy {

  def hadoopFile[K, V](
                      sc: SparkContext,
                        path: String,
                        inputFormatClass: Class[_ <: InputFormat[K, V]],
                        keyClass: Class[K],
                        valueClass: Class[V]): RDD[(K, V)] = {

    FileSystem.getLocal(sc.hadoopConfiguration)

    sc.hadoopConfiguration.set("mapreduce.input.fileinputformat.inputdir", path)

    // Add necessary security credentials to the JobConf before broadcasting it.
    SparkHadoopUtil.get.addCredentials(new JobConf(sc.hadoopConfiguration))
    new HadoopRDD(sc,
      new JobConf(sc.hadoopConfiguration),
      inputFormatClass,
      keyClass,
      valueClass,
      1)
  }
}

object HadoopRDDMy {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setAppName("RDDBasics").setMaster("local")
    SparkSubmitUtil.setJobHistory(conf)

    val sc = new SparkContext(conf)

    val path = "file:///E:/Workspace/BigData/Spark/spark-master-2.2.0/README.md"
    val hadoopRDD = new HadoopRDDMy().hadoopFile(sc, path, classOf[TextInputFormat], classOf[LongWritable], classOf[Text])
    val mapPartitionsRDD = hadoopRDD.map(pair => pair._1.toString + ":" + pair._2.toString).setName(path)
    mapPartitionsRDD.collect().foreach(println)


  }

}