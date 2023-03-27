package com.hhz.ifttt.utils;

import org.apache.log4j.Logger;

import java.sql.*;

public class PrestoHelper {

    private static Logger logger = Logger.getLogger(PrestoHelper.class);
    public static String url = "jdbc:presto://172.17.107.241:18999/hive/ods?user=hdfs";
    private static Connection connection = null;
    private static synchronized Connection getConnection()throws Exception{
        if(connection == null || connection.isClosed()){
            try {
                connection = DriverManager.getConnection(url);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return connection;
    }

    public static void execute(String sql)throws Exception{
        Statement statment = getConnection().createStatement();
        logger.info(sql);
        statment.execute(sql);
    }

    public static ResultSet executeQuery(String sql)throws Exception{
        Statement statment = getConnection().createStatement();
        logger.info(sql);
        ResultSet resultSet = statment.executeQuery(sql);
        return resultSet;
    }
}
