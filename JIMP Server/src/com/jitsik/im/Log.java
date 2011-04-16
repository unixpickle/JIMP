package com.jitsik.im;

public class Log {

	// levels
	// error		0
	// warning		1
	// events		2
	// debug		3
	// all			4
	
	public static int debugLevel = 0;
	public static final int LEVEL_ERROR = 0;
	public static final int LEVEL_WARNING = 1;
	public static final int LEVEL_EVENTS = 2;
	public static final int LEVEL_DEBUG = 3;
	public static final int LEVEL_ALL = 4;
	
	public static void debugLevel (int level) {
		debugLevel = level;
	}
	
	public static void log (int level, String message) {
		if (debugLevel >= level) {
			String prompt = "[LOG]     ";
			if (level == LEVEL_ERROR) prompt = "[ERR]     ";
			else if (level == LEVEL_WARNING) prompt = "[WARNING] ";
			else if (level == LEVEL_EVENTS) prompt = "[EVENT]   ";
			else if (level == LEVEL_DEBUG) prompt = "[DEBUG]   ";
			else if (level == LEVEL_ALL) prompt = "[GENERAL] ";
			System.out.println(prompt + message);
		}
	}
	
}
