package lincolntheatre.events.parser.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive tests for DateExtractor.
 * Tests multi-strategy date extraction from meta tags, time elements, and date classes.
 */
class DateExtractorTest {

    private DateExtractor extractor;

    @Test
    void extractDate_withCapitalLocalDateClass_parsesCorrectly() {
        String html = """
            <html>
            <body>
                <div class="eventDate">December 15, 2025</div>
            </body>
            </html>
            """;
        Document doc = Jsoup.parse(html);

        LocalDate result = extractor.extractLocalDate(doc);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(LocalDate.of(2025, Month.DECEMBER, 15));
    }

    // Tests for meta property extraction

    @Test
    void extractDate_withDateClass_returnsLocalDate() {
        String html = """
            <html>
            <body>
                <div class="event-date">December 15, 2025</div>
            </body>
            </html>
            """;
        Document doc = Jsoup.parse(html);

        LocalDate result = extractor.extractLocalDate(doc);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(LocalDate.of(2025, Month.DECEMBER, 15));
    }

    @Test
    void extractDate_withEmptyTimeElement_fallsBackToLocalDateClass() {
        String html = """
            <html>
            <body>
                <time datetime=""></time>
                <div class="date">2025-12-15</div>
            </body>
            </html>
            """;
        Document doc = Jsoup.parse(html);

        LocalDate result = extractor.extractLocalDate(doc);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(LocalDate.of(2025, Month.DECEMBER, 15));
    }

    // Tests for time element extraction

    @Test
    void extractDate_withInvalidDateInTimeElement_fallsBackToLocalDateClass() {
        String html = """
            <html>
            <body>
                <time datetime="invalid">Invalid</time>
                <div class="date">2025-12-15</div>
            </body>
            </html>
            """;
        Document doc = Jsoup.parse(html);

        LocalDate result = extractor.extractLocalDate(doc);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(LocalDate.of(2025, Month.DECEMBER, 15));
    }

    @Test
    void extractDate_withInvalidJsonLdStartLocalDate_fallsBackToMeta() {
        String html = """
            <html>
            <head>
                <script type="application/ld+json">
                {
                    "@type": "Event",
                    "startDate": "invalid-date"
                }
                </script>
                <meta property="event:start_date" content="2025-01-15"/>
            </head>
            <body></body>
            </html>
            """;
        Document doc = Jsoup.parse(html);

        LocalDate result = extractor.extractLocalDate(doc);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(LocalDate.of(2025, Month.JANUARY, 15));
    }

    @Test
    void extractDate_withJsonLdLocalDateTime_returnsLocalDate() {
        String html = """
            <html>
            <head>
                <script type="application/ld+json">
                {
                    "@type": "Event",
                    "startDate": "2025-12-30T20:00:00"
                }
                </script>
            </head>
            <body></body>
            </html>
            """;
        Document doc = Jsoup.parse(html);

        LocalDate result = extractor.extractLocalDate(doc);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(LocalDate.of(2025, Month.DECEMBER, 30));
    }

    // Tests for date class extraction

    @Test
    void extractDate_withJsonLdLocalDate_returnsLocalDate() {
        String html = """
            <html>
            <head>
                <script type="application/ld+json">
                {
                    "@type": "Event",
                    "startDate": "2025-12-30"
                }
                </script>
            </head>
            <body></body>
            </html>
            """;
        Document doc = Jsoup.parse(html);

        LocalDate result = extractor.extractLocalDate(doc);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(LocalDate.of(2025, Month.DECEMBER, 30));
    }

    @Test
    void extractDate_withJsonLdStartDateWithColon_returnsLocalDate() {
        String html = """
            <html>
            <head>
                <script type="application/ld+json">
                {
                    "@context": "https://schema.org",
                    "@type": "Event",
                    "startDate": "2025-12-30T20:00:00-05:00"
                }
                </script>
            </head>
            <body></body>
            </html>
            """;
        Document doc = Jsoup.parse(html);

        LocalDate result = extractor.extractLocalDate(doc);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(LocalDate.of(2025, Month.DECEMBER, 30));
    }

    @Test
    void extractDate_withJsonLdStartDate_returnsLocalDate() {
        String html = """
            <html>
            <head>
                <script type="application/ld+json">
                {
                    "@context": "https://schema.org",
                    "@type": "Event",
                    "name": "Test Event",
                    "startDate": "2025-12-30T20:00:00-0500"
                }
                </script>
            </head>
            <body></body>
            </html>
            """;
        Document doc = Jsoup.parse(html);

        LocalDate result = extractor.extractLocalDate(doc);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(LocalDate.of(2025, Month.DECEMBER, 30));
    }

