package com.bigdata.spark.apps.cores

import com.bigdata.spark.apps.submit.SparkSubmitUtil
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by liulv on 2016/11/23 0023.
  * 弹性分布式数据集RDDs)
  * Spark	核心的概念是	Resilient	Distributed	Dataset	(RDD)：一个可并行操作的有容错机制的数据集合。有	2	种方式创建
  * RDDs：第一种是在你的驱动程序中并行化一个已经存在的集合；另外一种是引用一个外部存储系统的数据集，例如共享的
  * 文件系统，HDFS，HBase或其他	Hadoop	数据格式的数据源
  */
object RDDBasics2 {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("RDDBasics2").setMaster("local")
    SparkSubmitUtil.setJobHistory(conf)
    val sc = new SparkContext(conf)

    //并行集合
    /** 并行集合	(Parallelized	collections)	的创建是通过在一个已有的集合(Scala Seq)上调用	Spark Context	的parallelize方法
      *实现的。集合中的元素被复制到一个可并行操作的分布式数据集中。例如，这里演示了如何在一个包含	1	到	5	的数组中创建并行集合：
      **/
    val	data	=	Array(1,	2,	3,	4,	5)
    val	distData = sc.parallelize(data)
    val dataRDD = distData.map(data => data + 1).reduce((a , b) => a + b)
    println(dataRDD)

    sc.stop()
  }

}
