package com.example.btck.utils;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class InviteCodeUtils {

    private static final Pattern LABELED_CODE_PATTERN = Pattern.compile(
            "(?:code|ma|mã|join)[\\s:=/]+([A-Za-z0-9_-]{8,12})",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern EXACT_CODE_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{8,12}$");

    private InviteCodeUtils() {}

    public static String normalize(String rawInput) {
        if (rawInput == null) return "";

        String input = rawInput.trim();
        if (input.isEmpty()) return "";

        Matcher labeledMatcher = LABELED_CODE_PATTERN.matcher(input);
        String code = null;
        while (labeledMatcher.find()) {
            code = labeledMatcher.group(1);
        }

        if (code == null && EXACT_CODE_PATTERN.matcher(input).matches()) {
            code = input;
        }

        if (code == null) {
            String[] tokens = input.split("[^A-Za-z0-9_-]+");
            for (String token : tokens) {
                if (EXACT_CODE_PATTERN.matcher(token).matches()) {
                    code = token;
                }
            }
        }

        return code != null ? code.toUpperCase(Locale.US) : "";
    }
}
