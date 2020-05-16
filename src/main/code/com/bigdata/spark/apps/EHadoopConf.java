package com.bigdata.spark.apps;

import org.apache.hadoop.conf.Configuration;

import java.util.Properties;

/**
 * @author liulv
 * @date 2020/1/10
 * @time 17:57
 *
 * 枚举配置工具类，属性名、cofn属性名、默认值、说明
 * @see Properties
 * @see Configuration
 *
 * 1）赋值到Configuration：
 * 通过EConf.属性.setString(Properties, Configuration)方式读取Properties中根据属性名的读取到值
 * 然后写入到Configuration中
 *
 * 2) 读取值
 * a. 可以通过EConf.属性.getString(Properties)}根据属性名从Properties中读取值返回
 * b. 值也可以根据属性名从Configuration中读取如：EConf.属性.getString(Configuration)
 * c.如果无配置可以读取其属性名的默认值通 EConf.属性.getDefaultValue()方式获取其默认值
 *
 */
public enum EHadoopConf implements IEConf {
    HBASE_DEFAULT_FAMILY("hbase_default_family", "hbase_default_family", "cf",
            "默认列族名"),
    ORACLE_DRIVER_CLASS("oracle_driver_class", null, "oracle.jdbc.driver.OracleDriver",
            "Oracle连接Driver类"),
    ORACLE_DB_URL("oracle_db_url", null, "jdbc:oracle:thin:@//dzzx-db1.hnisi.com.cn:1523/zxkfk",
            "Oracle连接url"),
    ORACLE_USER_NAMEL("oracle_user_name", null, "pcs_dsjyyfx_bzk",
            "Oracle连接用户"),
    ORACLE_USER_PASSWORD("oracle_password", null, "pcs_dsjyyfx_bzk",
            "Oracle连接密码"),
    ORACLE_ID_FIELD("oracle_id_field", "oracle_id_field", "SYSTEMID",
            "Oracle主键字段名"),
    ;

    /**
     * 属性名称
     */
    public String attribute;
    /**
     * Hadoop Conf配置属性名
     */
    public String hadoopAttribute;
    /**
     * 默认值
     */
    public Object defaultValue;
    /**
     * 属性说明
     */
    public String description;

    EHadoopConf(String attribute, String hadoopAttribute, Object defaultValue, String description) {
        this.attribute = attribute;
        this.hadoopAttribute = hadoopAttribute;
        this.defaultValue = defaultValue;
        this.description = description;
    }

    @Override
    public String getAttribute() {
        return attribute;
    }

    @Override
    public String getHadoopAttribute() {
        return hadoopAttribute;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String lookupValue(Properties tbl, Configuration conf) {
        String result = null;
        if (tbl != null) {
            result = tbl.getProperty(attribute);
        }
        if (result == null && conf != null) {
            result = conf.get(attribute);
            if (result == null && hadoopAttribute != null) {
                result = conf.get(hadoopAttribute);
            }
        }
        return result;
    }

    @Override
    public String getValue(Properties tbl, Configuration conf) {
        String result = null;
        if (tbl != null) {
            result = tbl.getProperty(attribute);
        }
        if (result == null && conf != null) {
            result = conf.get(attribute);
            if (result == null && hadoopAttribute != null) {
                result = conf.get(hadoopAttribute);
            }
        }
        if (result == null) {
            result = (String) getDefaultValue();
        }
        return result;
    }

    @Override
    public long getLong(Properties tbl, Configuration conf) {
        String value = lookupValue(tbl, conf);
        if (value != null) {
            return Long.parseLong(value);
        }
        return ((Number) defaultValue).longValue();
    }

    @Override
    public long getLong(Configuration conf) {
        return getLong(null, conf);
    }

    @Override
    public void setLong(Configuration conf, long value) {
        conf.setLong(attribute, value);
    }

    @Override
    public void setLong(Properties properties, Configuration conf) {
        long value = Long.parseLong(lookupValue(properties, conf));
        conf.setLong(attribute, value);
    }

    @Override
    public String getString(Properties tbl) {
        String value = lookupValue(tbl, null);
        return value == null ? (String) defaultValue : value;
    }

    @Override
    public String getString() {
        return (String) defaultValue;
    }

    @Override
    public String getString(Properties tbl, Configuration conf) {
        String value = lookupValue(tbl, conf);
        return value == null ? (String) defaultValue : value;
    }

    @Override
    public String getString(Configuration conf) {
        return getString(null, conf);
    }

    @Override
    public void setString(Configuration conf, String value) {
        conf.set(attribute, value);
    }

    @Override
    public void setString(Properties properties) {
        String value = getValue(properties, null);
        defaultValue = value;
    }

    @Override
    public boolean getBoolean(Properties tbl, Configuration conf) {
        String value = lookupValue(tbl, conf);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return (Boolean) defaultValue;
    }

    @Override
    public boolean getBoolean(Configuration conf) {
        return getBoolean(null, conf);
    }

    @Override
    public boolean setBoolean(Properties properties, Configuration conf) {
        boolean value = Boolean.parseBoolean(lookupValue(properties, conf));
        conf.setBoolean(attribute, value);
        return value;
    }

    @Override
    public void setBoolean(Configuration conf, boolean value) {
        conf.setBoolean(attribute, value);
    }

    @Override
    public double getDouble(Properties tbl, Configuration conf) {
        String value = lookupValue(tbl, conf);
        if (value != null) {
            return Double.parseDouble(value);
        }
        return ((Number) defaultValue).doubleValue();
    }

    @Override
    public double getDouble(Configuration conf) {
        return getDouble(null, conf);
    }

    @Override
    public void setDouble(Configuration conf, double value) {
        conf.setDouble(attribute, value);
    }

    @Override
    public void setDouble(Properties properties, Configuration conf) {
        Double value = Double.parseDouble(lookupValue(properties, conf));
        conf.setDouble(attribute, value);
    }

    @Override
    public float getFloat(Properties tbl, Configuration conf) {
        String value = lookupValue(tbl, conf);
        if (value != null) {
            return Float.parseFloat(value);
        }
        return ((Number) defaultValue).floatValue();
    }

    @Override
    public float getFloat(Configuration conf) {
        return getFloat(null, conf);
    }

    @Override
    public void setFloat(Configuration conf, float value) {
        conf.setFloat(attribute, value);
    }

    @Override
    public void setFloat(Properties properties, Configuration conf) {
        float value = Float.parseFloat(lookupValue(properties, conf));
        conf.setFloat(attribute, value);
    }

    @Override
    public Integer getInt(Properties tbl, Configuration conf) {
        String value = lookupValue(tbl, conf);
        if (value != null) {
            return Integer.parseInt(value);
        }
        return ((Number) defaultValue).intValue();
    }

    @Override
    public int getInt(Configuration conf) {
        return getInt(null, conf);
    }

    @Override
    public void setInt(Configuration conf, int value) {
        conf.setInt(attribute, value);
    }

    @Override
    public void setInt(Properties properties, Configuration conf) {
        int value = Integer.parseInt(lookupValue(properties, conf));
        conf.setInt(attribute, value);
    }
}
