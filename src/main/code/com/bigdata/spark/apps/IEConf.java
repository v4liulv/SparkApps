package com.bigdata.spark.apps;

import org.apache.hadoop.conf.Configuration;

import java.util.Properties;

/**
 * @author liulv
 * @date 2020/2/8 2:04
 */
public interface IEConf {

    public String getAttribute();

    public String getHadoopAttribute();

    public Object getDefaultValue();

    public String getDescription();

    public String lookupValue(Properties tbl, Configuration conf);

    public String getValue(Properties tbl, Configuration conf) ;

    public long getLong(Properties tbl, Configuration conf);

    public long getLong(Configuration conf);

    public void setLong(Configuration conf, long value);

    public void setLong(Properties properties, Configuration conf );

    public String getString(Properties properties);

    public String getString();

    public String getString(Properties tbl, Configuration conf);

    public String getString(Configuration conf);

    public void setString(Configuration conf, String value);

    public void setString(Properties properties);

    public boolean getBoolean(Properties tbl, Configuration conf);

    public boolean getBoolean(Configuration conf);

    public boolean setBoolean(Properties properties, Configuration conf );

    public void setBoolean(Configuration conf, boolean value);

    public double getDouble(Properties tbl, Configuration conf);

    public double getDouble(Configuration conf);

    public void setDouble(Configuration conf, double value);

    public void setDouble(Properties properties, Configuration conf );

    public float getFloat(Properties tbl, Configuration conf);

    public float getFloat(Configuration conf);

    public void setFloat(Configuration conf, float value);

    public void setFloat(Properties properties, Configuration conf );

    public Integer getInt(Properties tbl, Configuration conf);

    public int getInt(Configuration conf);

    public void setInt(Configuration conf, int value);

    public void setInt(Properties properties, Configuration conf );
}
