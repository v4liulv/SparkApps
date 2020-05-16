package com.bigdata.spark.apps.stream

import com.bigdata.spark.apps.submit.SparkSubmitUtil
import com.bigdata.spark.output.CustomTextOutputFormat
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.{NullWritable, Text}
import org.apache.hadoop.mapred.{FileOutputFormat, JobConf}
import org.apache.hadoop.mapreduce.lib.output.FileOutputCommitter
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * 消费Kafka生产数据，并进行单词统计
  *
  * @author liulv 
  * @date 2020/2/8 12:52
  */
class KafkaWordCountStoreHdfs {
}

object KafkaWordCountStoreHdfs {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("ConsumeKafkaWordCount").setMaster("local[2]")

    SparkSubmitUtil.setJobHistory(sparkConf)

    val ssc = new StreamingContext(sparkConf, Seconds(2))

    val hadoopConf = ssc.sparkContext.hadoopConfiguration
    val job = new JobConf(hadoopConf)
    job.set(FileOutputCommitter.SUCCESSFUL_JOB_OUTPUT_DIR_MARKER, "false")
    CustomTextOutputFormat.setJobConf(job)
    val outputPath = "/spark/kafka/wordcount"
    //解决在OutputFormat过程无法获取OutputPath
    FileOutputFormat.setOutputPath(job, new Path(outputPath))

    val topics = "topic1"
    val brokers = "localhost:9092"
    val groupId = "user_group"

    val topicsSet = topics.split(",").toSet
    val kafkaParams = Map[String, Object](
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> brokers,
      ConsumerConfig.GROUP_ID_CONFIG -> groupId,
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer])

    val messages = KafkaUtils.createDirectStream[String, String](
      ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](topicsSet, kafkaParams))
    val wordCount = messages.map(_.value()).flatMap(_.split(" ")).map(a => (a, 1)).reduceByKey(_ + _)

    wordCount.print()
    wordCount.mapPartitions ({iter =>
      val text = new Text()
      iter.map { x =>
        text.set(x._1.toString + ":" + x._2.toString)
        (NullWritable.get(), text)
      }
    }).saveAsHadoopFile(
      outputPath,
      classOf[NullWritable],
      classOf[Text],
      classOf[CustomTextOutputFormat],
      job
    )

    ssc.start()
    ssc.awaitTermination()
  }
}

