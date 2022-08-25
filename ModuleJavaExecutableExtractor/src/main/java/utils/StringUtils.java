package utils;

import java.util.Objects;

public class StringUtils {

	public final static String concat(String ... args) {
		return concat(50, args);
	}
	
	public final static String concat(int stringLengthAverage, String ... args) {
		Objects.requireNonNull(args);
		if (stringLengthAverage <= 0) {
			throw new IllegalArgumentException("stringLengthAverage must be greater than zero.");
		}
		
		StringBuilder sb = new StringBuilder(args.length * stringLengthAverage);
		
		for (int i = 0; i < args.length; i++) {
			sb.append(args[i]);
		}
		
		return sb.toString();
	}
	
	public final static boolean isNullOrEmpty(String arg) {
		return arg == null || arg.isEmpty();
	}
	
	public final static boolean isNullOrEmptyOrWhiteSpaces(String arg) {
		return arg == null || arg.trim().isEmpty();
	}
	
	public final static boolean isNotNullOrEmptyOrWhiteSpaces(String arg) {
		return arg != null && !arg.trim().isEmpty();
	}
}
