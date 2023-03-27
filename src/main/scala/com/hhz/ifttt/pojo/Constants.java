package com.hhz.ifttt.pojo;


public class Constants {

    /**
     * 基本配置
     */
    public static final String KUDU_MASTERS = "hadoop2:7051,hadoop4:7051,hadoop5:7051";

//    public static final String BROKERS = "172.17.147.1:9092,172.17.147.3:9092,172.17.147.2:9092";
    public static final String BROKERS = "172.17.192.33:9092,172.17.192.31:9092,172.17.192.34:9092";

    public static final String NEW_BROKERS = "172.17.192.33:9092,172.17.192.31:9092,172.17.192.34:9092";

    public static final String CHECKPOINT_URL = "hdfs://172.17.147.101:8020/flink/checkpoints/%s";
    public static final String CHECKPOINT_OSS_URL = "oss://hhz-flink-tmp/flink-checkpoint/%s";
//    public static final String CHECKPOINT_URL = "file:///Users/zhangyinghao/fsdownload/flink-1.12.0/bin/checkpoint/%s";

    public static final String REDIS_PATH = "/home/resource_config/rds_cache.ini";
    public static final String REDIS_FLAG = "identity_fill";
    public static String IFTTT_RULE_INFO = "rule_info";

    public interface IFTTT_COMMON_PATTER_PROCESS {
        String IFTTT_COMMON_PATTER = "ifttt_common_pattern";

        String IFTTT_UPDATE_INFO = "ifttt_ratel_common_pattern";



        String IFTTT_DATA_USER_INFO = "user_info";
        String IFTTT_DATA_NOTE_CATEGORY = "note_category";
        String IFTTT_DATA_WIKI_NOTE = "wiki_note";
        String IFTTT_DATA_WIKI_CATEGORY = "wiki_category";
        String IFTTT_DATA_CATEGORY = "category";
        String IFTTT_DATA_RANK_NOTE_CATEGORY = "wiki_ranking_category";
        String IFTTT_DATA_CATEGORY_INTEREST = "category_interest";
        String IFTTT_DATA_CATEGORY_SEARCH_KEYWORD = "category_search_keyword";
        String IFTTT_DATA_CATEGORY_USER_SCORE = "user_all_category_score";
        String IFTTT_DATA_WIKI_BASIC = "wiki_basic";




    }

    /**
     *  // 1.主维度 author, brand, content
     *  // 2.维度属性
     */
    public static final  String IFTTT_DYNAMIC_DIM_FIELD_PREFIX = "ifttt_%s_%s";
    public static final String IFTTT_DYNAMIC_DIM_FIELD_PREFIX_V3 = "dim_ifttt_%s_%s";
    public static final String IFTTT_DYNAMIC_DIM_FIELD_PREFIX_V3_CACHE = "dim_ifttt_%s_%s_%s";
    public static final String IFTTT_ERROR_DATA_TOPIC = "flink_error_data";
    public static final String IFTTT_USER_PROFILE_DATA_KEY = "ifttt_user_ifttt_task_%s";

    public interface IFTTT_OUT_REASON{
        String RULE_MATCH_ERROR = "规则匹配异常, 异常信息: %s";
        String PRE_FILTER = "预过滤";

    }




    /**
     * ifttt 规则
     */
    public interface IFTTT_RULE {
        String RUNNING = "running";
        String UPDATE = "update";
        String STOP = "stop";
    }
    /**
     * iftttlog
     */
    public interface IFTTT_LOG {
        String LOG_SENSORS = "sensors";
        String LOG_ACTION = "action";
        String LOG_RESSHOW = "resshow";
        String LOG_RESCLICK = "resclick";
        String LOG_PV = "pv";
        String LOG_NGINX = "nginx";
    }

    public interface IFTTT_DIMM_FIELD{
        // 作者
        String DIM_AUTHOR = "author";
        // 品牌
        String DIM_BRAND = "brand";
        // 内容
        String DIM_CONTENT = "content";
        // 内容类型
        String DIM_BS = "bs";
    }

    // ifttt keyBy维度  交互共性
    public interface IFTTT_DIM {
        // 作者
        String DIM_AUTHOR = "author";
        // 品牌
        String DIM_BRAND = "brand";
        // 内容
        String DIM_CONTENT = "content";
        // 内容类型
        String DIM_CONTENT_TYPE = "content_type";
    }


    /**
     * 新sdk 日志类型
     */
    public interface LOG_TYPE{
        String LOG_RESSHOW="show";
        String LOG_RESCLICK="click";
        String LOG_SENSORS="event";
        String LOG_PV="pv";
    }

    /**
     * topic
     */
    public interface SINK_TOPIC{
        String SINK_SENSORS = "dwd_sensors_log_v2";
        String SINK_PV = "dwd_pv_log_v2";
        String SINK_CTR = "dwd_ctr_log_v2";
        String SINK_ERROR = "dwd_error_log_v2";
        String SINK_ACTION = "dwd_action_log_v2";

        String COMPLETE_STREAM = "test";
        String OUT_LOG = "ifttt_out_log";
        String IFTTT_RES = "flink_cep_pattern_result";
    }


}

