package lincolntheatre.events.parser.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for DateParser.
 * Verifies date parsing with multiple format strategies and ISO datetime parsing.
 */
class DateParserTest {

    private DateParser parser;

    @Test
    void parseIsoDateTime_acrossMidnight_returnsCorrectDate() {
        // 11 PM on Dec 30 EST (UTC-5) is still Dec 30
        LocalDate result = parser.parseIsoDateTime("2025-12-30T23:00:00-0500");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 30));
    }

    // Tests for parse() method

    @Test
    void parseIsoDateTime_withBlankString_returnsNull() {
        LocalDate result = parser.parseIsoDateTime("   ");

        assertThat(result).isNull();
    }

    @Test
    void parseIsoDateTime_withDifferentTimezone_extractsCorrectLocalDate() {
        // The local date component should be extracted regardless of timezone
        LocalDate result = parser.parseIsoDateTime("2025-12-30T20:00:00+0900");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 30));
    }

    @Test
    void parseIsoDateTime_withEmptyString_returnsNull() {
        LocalDate result = parser.parseIsoDateTime("");

        assertThat(result).isNull();
    }

    @Test
    void parseIsoDateTime_withInvalidFormat_returnsNull() {
        LocalDate result = parser.parseIsoDateTime("not a date");

        assertThat(result).isNull();
    }

    @Test
    void parseIsoDateTime_withLocalDateTime_returnsLocalDate() {
        LocalDate result = parser.parseIsoDateTime("2025-12-30T20:00:00");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 30));
    }

    @Test
    void parseIsoDateTime_withLocalDate_returnsLocalDate() {
        LocalDate result = parser.parseIsoDateTime("2025-12-30");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 30));
    }

    @Test
    void parseIsoDateTime_withMidnightTime_returnsLocalDate() {
        LocalDate result = parser.parseIsoDateTime("2025-12-30T00:00:00-0500");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 30));
    }

    @Test
    void parseIsoDateTime_withNoonTime_returnsLocalDate() {
        LocalDate result = parser.parseIsoDateTime("2025-12-30T12:00:00-0500");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 30));
    }

    @Test
    void parseIsoDateTime_withNull_returnsNull() {
        LocalDate result = parser.parseIsoDateTime(null);

        assertThat(result).isNull();
    }

    @Test
    void parseIsoDateTime_withOffsetNoColon_returnsLocalDate() {
        LocalDate result = parser.parseIsoDateTime("2025-12-30T20:00:00-0500");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 30));
    }

    @Test
    void parseIsoDateTime_withOffsetWithColon_returnsLocalDate() {
        LocalDate result = parser.parseIsoDateTime("2025-12-30T20:00:00-05:00");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 30));
    }

    @Test
    void parseIsoDateTime_withPositiveOffsetNoColon_returnsLocalDate() {
        LocalDate result = parser.parseIsoDateTime("2025-12-30T20:00:00+0500");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 30));
    }

    @Test
    void parseIsoDateTime_withPositiveOffsetWithColon_returnsLocalDate() {
        LocalDate result = parser.parseIsoDateTime("2025-12-30T20:00:00+05:00");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 30));
    }

    @Test
    void parseIsoDateTime_withSecondsAndMillis_returnsLocalDate() {
        LocalDate result = parser.parseIsoDateTime("2025-12-30T20:30:45");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 30));
    }

    @Test
    void parseIsoDateTime_withWhitespace_trimsAndParses() {
        LocalDate result = parser.parseIsoDateTime("  2025-12-30T20:00:00-0500  ");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 30));
    }

    @Test
    void parseIsoDateTime_withZuluTime_returnsLocalDate() {
        LocalDate result = parser.parseIsoDateTime("2025-12-30T20:00:00Z");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 30));
    }

    // Tests for parseIsoDateTime() method

    @Test
    void parse_withAbbreviatedMonthDoubleDigitDay_returnsLocalDate() {
        LocalDate result = parser.parse("Dec 15, 2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 15));
    }

    @Test
    void parse_withAbbreviatedMonthSingleDigitDay_returnsLocalDate() {
        LocalDate result = parser.parse("Dec 5, 2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 5));
    }

    @Test
    void parse_withBlankString_returnsNull() {
        LocalDate result = parser.parse("   ");

        assertThat(result).isNull();
    }

    @Test
    void parse_withDoubleDigitSlashFormat_returnsLocalDate() {
        LocalDate result = parser.parse("12/15/2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 15));
    }

    @Test
    void parse_withEmptyString_returnsNull() {
        LocalDate result = parser.parse("");

        assertThat(result).isNull();
    }

    @Test
    void parse_withFullMonthDoubleDigitDay_returnsLocalDate() {
        LocalDate result = parser.parse("December 15, 2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 15));
    }

    @Test
    void parse_withFullMonthSingleDigitDay_returnsLocalDate() {
        LocalDate result = parser.parse("December 5, 2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 5));
    }

    @Test
    void parse_withInvalidFormat_returnsNull() {
        LocalDate result = parser.parse("not a date");

        assertThat(result).isNull();
    }

    @Test
    void parse_withIsoFormat_returnsLocalDate() {
        LocalDate result = parser.parse("2025-12-15");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 15));
    }

    @Test
    void parse_withIsoLocalDateFormatter_returnsLocalDate() {
        LocalDate result = parser.parse("2025-12-15");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 15));
    }

    @Test
    void parse_withLeadingWhitespace_trimsAndParses() {
        LocalDate result = parser.parse("  12/15/2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 15));
    }

    @Test
    void parse_withNull_returnsNull() {
        LocalDate result = parser.parse(null);

        assertThat(result).isNull();
    }

    @Test
    void parse_withRfc1123Format_returnsLocalDate() {
        LocalDate result = parser.parse("Mon, 15 Dec 2025 10:00:00 GMT");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 15));
    }

    @Test
    void parse_withSingleDigitSlashFormat_returnsLocalDate() {
        LocalDate result = parser.parse("1/5/2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 1, 5));
    }

    @Test
    void parse_withTrailingWhitespace_trimsAndParses() {
        LocalDate result = parser.parse("12/15/2025  ");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 15));
    }

    @Test
    void parse_withWhitespace_trimsAndParses() {
        LocalDate result = parser.parse("  December 15, 2025  ");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 15));
    }

    @BeforeEach
    void setUp() {
        parser = new DateParser();
    }
}
