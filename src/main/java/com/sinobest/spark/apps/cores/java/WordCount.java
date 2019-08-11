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
 * Created by Administrator on 2016/11/16 0016.
 *
 * 集群运行模式  本地测试使用本地的CDH5.7.1
 * spark 读取hdfs文件 单词进行统计
 */
public class WordCount {

    public static void main(String[] args){
        /**
         * 创建SparkContext  设置Spark程序运行的配置信息
         * 创建JavaSparkConf   Spark的唯一入口,实际还是调用了Scala代码 SparkContext
         * */
        SparkConf conf = new SparkConf();
        conf.setAppName("Spark WordCount witter by Java");
        conf.setMaster("spark://hadoop01:7077");
        //conf.setJars(new String[]{"E:\\workspace\\打包Jar\\Spark\\SparkApps.jar"});

        JavaSparkContext sc = new JavaSparkContext(conf);

        /**
         * 更加SparkContext读取本地文件创建RDD
         * 并调用回调函数对lines进行单词切分
         * 读取参数
         */
        //JavaRDD<String> lines = sc.textFile("E://文档//大数据//Spark//源码//spark1.6.0//spark-1.6.0-bin-hadoop2.6/README.md");
        JavaRDD<String> lines;
        if(args.length ==1 && args[0] != null ) lines = sc.textFile(args[0]);
        else lines = sc.textFile("hdfs://hadoop01.com.cn:8020/tmp/CHANGES.txt");

        JavaRDD<String> words = lines.flatMap((FlatMapFunction<String, String>) line -> Arrays.asList(line.split(" ")));

        /**
         * 对单词切分的基础上对每个单词计数为1
         */
        JavaPairRDD<String, Integer> pairs = words.mapToPair((PairFunction<String, String, Integer>) word -> new Tuple2<>(word, 1));

        /**
         * 在每个单词的计算的基础上，对相同的单词进行累加1
         */
        JavaPairRDD<String, Integer> wordCount = pairs.reduceByKey((Function2<Integer, Integer, Integer>) (v1, v2) -> v1 + v2);

        /**
         * 打印统计的内容
         */
        wordCount.foreach((VoidFunction<Tuple2<String, Integer>>) pairs1 -> System.out.println(pairs1._1 + ":" + pairs1._2));
        //wordCount.foreach(pairs1 -> System.out.println(pairs1._1 + ":" + pairs1._2));

        /**
         * 关闭SpartContext
         */
        sc.stop();
    }

}
