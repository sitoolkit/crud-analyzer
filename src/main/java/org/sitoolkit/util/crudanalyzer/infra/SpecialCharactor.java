package org.sitoolkit.util.crudanalyzer.infra;

import org.apache.commons.lang3.StringUtils;

public enum SpecialCharactor {
	CSV_DOUBLE_QUATE("\"", "\"\""),
	;

	private String regex;

	private String replacement;

	private SpecialCharactor(String regex, String replacement) {
		this.regex = regex;
		this.replacement = replacement;
	}

	public String escape(String before) {
		return StringUtils.defaultString(before).replaceAll(regex, replacement);
	}

	public String unEscape(String before) {
		return StringUtils.defaultString(before).replaceAll(replacement, regex);
	}

	public String getRegex() {
		return regex;
	}

	public String getReplacement() {
		return replacement;
	}
}
