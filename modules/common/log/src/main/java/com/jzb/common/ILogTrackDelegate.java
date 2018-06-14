package com.jzb.common;

import java.io.File;

/**
 * Created by wikipeng on 2018/2/9.
 */
public interface ILogTrackDelegate extends ILogDelegate {
    File getDefaultLogFile();

    File generateLogFile(String fileName);

    /**
     * 是否需要上传日志
     */
    boolean isNeedSendLog();

    /**
     * 上传日志
     */
    void sendLogToServer();

    /**
     * 是否需要清空日志文件
     */
    boolean isNeedClearLogFile();

    /**
     * 清空日志文件
     */
    void clearLogFile();

    /**
     * 标记清空日志
     */
    void markClearLog(boolean isClearLog);

    /**
     * 标记发送日志
     */
    void markSendLog(boolean isSendLog);
}
