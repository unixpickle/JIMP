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
	
	/**
	 * Sets the debug level.  Any log of a level equal to or less
	 * than this will be printed to the console.
	 * @param level The level to use.
	 */
	public static void debugLevel (int level) {
		debugLevel = level;
	}
	
	/**
	 * Prints out a string with a certain prefix (level based.)
	 * This will do nothing if the debug level is a higher priority
	 * than the level passed to this function.
	 * @param level The level to use
	 * @param message The message to print
	 */
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
