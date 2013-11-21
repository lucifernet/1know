package tw.com.ischool.oneknow.util;

import android.text.TextUtils;

public class StringUtil {
	public static final String EMPTY = "";
	public static final String WHITESPACE = " ";

	public static Boolean isNullOrWhitespace(String string) {
		if (string == null)
			return true;

		if (string.equalsIgnoreCase("null"))
			return true;

		String trimed = string.trim();
		return TextUtils.isEmpty(trimed);
	}

	public static String getExceptionMessage(Throwable ex) {
		StringBuilder sb = new StringBuilder(ex.getClass().getSimpleName())
				.append(":");
		if (ex.getMessage() != null) {
			sb.append(ex.getMessage()).append("\n");
		}

		for (StackTraceElement element : ex.getStackTrace()) {
			sb.append(element.toString()).append("-")
					.append(element.getLineNumber()).append("\n");
		}

		if (ex.getCause() != null) {
			String inner = getExceptionMessage(ex.getCause());
			sb.append(inner);
		}

		sb.append("\n");
		return sb.toString();
	}
}
