package com.contract.constants;

import com.fasterxml.classmate.ResolvedType;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: Ivan
 * @date: 2022/10/11
 * @Version: 1.0
 * @Description:
 */
public final class Constants {

    /**
     * blank constant.
     */
    public static final class Blank {
        public static final String BLANK_STRING = "";
        public static final char NULL_CHAR = '\u0000';
    }

    /**
     * system constant.
     */
    public static final class System {
        public static final String CLASS = "class";
        public static final String POINT = ".";
        public static final String POINT_CLASS = ".class";
        public static final String SLASH = "/";
        public static final String UNDER_LINE = "_";
        public static final String SEPARATOR = ":";
        public static final int COMPUTER_CORE = Runtime.getRuntime().availableProcessors() * 2;
        public static final String PROD = "prod";
    }

    /**
     * http constant.
     */
    public static final class Http {
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String APPLICATION_JSON = "application/json";
        public static final int STATUS_OK = 200;
        public static final int STATUS_404 = 404;
        public static final int STATUS_502 = 502;
    }

    /**
     * date constant.
     */
    public static final class Date {
        public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
        public static final String TIME_FORMAT = "HH:mm:ss";
        public static final String DATE_FORMAT_YYYY_MM_DD = "yyyy-MM-dd";
        public static final String DATE_FORMAT_YYYYMMDD = "yyyyMMdd";
    }

    /**
     * charset constant.
     */
    public static final class Charset {
        public static final String UTF8 = "utf-8";
        public static final String GBK = "gbk";
    }

    public static final class Time {
        public static final String TIME_ZONE = "GMT+8";
        public static final long DAY_MILL_SECOND = 1000L * 60 * 60 * 24;
        public static final long HOURS_MILL_SECOND = 1000L * 60 * 60;
        public static final long MINUTES_MILL_SECOND = 1000L * 60;
        public static final long SECONDS_MILL_SECOND = 1000L;
    }

    public static final class Cypto {
        public static final String BTC_USDT = "BTCUSDT";
    }
}