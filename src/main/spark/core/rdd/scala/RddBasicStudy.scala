package rdd.scala

import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by liulv on 2017/3/31.
  *
  * 本类示例；Spark RDD 基础
  *
  * RDD 简述：
  *         Spark RDD 弹性数据分布集，其实就是分布式的元素集合 。
  *     在Spark中，对数据的操作不外乎创建RDD、转化RDD、以及调用RDD进行求值。
  *     在这一切的背后,Spark会自动将RDD中的数据分发到集群，并将操作并行化执行。
  *
  *  1. 创建RDD
  *       方式一：读取一个外部数据集
  *       方式二：驱动程序里分发驱动器程序中的对象集合（比如List 和 Set）
  *
  *  Spark或者Shell程序工作方式：
  *      1、从外部数据创建输入RDD
  *      2、使用filter()这样的转换操作对RDD进行转换，以定义新的RDD
  *      3、告诉Spark对需要被重用的中间结果RDD执行persist()操作 （cache()的默认存储级别调用persist（）是一样的）
  *      4、使用行动操作（例如count()或first()）等来触发一次并行计算，Spark会对计算进行优化后再执行
  *
  */
object RddBasicStudy
{

  def main(args: Array[String]): Unit =
  {
    val conf = new SparkConf()
    conf.setAppName("RDD basic RDD create")
    conf.setMaster("local")
    //conf.setMaster("spark://hadoop01:7077")

    val sc = new SparkContext(conf)

    //创建RDD
    //通过 SparkContext.textFile 读取外部文本文件创建RDD
    val lines = sc.textFile("file:/G://文档//Spark//源码//spark1.6.0//spark-1.6.0-bin-hadoop2.6//README.md", 1)
    //var lines = sc.textFile("hdfs://hadoop01.com.cn:8020/tmp/CHANGES.txt")

    //创建出来的RDD 支持两种类型的操作：转换操作 和 行为操作
    //转换操作会由一个RDD生产一个新的RDD， 比如过滤就是一种常见的转换filter
    val words =  lines.flatMap(line => line.split(""))
    val wordsFilter = words.filter(_.contains("A"))

    //持久化RDD到内存中，  把数据读取到内存中，并反复查询这部分数据
    val wordsP = wordsFilter.persist()

    //另外行动操作会对RDD计算出一个结果，并把结果换回到驱动器程序中，或者结果存储到外部系统如HDFS
    System.out.println("Info : wordsP: " + wordsFilter.collect().foreach(words1 => println(words1)))

    sc.stop()

  }

}
