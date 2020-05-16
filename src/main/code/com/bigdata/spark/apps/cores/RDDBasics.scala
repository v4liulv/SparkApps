package com.bigdata.spark.apps.cores

import com.bigdata.spark.apps.submit.SparkSubmitUtil
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by liulv on 2016/11/22 0022.
  * RDD 基础操作 : 弹性分布式数据集  一个可并行操作的有容错机制的数据集合
  * RDDs：第一种是在你的驱动程序中并行化一个已经存在的集合；另外一种是引用一个外部存储系统的数据集，例如共享的
  * 文件系统，HDFS，HBase或其他	Hadoop	数据格式的数据源。
  */
object RDDBasics {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setAppName("RDDBasics").setMaster("local")
    SparkSubmitUtil.setJobHistory(conf)

    val sc = new SparkContext(conf)

    val path = "file:\\E:\\Workspace\\BigData\\Spark\\spark-master-2.2.0\\README.md"
    val textFile = sc.textFile(path)

    val rddCount = textFile.count() //	RDD	的数据条数
    println(rddCount)  //Long 95

    val rddFirst = textFile.first()  //	RDD	的第一行数据
    println("rddFirst===>" + rddFirst)   //res1:	String	=	#	Apache	Spark

    /** 现在让我们使用一个	transformation (转换)，我们将使用	filter	在这个文件里返回一个包含子数据集的新	RDD*/
    val linesWithSpark = textFile.filter(lines => lines.contains("Spark"))

    /** actions	和	transformations	链接在一起 **/
    textFile.filter(lines => lines.contains("Spark")).count()

    /** RDD	actions	和	transformations	能被用在更多的复杂计算中。比方说，我们想要找到一行中最多的单词数量 **/
    textFile.map(line => line.split(" ").length).reduce((a, b) => if (a > b) a else b ) //res4:	Long	=	15

    /** 	map和reduce的参数是	Scala	的函数串(闭包)，并且可以使用任何语言特性或者	Scala/Java	类库。例如，我们可以很方便地调用其他的函数声明。	我们使用Math.max()
	  函数让代码更容易理解**/
    textFile.map(line => line.split(" ").length).reduce((a, b) => Math.max(a, b))  //res5:	Int	=	15

    /** Hadoop	流行的一个通用的数据流模式是	Map Reduce。Spark	能很容易地实现	Map Reduce **/
    val wordCounts = textFile.flatMap(line => line.split(" ")).map(word => (word, 1)).reduceByKey((a, b) => a + b ) //word Counts:	spark.RDD[(String,	Int)]
    wordCounts.collect().foreach(print)

    /** Spark	支持把数据集拉到集群内的内存缓存中。当要重复访问时这是非常有用的，例如当我们在一个小的热(hot)数据集中查
    询，或者运行一个像网页搜索排序这样的重复算法。作为一个简单的例子，让我们把lines With Spark数据集标记在缓存中 **/
    linesWithSpark.cache()
    linesWithSpark.count()
    linesWithSpark.count()

    sc.stop()

  }

}
