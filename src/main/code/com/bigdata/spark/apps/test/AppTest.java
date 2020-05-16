package com.bigdata.spark.apps.test;

import com.bigdata.spark.apps.oracle.ReadOracleToHive;
import com.bigdata.spark.apps.sql.hive.ReadHive;
import com.bigdata.spark.apps.submit.SparkSubmitUtil;
import org.junit.Test;

/**
 * @author liulv
 * @date 2020/2/3
 * @time 16:12
 *
 * Unit test for simple App.
 */
public class AppTest {

    /**
     * 通过Submit提交ReadHive作业到Spark On Yarn
     */
    public static void main(String[] args) {
        SparkSubmitUtil.run(ReadHive.class.getSimpleName(), ReadHive.class.getName());
    }


    @Test
    public void readOracleToHiveTest(){
        SparkSubmitUtil.run(ReadOracleToHive.class.getSimpleName(), ReadOracleToHive.class.getName());
    }






}
