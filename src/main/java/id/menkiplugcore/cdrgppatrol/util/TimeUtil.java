package id.menkiplugcore.cdrgppatrol.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class TimeUtil {
    private static final DateTimeFormatter ABSOLUTE = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")
            .withZone(ZoneId.systemDefault());

    private TimeUtil() {
    }

    public static String formatAbsolute(long millis) {
        if (millis <= 0L) {
            return "Unknown";
        }
        return ABSOLUTE.format(Instant.ofEpochMilli(millis));
    }

    public static String formatRelativePast(long millis, long nowMillis) {
        if (millis <= 0L) {
            return "Unknown";
        }

        long delta = Math.max(0L, nowMillis - millis);
        long days = delta / 86_400_000L;
        long hours = (delta % 86_400_000L) / 3_600_000L;
        long minutes = (delta % 3_600_000L) / 60_000L;

        if (days > 0) {
            return days + "d " + hours + "h lalu";
        }
        if (hours > 0) {
            return hours + "h " + minutes + "m lalu";
        }
        return Math.max(1L, minutes) + "m lalu";
    }
}
