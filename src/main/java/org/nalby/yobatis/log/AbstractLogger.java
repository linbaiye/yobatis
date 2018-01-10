package org.nalby.yobatis.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class AbstractLogger implements Logger {

	private String className;
	
	public static enum LogLevel {
		DEBUG,
		INFO,
		ERROR
	}
	
	public static LogLevel defaultLevel = LogLevel.INFO;

	
	public AbstractLogger(String className) {
		this.className = className;
	}

	abstract protected void wirteToConsole(String msg);
	
	private String formatArgs (String format, Object ... args) {
		if (format == null) {
			return "";
		}
		if (args == null || args.length == 0) {
			return format;
		}
		Object[] strings = new String[args.length];
		for (int i = 0; i < args.length; i++) {
			Object arg = args[i];
			if (arg instanceof String) {
				strings[i] = (String)arg;
			} else if (arg instanceof Exception) {
				Exception exception = (Exception) arg;
				String tmp = exception.getMessage();
				try (StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);) {
					exception.printStackTrace(pw);
					tmp = sw.toString();
				} catch (Exception e) {
				}
				strings[i] = tmp;
			} else {
				strings[i] = arg.toString();
			}
		}
		for (int i = 0; i < args.length; i++) {
			if (format.contains("{}")) {
				format = format.replaceFirst("\\{\\}", "%s");
			} else {
				break;
			}
		}
		return String.format(format, strings);
	}

	
	private void formatAndWrite(String level, String format, Object ... args) {
		String tmp = formatArgs(format, args);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm:ss");
		String timestamp = simpleDateFormat.format(new Date());
		String msg =  timestamp + " " + level + " " + className + " - " + tmp + "\n";
		wirteToConsole(msg);
	}


	@Override
	public void info(String format, Object... args) {
		formatAndWrite("INFO ", format, args);
	}
	
	@Override
	public void error(String format, Object... args) {
		formatAndWrite("ERROR", format, args);
	}
	@Override
	public void debug(String format, Object... args) {
		if (defaultLevel == LogLevel.DEBUG) {
			formatAndWrite("DEBUG", format, args);
		}
	}
}
