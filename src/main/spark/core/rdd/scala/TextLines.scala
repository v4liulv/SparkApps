package rdd.scala

import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by liulv on 2017/5/25.
  *
  * 相同的行出现的总次数
  */
object TextLines {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    conf.setAppName("Wow, My first Spark APP in IDEA!")
    conf.setMaster("local") //本地模式
    val sc = new SparkContext(conf)

    //通过HadoopRdd和MapPartitionsRDD获取文件中每一行的信息
    val lines = sc.textFile("file:/G://文档//Spark//源码//spark1.6.0//spark-1.6.0-bin-hadoop2.6//README.md", 1)

    val lineCount = lines.map(line => (line, 1))//每一行变成内容和1构成Tuple

    val textLines = lineCount.reduceByKey(_+_)

    //collect是将机器中每个节点的数据收集起来解析成我们需要的数组， 数组里面就key value的Tuple（元祖）
    textLines.collect().foreach(pair =>(lines, println(pair._1 + ":" + pair._2)))

  }
}
