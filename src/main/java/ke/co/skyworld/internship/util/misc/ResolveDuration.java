package ke.co.skyworld.internship.util.misc;

import ke.co.skyworld.internship.util.logging.Log;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class ResolveDuration {

    private enum DurationUnit {
        SECONDS("seconds", "second", "sec", "s"),
        MINUTES("minutes", "minute", "min", "m"),
        HOURS("hours", "hour", "hr", "h"),
        DAYS("days", "day", "d");

        private final List<String> aliases;

        DurationUnit(String... aliases) {
            this.aliases = Arrays.asList(aliases);
        }

        // Helper to match the string to the correct enum constant
        public static DurationUnit fromString(String unit) {
            String normalizedUnit = unit.trim().toLowerCase();
            for (DurationUnit durationUnit : values()) {
                if (durationUnit.aliases.contains(normalizedUnit)) {
                    return durationUnit;
                }
            }
            throw new IllegalArgumentException("Unsupported duration value unit: " + unit);
        }
    }

    public static Duration createDuration(int value, String unit) {
        if (value <= 0) {
            throw new IllegalArgumentException(
                    "Duration value must be greater than zero"
            );
        }

        if (unit == null || unit.isBlank()) {
            throw new IllegalArgumentException(
                    "Duration timeout unit cannot be null or blank"
            );
        }

        // 1. Resolve the string to our strict Enum
        DurationUnit parsedUnit = DurationUnit.fromString(unit);

        // 2. Switch on the Enum directly
        return switch (parsedUnit) {
            case SECONDS -> Duration.ofSeconds(value);
            case MINUTES -> Duration.ofMinutes(value);
            case HOURS   -> Duration.ofHours(value);
            case DAYS    -> Duration.ofDays(value);
        };
    }
}