    // Tests for strategy priority

    @Test
    void extractDate_withLocalDateClassIsoFormat_parsesCorrectly() {
        String html = """
            <html>
            <body>
                <div class="date">2025-12-15</div>
            </body>
            </html>
            """;
        Document doc = Jsoup.parse(html);

        LocalDate result = extractor.extractLocalDate(doc);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(LocalDate.of(2025, Month.DECEMBER, 15));
    }

    @Test
    void extractDate_withMetaAndTime_prefersMetaStartLocalDate() {
        String html = """
            <html>
            <head>
                <meta property="event:start_date" content="2025-12-15"/>
            </head>
            <body>
                <time datetime="2025-01-20">January 20, 2025</time>
            </body>
            </html>
            """;
        Document doc = Jsoup.parse(html);

        LocalDate result = extractor.extractLocalDate(doc);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(LocalDate.of(2025, Month.DECEMBER, 15));
    }

    // Tests for fallback and edge cases

    @Test
    void extractDate_withMetaStartDate_returnsLocalDate() {
        String html = """
            <html>
            <head>
                <meta property="event:start_date" content="2025-12-15"/>
            </head>
            <body></body>
            </html>
            """;
        Document doc = Jsoup.parse(html);

        LocalDate result = extractor.extractLocalDate(doc);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(LocalDate.of(2025, Month.DECEMBER, 15));
    }

    @Test
    void extractDate_withMetaStartLocalDateIsoFormat_parsesCorrectly() {
        String html = """
            <html>
            <head>
                <meta property="event:start_date" content="2025-01-20"/>
            </head>
            <body></body>
            </html>
            """;
        Document doc = Jsoup.parse(html);

        LocalDate result = extractor.extractLocalDate(doc);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(LocalDate.of(2025, Month.JANUARY, 20));
    }

    @Test
    void extractDate_withMultipleJsonLdScripts_usesFirstValidStartLocalDate() {
        String html = """
            <html>
            <head>
                <script type="application/ld+json">
                {
                    "@type": "Organization",
                    "name": "Lincoln Theatre"
                }
                </script>
                <script type="application/ld+json">
                {
                    "@type": "Event",
                    "startDate": "2025-12-30T20:00:00-0500"
                }
                </script>
            </head>
            <body></body>
            </html>
            """;
        Document doc = Jsoup.parse(html);

        LocalDate result = extractor.extractLocalDate(doc);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(LocalDate.of(2025, Month.DECEMBER, 30));
    }

    @Test
    void extractDate_withMultipleLocalDateClasses_usesFirst() {
        String html = """
            <html>
            <body>
                <div class="event-date">December 15, 2025</div>
                <div class="date">2025-01-20</div>
            </body>
            </html>
            """;
        Document doc = Jsoup.parse(html);

        LocalDate result = extractor.extractLocalDate(doc);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(LocalDate.of(2025, Month.DECEMBER, 15));
    }

    @Test
    void extractDate_withNoLocalDateElements_returnsNull() {
        String html = """
            <html>
            <body>
                <div>No date information</div>
            </body>
            </html>
            """;
        Document doc = Jsoup.parse(html);

        LocalDate result = extractor.extractLocalDate(doc);

        assertThat(result).isNull();
    }

    @Test
    void extractDate_withNoStartLocalDateInJsonLd_fallsBackToMeta() {
        String html = """
            <html>
            <head>
                <script type="application/ld+json">
                {
                    "@type": "Event",
                    "name": "Test Event"
                }
                </script>
                <meta property="event:start_date" content="2025-01-15"/>
            </head>
            <body></body>
            </html>
            """;
        Document doc = Jsoup.parse(html);

        LocalDate result = extractor.extractLocalDate(doc);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(LocalDate.of(2025, Month.JANUARY, 15));
    }

    @Test
    void extractDate_withTimeAndLocalDateClass_prefersTime() {
        String html = """
            <html>
            <body>
                <time datetime="2025-12-15">December 15, 2025</time>
                <div class="date">2025-01-20</div>
            </body>
            </html>
            """;
        Document doc = Jsoup.parse(html);

        LocalDate result = extractor.extractLocalDate(doc);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(LocalDate.of(2025, Month.DECEMBER, 15));
    }

    @Test
    void extractDate_withTimeElement_returnsLocalDate() {
        String html = """
            <html>
            <body>
                <time datetime="2025-12-15">December 15, 2025</time>
            </body>
            </html>
            """;
        Document doc = Jsoup.parse(html);

        LocalDate result = extractor.extractLocalDate(doc);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(LocalDate.of(2025, Month.DECEMBER, 15));
    }

