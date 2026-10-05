package lincolntheatre.events.parser.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Locale;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Centralized date parsing utility
 */
public final class DateParser {

    private static final DateTimeFormatter[] DATE_FORMATTERS = {
        DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("M/d/yyyy", Locale.US),
        DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.US),
        DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH),
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.RFC_1123_DATE_TIME
    };
    // Pattern to match and remove day of week prefix (e.g., "Thursday, " or "Saturday, ")
    private static final Pattern DAY_OF_WEEK_PREFIX =
        Pattern.compile("^(Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday),\\s*",
            Pattern.CASE_INSENSITIVE);
    // Formatter for ISO datetime with timezone offset without colon (e.g., -0500)
    private static final DateTimeFormatter ISO_OFFSET_NO_COLON =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH);
    private static final Logger LOG = LoggerFactory.getLogger(DateParser.class);
    // Formatters for dates without year - will use current year as default
    private static final DateTimeFormatter[] NO_YEAR_FORMATTERS = {
        new DateTimeFormatterBuilder()
            .appendPattern("MMMM d")
            .parseDefaulting(ChronoField.YEAR, LocalDate.now().getYear())
            .toFormatter(Locale.ENGLISH),
        new DateTimeFormatterBuilder()
            .appendPattern("MMMM dd")
            .parseDefaulting(ChronoField.YEAR, LocalDate.now().getYear())
            .toFormatter(Locale.ENGLISH),
        new DateTimeFormatterBuilder()
            .appendPattern("MMM d")
            .parseDefaulting(ChronoField.YEAR, LocalDate.now().getYear())
            .toFormatter(Locale.ENGLISH),
        new DateTimeFormatterBuilder()
            .appendPattern("MMM dd")
            .parseDefaulting(ChronoField.YEAR, LocalDate.now().getYear())
            .toFormatter(Locale.ENGLISH)
    };

    /**
     * Parses a date string using multiple format strategies.
     *
     * @param dateStr the date string to parse
     * @return the parsed LocalDate, or null if parsing fails
     */
    public LocalDate parse(final String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }

        String trimmed = dateStr.trim();

        // Try standard formatters with year first
        for (final DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(trimmed, formatter);
            } catch (final DateTimeParseException e) {
                // Try next formatter
            }
        }

        // Remove day of week prefix if present (e.g., "Thursday, January 22" -> "January 22")
        trimmed = DAY_OF_WEEK_PREFIX.matcher(trimmed).replaceFirst("");

        // Try formatters that don't require a year
        for (final DateTimeFormatter formatter : NO_YEAR_FORMATTERS) {
            try {
                LocalDate parsed = LocalDate.parse(trimmed, formatter);
                // If the parsed date is in the past, assume it's for next year
                if (parsed.isBefore(LocalDate.now().minusMonths(1))) {
                    parsed = parsed.plusYears(1);
                }
                return parsed;
            } catch (final DateTimeParseException e) {
                // Try next formatter
            }
        }

        LOG.warn("Failed to parse date: {}", dateStr);
        return null;
    }

    /**
     * Parses ISO 8601 datetime strings to LocalDate.
     * Handles various ISO formats including OffsetDateTime, LocalDateTime, and LocalDate.
     * Supports timezone offsets with and without colons (e.g., -0500 or -05:00).
     *
     * @param dateTimeStr the ISO datetime string to parse
     * @return the parsed LocalDate, or null if parsing fails
     */
    public LocalDate parseIsoDateTime(final String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isBlank()) {
            return null;
        }

        final String trimmed = dateTimeStr.trim();

        // Try offset without colon first (e.g., 2025-12-30T20:00:00-0500)
        try {
            return OffsetDateTime.parse(trimmed, ISO_OFFSET_NO_COLON).toLocalDate();
        } catch (DateTimeParseException e) {
            // Try standard ISO OffsetDateTime (with colon: -05:00)
            try {
                return OffsetDateTime.parse(trimmed).toLocalDate();
            } catch (DateTimeParseException e2) {
                // Try parsing as LocalDateTime
                try {
                    return LocalDateTime.parse(trimmed).toLocalDate();
                } catch (DateTimeParseException e3) {
                    // Try parsing as LocalDate
                    try {
                        return LocalDate.parse(trimmed);
                    } catch (DateTimeParseException e4) {
                        LOG.warn("Could not parse ISO date: {}", trimmed);
                        return null;
                    }
                }
            }
        }
    }
}
