/*
package com.sinobest.mr.hbase_mr;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;



*/
/**
 * Created by Administrator on 08-12-2016.
 *
 *//*

public class Mr_MR_HBase {

    */
/** the column family containing the indexed row key *//*

    public static final byte[] INDEX_COLUMN = Bytes.toBytes("INDEX");


    */
/** the qualifier containing the indexed row key *//*

    public static final byte[] INDEX_QUALIFIER = Bytes.toBytes("ROW");

    private static class MyReadingMapper extends TableMapper<Text, Text> {
        private byte[] family;
        private HashMap<byte[], ImmutableBytesWritable> indexes;

        @Override
        protected void map(ImmutableBytesWritable key, Result value,
                           Context context) throws IOException, InterruptedException {
            // just print out the row key
            System.out.println("Got a row with key: "
                    + Arrays.toString(key.get()));
        }
        //	InterfaceAudience it ;
        //InterfaceStability it1;

        @Override
        protected void setup(Context context)
                throws IOException, InterruptedException {
            Configuration configuration = context.getConfiguration();
            String tableName = configuration.get("index.tablename");
            indexes = new HashMap<>();
        }
    }

    public static void main(String[] args) throws IOException,
            ClassNotFoundException, InterruptedException {
        String tablename = "BSH_SFZH";
        //tablename = args[0];
        System.err.println("Info tablename : " + tablename);
        Configuration conf = HBaseConfiguration.create();
        //E:\workspace\打包Jar\Spark
        conf.set("mapred.remote.os","Linux");
        conf.set("mapreduce.app-submission.cross-platform","true");
        conf.set("mapred.jar","E:\\workspace\\打包Jar\\Spark\\SparkApps.jar");
        //conf.set("mapred.jar","/home/hadoop/jar/mr/jar/SparkApps:jar");
        //conf.set("hbase-conf", "http://hadoop01.com.cn:60010/conf");
        //conf.set("hbase.zookeeper.quorum", "hadoop01");
        //conf.set("fs.default.name","hdfs://hadoop01:8020");
        conf.set("mapred.job.tracker","hadoop01:8032");
        conf.set("yarn.resourcemanager.address","hadoop01:8032");
        //HBaseUtil hbu = HBaseUtil.getInstance();
        Scan scan = new Scan();
        scan.setCaching(500);
        scan.setCacheBlocks(false);
        // conf.set(TableInputFormat.SCAN, convertScanToString(scan));
       // conf.set(TableInputFormat.INPUT_TABLE, tablename);

        */
/*
        conf.set("hbase.mapreduce.splittable", tablename);
        conf.set("hbase.mapreduce.inputtable.shufflemaps", "true");
        conf.set("index.tablename", tablename);
        *//*



        Job job = new Job(conf, tablename);
        job.setJarByClass(Mr_MR_HBase.class);
        //job.setMapperClass(MyReadingMapper.class);


        //Scan scan = new Scan();

        //conf.set("hbase.mapreduce.inputtable", tablename);
        //conf.set("hbase.mapreduce.scan", convertScanToString(scan));

       */
/*
       conf.setStrings("io.serializations",
				new String[] { conf.get("io.serializations"),
						MutationSerialization.class.getName(),
						ResultSerialization.class.getName(),
						KeyValueSerialization.class.getName() });
		*//*




        //job.setInputFormatClass(TableInputFormat.class);
        //job.setUser("hdfs");

        TableMapReduceUtil.initTableMapperJob(tablename, scan, MyReadingMapper.class, null, null, job);
        job.setMapperClass(MyReadingMapper.class);


        job.setNumReduceTasks(0);
        //job.setInputFormatClass(TableInputFormat.class);
        job.setOutputFormatClass(NullOutputFormat.class);


        //job.submit();
        job.waitForCompletion(true);
    }

   */
/*static String convertScanToString(Scan scan) throws IOException {
		ClientProtos.Scan proto = ProtobufUtil.toScan(scan);
		return Base64.encodeBytes(proto.toByteArray());
	}*//*

}

*/
