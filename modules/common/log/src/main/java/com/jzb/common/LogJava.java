package com.jzb.common;

import java.io.File;

public class LogJava implements ILogTrackDelegate {
    private static LogJava sLogJava;

    private LogJava() {

    }

    public void init() {
        LogTrackTool.getInstance()
                .setDelegate(this)
                .setDebugMode(true);

        LogTool.getInstance().setDelegate(this)
                .setDebugMode(true);
    }

    @Override
    public File getDefaultLogFile() {
        return null;
    }

    @Override
    public File generateLogFile(String fileName) {
        return null;
    }

    @Override
    public boolean isNeedSendLog() {
        return false;
    }

    @Override
    public void sendLogToServer() {

    }

    @Override
    public boolean isNeedClearLogFile() {
        return false;
    }

    @Override
    public void clearLogFile() {

    }

    @Override
    public void markClearLog(boolean isClearLog) {

    }

    @Override
    public void markSendLog(boolean isSendLog) {

    }

    @Override
    public void executeLog(String tag, String message) {
        System.out.println("[" + tag + "]===" + message);
    }

    @Override
    public String getProcessName() {
        return null;
    }

    public static LogJava getInstance() {
        synchronized (LogJava.class) {
            if (sLogJava == null) {
                sLogJava = new LogJava();
            }
        }
        return sLogJava;
    }
}
