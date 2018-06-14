package com.jzb.common;

/**
 * Created by wikipeng on 2018/2/9.
 */
public interface ILogDelegate {
    void executeLog(String tag, String message);
    String getProcessName();
}
