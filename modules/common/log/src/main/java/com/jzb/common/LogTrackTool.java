package com.jzb.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.jzb.common.LogTool.TAG;

/**
 * Created by wikipeng on 2017/9/22.
 */
public class LogTrackTool {
    public static final String PATTERN_DETAIL = "yyyy-MM-dd HH:mm:ss";
    private static LogTrackTool sDevLogTool;
    private File sLogFile;
    private boolean isPrintPath = true;
    private boolean isPrintLog;
    private ILogTrackDelegate mDelegate;

    private String lastClassName;

    public static void saveThrowable(Throwable throwable) {
        LogTrackTool logTrackTool = newInstance();
        logTrackTool.setLogFile("logWatch");
        logTrackTool.t("======清除上报日志======");

        if (throwable != null) {
            try {
                File defaultLogFile = LogTrackTool.getInstance().getDefaultLogFile();
                if (defaultLogFile != null) {
                    FileWriter fileWriter = new FileWriter(defaultLogFile, true);
                    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter, 4096);
                    PrintWriter printWriter = new PrintWriter(bufferedWriter);

                    //新起一行
                    printWriter.println();
                    printWriter.println("=======崩溃信息pjwFocus=======");

                    throwable.printStackTrace(printWriter);

                    fileWriter.flush();
                    bufferedWriter.flush();
                    printWriter.flush();

                    printWriter.close();
                    bufferedWriter.close();
                    fileWriter.close();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static LogTrackTool newInstance() {
        LogTrackTool result = null;
        synchronized (LogTrackTool.class) {
            result = new LogTrackTool();
        }
        return result;
    }

    public LogTrackTool setLogFile(String tempFileName) {
        if (mDelegate != null) {
            this.sLogFile = mDelegate.generateLogFile(tempFileName);
        }

        return this;
    }

    /**
     * 跟踪用户操作
     */
    public synchronized void t(String log, Object... messages) {
        if (sLogFile == null && mDelegate != null) {
            sLogFile = mDelegate.getDefaultLogFile();
        }

        if (sLogFile == null) {
            return;
        }

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        printAndWriteToFile(sLogFile, stackTrace, log, messages);
    }

    public File getDefaultLogFile() {
        if (mDelegate != null) {
            return mDelegate.getDefaultLogFile();
        }
        return null;
    }

    public static LogTrackTool getInstance() {
        synchronized (LogTrackTool.class) {
            if (sDevLogTool == null) {
                sDevLogTool = new LogTrackTool();
            }
        }

        return sDevLogTool;
    }

    /**
     * 打印并保存用户日志
     */
    private synchronized void printAndWriteToFile(File logFile, StackTraceElement[] stackTrace, String log, Object... messages) {
        LogInfo logInfo = LogTool.printLog(mDelegate, isPrintLog, stackTrace, log, messages);
        if (logInfo != null) {
            writeToFile(logFile, logInfo.className, logInfo.callStackMessage, logInfo.userLogMessage);
        }
    }

    /**
     * 跟踪用户操作
     */
    private synchronized void writeToFile(File logFile, String className, String codeMessage, String log) {
        ///////////////////////////////////////////////////////////////////////////
        //
        ///////////////////////////////////////////////////////////////////////////
        if (mDelegate == null) {
            return;
        }

        if (logFile == null) {
            logFile = mDelegate.getDefaultLogFile();
        }

        if (logFile == null) {
            return;
        }

        try {
            if (!logFile.exists() || logFile.isDirectory()) {
                logFile.getParentFile().mkdirs();
                logFile.createNewFile();
            }

            FileWriter fileWriter = new FileWriter(logFile, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            String timeStampText = getDetailTimeText(System.currentTimeMillis());

            if (lastClassName != null && !lastClassName.equals(className)) {
                bufferedWriter.newLine();
            }
            lastClassName = className;

            bufferedWriter.newLine();
            bufferedWriter.append("======");
            bufferedWriter.newLine();
            bufferedWriter.append(timeStampText);
            bufferedWriter.append(":");
            bufferedWriter.append(codeMessage);
            bufferedWriter.append("     lastClassName:");
            bufferedWriter.append(lastClassName);
            bufferedWriter.append("     className:");
            bufferedWriter.append(className);
            bufferedWriter.newLine();
            bufferedWriter.append(log);


            bufferedWriter.flush();
            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getDetailTimeText(long time) {
        SimpleDateFormat format = new SimpleDateFormat(PATTERN_DETAIL, Locale.getDefault());
        return format.format(new Date(time));
    }

    public synchronized void clearLog() {
        if (mDelegate == null) {
            return;
        }
        File defaultLogFile = mDelegate.getDefaultLogFile();
        if (defaultLogFile != null) {
            defaultLogFile.delete();
        }
    }

    protected synchronized void printLog(boolean isSaveLog, StackTraceElement[] stackTrace, String log, Object... messages) {
        if (stackTrace == null) {
            return;
        }

        String fullClassName = stackTrace[3].getClassName();
        String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
        int lineNumber = stackTrace[3].getLineNumber();

        String codeMessage = "";
        if (stackTrace.length > 5) {
            String supClassName = stackTrace[4].getClassName();
            supClassName = supClassName.substring(supClassName.lastIndexOf(".") + 1);

            int supLinenumber = stackTrace[4].getLineNumber();
            String methodName = stackTrace[4].getMethodName();

            codeMessage += supClassName + "." + methodName + ":" + supLinenumber + "==>";
        }

        codeMessage += "(" + className + ":" + lineNumber + "@" + Thread.currentThread().getName() + ") : ";

        if (messages != null && messages.length > 0) {
            StringBuilder logBuilder = new StringBuilder(log);
            for (Object message : messages) {
                logBuilder.append(message);
            }
            log = logBuilder.toString();
        }

        if (isPrintLog && mDelegate != null) {
            String tag = TAG;

            StringBuilder printLogStringBuilder = new StringBuilder();
            printLogStringBuilder.append("pjwFocus debug ");
            if (isValidString(className)) {
                printLogStringBuilder.append(className);
            }
            printLogStringBuilder.append("[");
            printLogStringBuilder.append(codeMessage);
            printLogStringBuilder.append("]");

            String printLogText = String.format("%-222s %s", log, printLogStringBuilder);

            mDelegate.executeLog(tag, printLogText);
        }

        if (isSaveLog) {
            writeToFile(null, className, codeMessage, log);
        }
    }

    protected boolean isValidString(String stringText) {
        return stringText != null && !"".equals(stringText);
    }

    /**
     * 不打印保存崩溃日志
     */
    public synchronized void silentLog(String log, Object... messages) {
        if (sLogFile == null && mDelegate != null) {
            sLogFile = mDelegate.getDefaultLogFile();
        }

        if (sLogFile == null) {
            return;
        }

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        boolean tempIsPrintLog = isPrintLog;
        this.isPrintLog = false;
        printAndWriteToFile(sLogFile, stackTrace, log, messages);
        this.isPrintLog = tempIsPrintLog;
    }

    /**
     * 跟踪用户操作
     */
    public synchronized void s(Object classObject, Object... messages) {
        if (sLogFile == null && mDelegate != null) {
            sLogFile = mDelegate.getDefaultLogFile();
        }

        if (sLogFile == null) {
            return;
        }

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        String log = null;
        if (classObject != null) {
            log = String.format("%-64s %s", classObject.getClass().getSimpleName(), "==>");
        }

        printAndWriteToFile(sLogFile, stackTrace, log, messages);
    }

    /**
     * 跟踪用户操作
     */
    public synchronized void f(File tempLogFile, String log, Object... messages) {
        if (tempLogFile == null) {
            return;
        }

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        printAndWriteToFile(tempLogFile, stackTrace, log, messages);
    }

    public LogTrackTool setDebugMode(boolean isDebugMode) {
        isPrintLog = isDebugMode;
        return this;
    }

    public LogTrackTool setLogFile(File tempLogFile) {
        this.sLogFile = tempLogFile;
        return this;
    }

    public LogTrackTool setDelegate(ILogTrackDelegate delegate) {
        this.mDelegate = delegate;
        return this;
    }

    public void build() {

    }

    public void resetDefaultLogFile() {
        if (mDelegate != null) {
            this.sLogFile = mDelegate.getDefaultLogFile();
        }
    }

    public void handleLogAction() {
        LogTool.getInstance().saveLog("调用 handleLogAction===>");
        if (mDelegate.isNeedSendLog()) {
            mDelegate.sendLogToServer();
            mDelegate.markSendLog(false);
        }

        if (mDelegate.isNeedClearLogFile()) {
            mDelegate.clearLogFile();
            mDelegate.markClearLog(false);
        }
    }
}
