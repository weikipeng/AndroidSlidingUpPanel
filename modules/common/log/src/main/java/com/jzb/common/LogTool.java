package com.jzb.common;

public class LogTool {
    public static final String TAG = "pjwdev";
    public static boolean FLAG = true;
    private static LogTool sDevLogTool;
    private ILogDelegate mDelegate;
    private boolean isPrintLog = true;

    private LogTool() {

    }

    public static LogTool getInstance() {
        if (sDevLogTool == null) {
            sDevLogTool = new LogTool();
        }
        return sDevLogTool;
    }

    /**
     * 主要实现工具
     */
    public static LogInfo printLog(ILogDelegate delegate, boolean isPrintLog, StackTraceElement[] stackTrace
            , String log, Object... messages) {
        LogInfo logInfo = null;

        //-
        if (stackTrace != null) {
            String fullClassName = stackTrace[3].getClassName();
            String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
            int lineNumber = stackTrace[3].getLineNumber();

            StringBuffer codeMessageBuffer = new StringBuffer("");
            if (stackTrace.length > 5) {
                String supClassName = stackTrace[4].getClassName();
                supClassName = supClassName.substring(supClassName.lastIndexOf(".") + 1);

                int supLinenumber = stackTrace[4].getLineNumber();
                String methodName = stackTrace[4].getMethodName();

//                codeMessage += supClassName + "." + methodName + ":" + supLinenumber + "==>";
                codeMessageBuffer.append(supClassName)
                        .append(".")
                        .append(methodName)
                        .append(":")
                        .append(supLinenumber)
                        .append("==>");
            }

            String processName = "";
            if (delegate != null) {
                processName = delegate.getProcessName();
                if (isValidString(processName)) {
                    processName += ":进程==>";
                }
            }


//            codeMessage += "(" + className + ":" + lineNumber + "@"
//                    + processName
//                    + Thread.currentThread().getName()
//                    + ") : ";

            codeMessageBuffer.append("(")
                    .append(className)
                    .append(":")
                    .append(lineNumber)
                    .append("@");

            if (isValidString(processName)) {
                codeMessageBuffer.append(processName);
            }

            codeMessageBuffer.append(Thread.currentThread().getName())
                    .append(") : ");

            ///////////////////////////////////////////////////////////////////////////
            //
            ///////////////////////////////////////////////////////////////////////////


            if (messages != null && messages.length > 0) {
                StringBuilder logBuilder = new StringBuilder(log);
                for (Object message : messages) {
                    logBuilder.append(message);
                }
                log = logBuilder.toString();
            }


            ///////////////////////////////////////////////////////////////////////////
            //
            ///////////////////////////////////////////////////////////////////////////
            String tag = TAG;

            StringBuilder printLogStringBuilder = new StringBuilder();
            printLogStringBuilder.append("pjwFocus debug ");
            if (isValidString(className)) {
                printLogStringBuilder.append(className);
            }
            printLogStringBuilder.append("[");
            printLogStringBuilder.append(codeMessageBuffer);
            printLogStringBuilder.append("]");

            String printLogText = String.format("%-222s %s", log, printLogStringBuilder);

            if (isPrintLog && delegate != null) {
                delegate.executeLog(tag, printLogText);
            }

            logInfo = new LogInfo();
            logInfo.callStackMessage = codeMessageBuffer.toString();
            logInfo.userLogMessage = printLogText;
            logInfo.className = className;
        }

        //--
        return logInfo;
    }

    public static boolean isValidString(String stringText) {
//        processName != null && !processName.trim().equals("")
        return stringText != null && !"".equals(stringText.trim());
    }

    public LogTool setDelegate(ILogDelegate delegate) {
        mDelegate = delegate;
        return this;
    }

    public synchronized void saveLog(String log) {
        if (!FLAG) {
            return;
        }

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        printLog(mDelegate, FLAG, stackTrace, log, "");
    }

    public LogTool setDebugMode(boolean isDebugMode) {
        isPrintLog = isDebugMode;
        return this;
    }

    public synchronized void s(String log, Object... messages) {
        if (!FLAG) {
            return;
        }
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        printLog(mDelegate, FLAG, stackTrace, log, messages);
    }

    public synchronized void s(int spaceLineNum, String log, Object... messages) {
        if (!FLAG) {
            return;
        }

        StringBuilder logBuilder = new StringBuilder(log);

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
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
            for (Object message : messages) {
                logBuilder.append(message);
            }
        }

        logBuilder.insert(0, codeMessage);
        logBuilder.insert(0, "pjwFocus debug ");
        //        logBuilder.insert(0, "      ");
        //        logBuilder.insert(0, className);

        if (mDelegate != null) {
            String tagName = className;
            if ("".equals(tagName)) {
                tagName = "jzb";
            }
            if (spaceLineNum > 0) {
                for (int i = 0; i < spaceLineNum; i++) {
                    mDelegate.executeLog(tagName, "pjwFocus debug \n");
                }
            }
            mDelegate.executeLog(tagName, logBuilder.toString());
        }
    }

    public String getCurrentMethodName() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length > 3) {
            return stackTrace[3].getMethodName();
        }

        return "";
    }
}
