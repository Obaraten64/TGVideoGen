package com.burmaldabot.telegram.keyboard;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class KeyboardTemplateEngine {

    private static final Pattern NUMERIC_COMPARE_PATTERN = Pattern.compile("^(\\w+)\\s*(>|>=|<|<=|==|!=)\\s*(\\d+)$");

    /**
     * Evaluates text placeholders such as {count} or {name} using provided parameters.
     */
    public String evaluateText(String text, Map<String, Object> params) {
        if (text == null || text.isBlank() || params == null || params.isEmpty()) {
            return text;
        }

        String result = text;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            if (result.contains(placeholder)) {
                result = result.replace(placeholder, String.valueOf(entry.getValue()));
            }
        }
        return result;
    }

    /**
     * Evaluates a condition string against parameters:
     * - null or empty -> true (button is visible)
     * - "count > 0", "count >= 1" -> numeric comparison
     * - "!flag" -> negated boolean check
     * - "flag" -> boolean truthy check
     */
    public boolean evaluateCondition(String condition, Map<String, Object> params) {
        if (condition == null || condition.isBlank()) {
            return true;
        }

        String trimmed = condition.trim();
        if (params == null || params.isEmpty()) {
            return false;
        }

        // Numeric comparison (e.g. "count > 0")
        Matcher matcher = NUMERIC_COMPARE_PATTERN.matcher(trimmed);
        if (matcher.matches()) {
            String key = matcher.group(1);
            String operator = matcher.group(2);
            int threshold = Integer.parseInt(matcher.group(3));

            Object val = params.get(key);
            if (!(val instanceof Number number)) {
                return false;
            }

            int actual = number.intValue();
            return switch (operator) {
                case ">" -> actual > threshold;
                case ">=" -> actual >= threshold;
                case "<" -> actual < threshold;
                case "<=" -> actual <= threshold;
                case "==" -> actual == threshold;
                case "!=" -> actual != threshold;
                default -> false;
            };
        }

        // Inverted boolean flag check (e.g. "!used")
        if (trimmed.startsWith("!")) {
            String key = trimmed.substring(1).trim();
            return !isTruthy(params.get(key));
        }

        // Direct boolean flag check (e.g. "freeTrialAvailable")
        return isTruthy(params.get(trimmed));
    }

    private boolean isTruthy(Object val) {
        if (val == null) {
            return false;
        }
        if (val instanceof Boolean b) {
            return b;
        }
        if (val instanceof Number n) {
            return n.intValue() != 0;
        }
        return Boolean.parseBoolean(String.valueOf(val));
    }
}
