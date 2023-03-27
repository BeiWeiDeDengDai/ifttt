package com.hhz.ifttt.pojo;

/**
 * @program ifttt
 * @description:
 * @author: zhangyinghao
 * @create: 2022/10/20 16:55
 **/
public class DimModule {

    private String tableName;

    private String rowKey;

    private String family;

    private String column;

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getRowKey() {
        return rowKey;
    }

    public void setRowKey(String rowKey) {
        this.rowKey = rowKey;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
