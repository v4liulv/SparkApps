package sort.java;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import scala.Tuple2;

/**
 * Created by liulv on 2017/5/16.
 * <p>
 * 二次排序，具体的实现步骤：
 * 1、按照Order和Serializable实现自定义排序的key
 * 2、将要进行的二次排查的文件加载进来生成key-value类似的RDD  ,其中的key是SecondarySortKey，value就是本身的值
 * 我们构造一个自定义的key
 * 3、使用SortByKey基于自定义的key进行二次排序
 * 4、去除掉排序的key值。保留排序的结果
 */
public class SecondarySortApp {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf();
        conf.setAppName("Spark SecondarySortApp witten by Java");
        //conf.setMaster("spark://hadoop01:7077");
        conf.setMaster("local");
        //conf.setJars(new String[]{"E:\\workspace\\打包Jar\\Spark\\SparkApps.jar"});

        JavaSparkContext sc = new JavaSparkContext(conf);
        JavaRDD<String> lines = sc.textFile("file:/G://文档//Spark//源码//spark1.6.0//spark-1.6.0-bin-hadoop2.6//sort.txt");
        JavaPairRDD<SecondarySort, String> pairs = lines.mapToPair((PairFunction<String, SecondarySort, String>) line -> {
            String[] splited = line.split(" ");
            SecondarySort key = new SecondarySort(Integer.valueOf(splited[0]), Integer.valueOf(splited[1]));

            return new Tuple2<>(key, line);
        });

        JavaPairRDD<SecondarySort, String> sorted = pairs.sortByKey(false);

        JavaRDD<String> seconderySorted = sorted.map((Function<Tuple2<SecondarySort, String>, String>) sortedContent -> sortedContent._2);

        seconderySorted.foreach((VoidFunction<String>) System.out::println);
    }
}