    @Test
    void extractLocalDate_withAllFormats_parsesCorrectly() {
        String[][] testCases = {
            {"December 15, 2025", "2025-12-15"},
            {"Dec 15, 2025", "2025-12-15"},
            {"2025-12-15", "2025-12-15"},
            {"January 1, 2026", "2026-01-01"},
            {"Jan 1, 2026", "2026-01-01"}
        };

        for (String[] testCase : testCases) {
            String dateString = testCase[0];
            String expectedIso = testCase[1];
            LocalDate expected = LocalDate.parse(expectedIso);

            String html = String.format("""
                <html>
                <body>
                    <div class="date">%s</div>
                </body>
                </html>
                """, dateString);
            Document doc = Jsoup.parse(html);

            LocalDate result = extractor.extractLocalDate(doc);

            assertThat(result).as("Date string: " + dateString)
                .isNotNull()
                .isEqualTo(expected);
        }
    }

    // Tests for JSON-LD extraction

    @Test
    void extractLocalDate_withBlankMetaContent_fallsBackToTime() {
        String html = """
            <html>
            <head>
                <meta property="event:start_date" content="   "/>
            </head>
            <body>
                <time datetime="2025-12-15">December 15, 2025</time>
            </body>
            </html>
            """;
        Document doc = Jsoup.parse(html);

        LocalDate result = extractor.extractLocalDate(doc);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(LocalDate.of(2025, Month.DECEMBER, 15));
    }

    @Test
    void extractLocalDate_withEmptyMetaContent_fallsBackToTime() {
        String html = """
            <html>
            <head>
                <meta property="event:start_date" content=""/>
            </head>
            <body>
                <time datetime="2025-12-15">December 15, 2025</time>
            </body>
            </html>
            """;
        Document doc = Jsoup.parse(html);

        LocalDate result = extractor.extractLocalDate(doc);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(LocalDate.of(2025, Month.DECEMBER, 15));
    }

    @Test
    void extractLocalDate_withInvalidMetaContent_fallsBackToTime() {
        String html = """
            <html>
            <head>
                <meta property="event:start_date" content="invalid-date"/>
            </head>
            <body>
                <time datetime="2025-12-15">December 15, 2025</time>
            </body>
            </html>
            """;
        Document doc = Jsoup.parse(html);

        LocalDate result = extractor.extractLocalDate(doc);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(LocalDate.of(2025, Month.DECEMBER, 15));
    }

    @Test
    void extractLocalDate_withJsonLd_takesPriorityOverMeta() {
        String html = """
            <html>
            <head>
                <script type="application/ld+json">
                {
                    "@type": "Event",
                    "startDate": "2025-12-30T20:00:00-0500"
                }
                </script>
                <meta property="event:start_date" content="2025-01-15"/>
            </head>
            <body></body>
            </html>
            """;
        Document doc = Jsoup.parse(html);

        LocalDate result = extractor.extractLocalDate(doc);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(LocalDate.of(2025, Month.DECEMBER, 30));
    }

    @Test
    void extractLocalDate_withMultipleTimeElements_usesFirst() {
        String html = """
            <html>
            <body>
                <time datetime="2025-12-15">December 15, 2025</time>
                <time datetime="2025-01-20">January 20, 2025</time>
            </body>
            </html>
            """;
        Document doc = Jsoup.parse(html);

        LocalDate result = extractor.extractLocalDate(doc);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(LocalDate.of(2025, Month.DECEMBER, 15));
    }

    @Test
    void extractLocalDate_withTimeElementFullFormat_parsesCorrectly() {
        String html = """
            <html>
            <body>
                <time datetime="December 15, 2025">Event Date</time>
            </body>
            </html>
            """;
        Document doc = Jsoup.parse(html);

        LocalDate result = extractor.extractLocalDate(doc);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(LocalDate.of(2025, Month.DECEMBER, 15));
    }

    @Test
    void extractLocalDate_withTimeElementShortFormat_parsesCorrectly() {
        String html = """
            <html>
            <body>
                <time datetime="Dec 15, 2025">Event Date</time>
            </body>
            </html>
            """;
        Document doc = Jsoup.parse(html);

        LocalDate result = extractor.extractLocalDate(doc);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(LocalDate.of(2025, Month.DECEMBER, 15));
    }

    @BeforeEach
    void setUp() {
        extractor = new DateExtractor();
    }
}
