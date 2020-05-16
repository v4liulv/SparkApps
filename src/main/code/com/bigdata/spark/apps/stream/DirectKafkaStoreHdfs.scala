package com.bigdata.spark.apps.stream

import com.bigdata.spark.apps.submit.SparkSubmitUtil
import com.bigdata.spark.output.CustomTextOutputFormat
import org.apache.hadoop.io.compress.GzipCodec
import org.apache.hadoop.io.{NullWritable, Text}
import org.apache.hadoop.mapred.JobConf
import org.apache.hadoop.mapreduce.lib.output.{FileOutputCommitter, FileOutputFormat}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.{Milliseconds, Minutes, Seconds, StreamingContext}
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.sql.{DataFrame, SQLContext}
import org.apache.spark.sql.internal.SQLConf
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe

/**
  * 说明：SparkStream示例，从Socket读取实时流数据和从kafka读取流数据消费数据
  *
  * 已经安装kafka并且创建topic1, 通过kafka生产者生产数据
  * kafka-console-producer --broker-list localhost:9092 --topic topic1
  *
  * 这样SparkStream本样例能实时消费Kafka生产的数据
  * 这里只是打印生成的数据
  * stream.map(record => record.value).print()
  *
  * 查看offset
  * kafka-run-class kafka.tools.ConsumerOffsetChecker --broker-info --zookeeper localhost:2181/kafka --group user_group --topic topic1
  *
  * @author liulv
  * @date 2020/2/8 1:45
  */
class DirectKafkaStoreHdfs {
}

object DirectKafkaStoreHdfs {
  def main(args: Array[String]): Unit = {
    val conf = new
        SparkConf().setMaster("local[2]").setAppName("NetworkWordCount")

    SparkSubmitUtil.setJobHistory(conf)
    val ssc = new StreamingContext(conf, Milliseconds(20))

    //socket
    //val lines = ssc.socketTextStream("localhost", 9092)
    //lines.flatMap(_.split(" ")).map(word => (word, 1)).reduceByKey(_ + _).print()

    //kafka
    val kafkaParams = Map[String, Object](
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> "localhost:9092",
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.GROUP_ID_CONFIG -> "user_group",
      ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "latest", //自动重置offset方式
      ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG -> (true: java.lang.Boolean), //自动提交
      "offsets.storage" -> "zookeeper", //老版方式windows能查询方式，offsets存储方式，新版已经存储到kafka
      "zookeeper.connect" -> "localhost:2181/kafka" //zookeeper连接方式，如果offset存储在zookeeper则需要配置
    )

    val topics = Array("topic1")

    val stream = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaParams)
    )

    //val value = stream.map(record => record.value).filter(v => v.trim.contains(""))
    //value.saveAsTextFiles("/spark/kafka/test", "txt")
    val sc = ssc.sparkContext
    val sqlContext = SQLContext.getOrCreate(ssc.sparkContext)
    import sqlContext.implicits._

    //配置输出文件不生成success文件
    sc.hadoopConfiguration.set(FileOutputCommitter.SUCCESSFUL_JOB_OUTPUT_DIR_MARKER, "false")

    //配置一些参数
    //如果设置为true，sparkSql将会根据数据统计信息，自动为每一列选择单独的压缩编码方式
    sqlContext.setConf(SQLConf.COMPRESS_CACHED.key, "true")
    //控制列式缓存批量的大小。增大批量大小可以提高内存的利用率和压缩率，但同时也会带来OOM的风险
    sqlContext.setConf(SQLConf.COLUMN_BATCH_SIZE.key, "1000")
    sqlContext.setConf(SQLConf.AUTO_BROADCASTJOIN_THRESHOLD.key, "10485760")
    //设为true，则启用优化的Tungsten物理执行后端。Tungsten会显示的管理内存，并动态生成表达式求值得字节码
    sqlContext.setConf("spark.sql.tungsten.enabled", "true")
    //配置shuffle是的使用的分区数
    sqlContext.setConf(SQLConf.SHUFFLE_PARTITIONS.key, "10")

    ////设置输出的JobConf
    val jobConf = new JobConf(sc.hadoopConfiguration)
    CustomTextOutputFormat.setJobConf(jobConf)
    //设置输出文件最大大小，这里设置比较小为了测试，正式环境请结合要去调整
    CustomTextOutputFormat.setMaxFileSize(1024 * 10)
    //设置输出文件后缀
    CustomTextOutputFormat.setFileSuffix(".txt")
    //设置输出启动压缩
    //jobConf.setBoolean(FileOutputFormat.COMPRESS, true)
    //设置压缩类为Gzip
    //jobConf.set(FileOutputFormat.COMPRESS_CODEC, classOf[GzipCodec].getName)

    stream.map(t => t.value()).foreachRDD { rdd =>
      if (!rdd.isEmpty()) {
        if (!rdd.isEmpty()) {
          val df = rdd.toDF().repartition(2)
          //df.show()
          val allClumnName: String = "value"
          //合并全部列为一列allclumn通过splitRex进行分割
          val splitRex = ","
          val result: DataFrame = df.selectExpr(s"concat_ws('$splitRex',$allClumnName) as allclumn")
          result.show(1)

          val r = rdd.mapPartitions { iter =>
            val text = new Text()
            iter.map { x =>
              text.set(x.toString)
              (NullWritable.get(), text)
            }
          }.saveAsHadoopFile("/spark/kafka/dataFile",
            classOf[NullWritable],
            classOf[String],
            classOf[CustomTextOutputFormat],
            jobConf
          )
        }
      }
    }

    ssc.start()
    ssc.awaitTermination()
  }
}
