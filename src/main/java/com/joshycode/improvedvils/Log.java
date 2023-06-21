package com.joshycode.improvedvils;

import org.apache.logging.log4j.Level;

public class Log {

	private static final Throwable stackInfo = new Throwable();

	private static String getLogLocation(Throwable t) {
		final StackTraceElement[] stack = t.getStackTrace();
		if (stack.length < 2) return "";
		final StackTraceElement caller = stack[1];
		return caller.getClassName() + "." + caller.getMethodName() + "(" + caller.getFileName() + ":" + caller.getLineNumber() + "): ";
	}

	private static void logWithCaller(Throwable callerStack, Level level, String format, Object... data) {
		ImprovedVils.logger.log(level, getLogLocation(callerStack) + String.format(format, data));
	}

	public static void warn(String format, Object ... data)
	{
		logWithCaller(stackInfo.fillInStackTrace(), Level.WARN, format, data);
	}

	public static void info(String format, Object ... data)
	{
		logWithCaller(stackInfo.fillInStackTrace(), Level.INFO, format, data);
	}
}
