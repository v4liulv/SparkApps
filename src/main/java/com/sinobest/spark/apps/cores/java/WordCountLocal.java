package com.sinobest.spark.apps.cores.java;


import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import scala.Tuple2;

import java.util.Arrays;

/**
 * Created by Administrator on 2016/11/17 0017.
 * 本地运行模式
 */
public class WordCountLocal {

    public static void main(String[] args){

        /**
         * 创建SparkContext  设置Spark程序运行的配置信息
         * 创建JavaSparkConf   Spark的唯一入口,实际还是调用了Scala代码 SparkContext
         */
        SparkConf conf = new SparkConf().setAppName("Spark WordCount writer by Java").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);

        /**
         * 更加SparkContext读取本地文件创建RDD
         * 并调用回调函数对lines进行单词切分
         * 数据处理流程：textFile 通过创建hadoop file 并且new了一个HadoopRDD读取本地磁盘或者集群HDFS分布式文件，
         * 并且以分片的模式，存于集群中。
         * 基于HadoopRDD产生的Partition去掉行的key,生成对应MapPartionRDD
         */
        JavaRDD<String> lines = sc.textFile("file:/E://文档//大数据//Spark//源码//spark1.6.0//spark-1.6.0-bin-hadoop2.6/README.md");
        //JavaRDD<String> lines = sc.textFile("hdfs://hadoop01.com.cn:8020/tmp/CHANGES.txt");

        /**
         * 对每个MapPartion中的每一行进行单词切分，并合并成一个大的单词实例的集合
         * flatMap产生的依旧是MapPartionRDD ，产生拆分过的集合
         * 对每个Partion的每一行进行单词切分并合并成一个大的单词实例的集合
         */
        JavaRDD<String> words = lines.flatMap((FlatMapFunction<String, String>) line -> Arrays.asList(line.split(" ")));

        /**
         * 对单词切分的基础上对每个单词计数为1
         * MapPartionRDD集合切分并且 每个单词计数为1
         * 在产生了一个MapPartionRDD (hallo 1) (spark 1) 变成kayValue的方式
         */
        JavaPairRDD<String, Integer> pairs = words.mapToPair((PairFunction<String, String, Integer>) word -> new Tuple2<>(word, 1));

        /**
         * 在每个单词的计算的基础上，对相同的单词进行累加1
         * reduceByKey
         *    1 、 shuffle之前 进行localReduce操作，主要负责本地局部统计，并把统计结果按照分区策略放到不同的file中
         *    2、
         */
        JavaPairRDD<String, Integer> wordCount = pairs.reduceByKey((Function2<Integer, Integer, Integer>) (v1, v2) -> v1 + v2);


        /**
         * 以上的全部步骤，属于一个Stage, Stage的操作是基于内存迭代的
         */

        /**
         * 打印统计的内容
         */
        wordCount.foreach((VoidFunction<Tuple2<String, Integer>>) pairs1 -> System.out.println(pairs1._1 + ":" + pairs1._2));

    }
}
