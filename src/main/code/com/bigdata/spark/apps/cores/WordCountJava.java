package com.bigdata.spark.apps.cores;

import com.bigdata.spark.apps.SparkUtil;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import scala.Tuple2;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Iterator;

/**
 * @author liulv
 * @date 2020/3/17 6:32
 *
 * 在MapReduce中是通过hdfs输入文件数据
 * map编程对数据进行分割，然后每个单词作为key, value值为1
 * reduce编程key的全部value值进行累积，进行输出即得到单词计数
 *
 * Spark怎么进行单词计数
 */
public class WordCountJava {
    public static void main(String[] args) throws UnsupportedEncodingException, InterruptedException {

        String dataPath = PUtil.getDataPath();
        String logFile = dataPath + "/README.md";
        String hdfsFile = "/spark/README.md";

        SparkContext sc = SparkUtil.getSC("wordCount");

        //hadoopFile(path, classOf[TextInputFormat], classOf[LongWritable], classOf[Text],
        //      minPartitions).map(pair => pair._2.toString).setName(path)
        JavaRDD<String> lines = sc.textFile(hdfsFile, 1).toJavaRDD();

        //(FlatMapFunction<String, String>) line -> Arrays.asList(line.split(" "))
        JavaRDD<String> words = lines.flatMap((FlatMapFunction<String, String>) line -> {
            return Arrays.asList(line.split(" ")).iterator();
        });
        //JavaRDD<String> words = line.flatMap(new Split());

        JavaPairRDD<String, Integer> wordsAndOne = words.mapToPair(word -> new Tuple2<>(word, 1));
        //JavaPairRDD<String, Integer> wordsAndOne = words.mapToPair(new Pair());

        JavaPairRDD<String, Integer> wordCount = wordsAndOne.reduceByKey(Integer::sum);
        //JavaPairRDD<String, Integer> wordCount = wordsAndOne.reduceByKey(new Count());

        JavaPairRDD<Integer, String> countWord =  wordCount.mapToPair(word -> new Tuple2<>(word._2, word._1));
        //JavaPairRDD<Integer, String> countWord =  wordCount.mapToPair(new Trs());

        //排序
        JavaPairRDD<Integer, String> sortByKey = countWord.sortByKey(false);

        //key value互换
        JavaPairRDD<String, Integer> result = sortByKey.mapToPair(pair -> new Tuple2<>(pair._2, pair._1));
        //JavaPairRDD<String, Integer> result = sortByKey.mapToPair(new Trs2());

        result.foreach(pairs1 -> System.out.println(pairs1._1 + ":" + pairs1._2));
        //result.foreach(new Print());

        sc.stop();
    }
}

/**
 * 从每个输入记录返回零个或多个输出记录的函数。
 */
class Split implements FlatMapFunction<String, String> {
    public Iterator<String> call(String s) {
       return  Arrays.asList(s.split(" ")).iterator();
    }
}

/**
 * 返回键值对(Tuple2<K, V>)的函数，可用于构造PairRDDs。
 *
 * 对每个单词计数为1
 */
class Pair implements PairFunction<String, String, Integer> {
    public Tuple2<String, Integer> call(String s) {
        return  new Tuple2<>(s, 1);
    }
}

/**
 * 一个双参数函数，接受类型为T1和T2的参数并返回一个R。
 *
 * 累加器
 */
class Count implements Function2<Integer, Integer, Integer> {
    public Integer call(Integer a, Integer b) { return a + b; }
}

/**
 * Tuple2<String, Integer> 转换其key和value值
 */
class Trs implements PairFunction<Tuple2<String, Integer>, Integer, String> {
    @Override
    public Tuple2<Integer, String> call(Tuple2<String, Integer> stringIntegerTuple2) throws Exception {
        return new Tuple2<>(stringIntegerTuple2._2, stringIntegerTuple2._1);
    }
}

/**
 * Tuple2<Integer, String> 转换其key和value值
 */
class Trs2 implements PairFunction<Tuple2<Integer, String>, String, Integer > {
    @Override
    public Tuple2<String, Integer> call(Tuple2<Integer, String> stringIntegerTuple2) throws Exception {
        return new Tuple2<>(stringIntegerTuple2._2, stringIntegerTuple2._1);
    }
}

/**
 * 一个没有返回值的函数。
 *
 * 用于打印 tuple2的key和value值
 */
class Print implements VoidFunction<Tuple2<String, Integer>> {
    @Override
    public void call(Tuple2<String, Integer> stringIntegerTuple2) throws Exception {
        System.out.println(stringIntegerTuple2._1 + ":" + stringIntegerTuple2._2);
    }
}


