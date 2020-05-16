package com.bigdata.spark.apps.oracle;

import org.apache.spark.sql.jdbc.JdbcDialect;
import org.apache.spark.sql.jdbc.JdbcDialects;
import org.apache.spark.sql.jdbc.JdbcType;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.MetadataBuilder;
import scala.Option;

import java.sql.Types;

/**
 * @author liulv
 * @date 2020/1/22
 * @time 14:09
 *
 * @description
 */
public class OracleDateTypeInit {

    public static void oracleInit() {
        JdbcDialect dialect = new JdbcDialect() {
            //判断是否为oracle库
            @Override
            public boolean canHandle(String url) {
                return url.startsWith("jdbc:oracle");
            }

            //用于读取Oracle数据库时数据类型的转换
            @Override
            public Option<DataType> getCatalystType(int sqlType, String typeName, int size, MetadataBuilder md) {
                if (sqlType == Types.DATE && typeName.equals("DATE") && size == 0)
                    return Option.apply(DataTypes.TimestampType);
                return Option.empty();
            }

            //用于写Oracle数据库时数据类型的转换
            @Override
            public Option<JdbcType> getJDBCType(DataType dt) {
                if (DataTypes.StringType.sameType(dt)) {
                    return Option.apply(
                            new JdbcType("VARCHAR2(255)", Types.VARCHAR));
                } else if (DataTypes.BooleanType.sameType(dt)) {
                    return Option.apply(
                            new JdbcType("NUMBER(1)", Types.NUMERIC));
                } else if (DataTypes.IntegerType.sameType(dt)) {
                    return Option.apply(
                            new JdbcType("NUMBER(10)", Types.NUMERIC));
                } else if (DataTypes.LongType.sameType(dt)) {
                    return Option.apply(
                            new JdbcType("NUMBER(19)", Types.NUMERIC));
                } else if (DataTypes.DoubleType.sameType(dt)) {
                    return Option.apply(
                            new JdbcType("NUMBER(19,4)", Types.NUMERIC));
                } else if (DataTypes.FloatType.sameType(dt)) {
                    return Option.apply(
                            new JdbcType("NUMBER(19,4)", Types.NUMERIC));
                } else if (DataTypes.ShortType.sameType(dt)) {
                    return Option.apply(
                            new JdbcType("NUMBER(5)", Types.NUMERIC));
                } else if (DataTypes.ByteType.sameType(dt)) {
                    return Option.apply(
                            new JdbcType("NUMBER(3)", Types.NUMERIC));
                } else if (DataTypes.BinaryType.sameType(dt)) {
                    return Option.apply(
                            new JdbcType("BLOB", Types.BLOB));
                } else if (DataTypes.TimestampType.sameType(dt)) {
                    return Option.apply(
                            new JdbcType("DATE", Types.DATE));
                } else if (DataTypes.DateType.sameType(dt)) {
                    return Option.apply(
                            new JdbcType("DATE", Types.DATE));
                } else if (DataTypes.createDecimalType()
                        .sameType(dt)) { //unlimited
/*                    return DecimalType.Fixed(precision, scale)
                            =>Some(JdbcType("NUMBER(" + precision + "," + scale + ")",
                            java.sql.Types.NUMERIC))*/
                    return Option.apply(
                            new JdbcType("NUMBER(38,4)", Types.NUMERIC));
                }
                return Option.empty();
            }
        };
        //注册此方言
        JdbcDialects.registerDialect(dialect);
    }
}