package id.menkiplugcore.cdrgppatrol.util;

import java.util.Locale;

public final class DurationParser {
    private DurationParser() {
    }

    public static Integer parseDays(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }

        String value = input.trim().toLowerCase(Locale.ROOT);
        long multiplier = 1L;

        if (value.endsWith("days")) {
            value = value.substring(0, value.length() - 4).trim();
        } else if (value.endsWith("day")) {
            value = value.substring(0, value.length() - 3).trim();
        } else if (value.endsWith("d")) {
            value = value.substring(0, value.length() - 1).trim();
        } else if (value.endsWith("weeks")) {
            multiplier = 7L;
            value = value.substring(0, value.length() - 5).trim();
        } else if (value.endsWith("week")) {
            multiplier = 7L;
            value = value.substring(0, value.length() - 4).trim();
        } else if (value.endsWith("w")) {
            multiplier = 7L;
            value = value.substring(0, value.length() - 1).trim();
        }

        try {
            long amount = Long.parseLong(value);
            long days = Math.multiplyExact(amount, multiplier);
            if (days < 1L || days > 3650L) {
                return null;
            }
            return (int) days;
        } catch (NumberFormatException | ArithmeticException ignored) {
            return null;
        }
    }
}
