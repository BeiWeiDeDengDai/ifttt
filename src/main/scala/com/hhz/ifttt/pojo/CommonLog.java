package com.hhz.ifttt.pojo;

/**
 * @program realtime
 * @description:
 * @author: zhangyinghao
 * @create: 2022/07/25 16:12
 **/
public class CommonLog {

    private int partition;

    private long offset;

    private String log;

    public CommonLog(int partition, long offset, String log) {
        this.partition = partition;
        this.offset = offset;
        this.log = log;
    }

    public int getPartition() {
        return partition;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }
}
