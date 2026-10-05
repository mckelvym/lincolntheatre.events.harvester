package lincolntheatre.events.parser.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;
import lincolntheatre.events.domain.EventItem;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive tests for EventParserImpl.
 * Tests integration of all extractors and event parsing logic.
 */
class EventParserImplTest {

    private EventParserImpl parser;

    @Test
    void parseEvent_withAllExtractorsFailing_returnsEmptyForMissingTitle() {
        String html = """
            <html>
            <body>
                <div>No event data</div>
            </body>
            </html>
            """;
        Document doc = Jsoup.parse(html);
        String eventUrl = "https://lincolntheatre.com/events/event/";

        Optional<EventItem> result = parser.parseEvent(doc, eventUrl);

        assertThat(result).isEmpty();
    }

    @Test
    void parseEvent_withBlankTitle_returnsEmpty() {
        String html = """
            <html>
            <head>
                <meta property="event:start_date" content="2025-12-15"/>
            </head>
            <body>
                <h1 class="entry-title">   </h1>
            </body>
            </html>
            """;
        Document doc = Jsoup.parse(html);
        String eventUrl = "https://lincolntheatre.com/events/event/";

        Optional<EventItem> result = parser.parseEvent(doc, eventUrl);

        assertThat(result).isEmpty();
    }

    @Test
    void parseEvent_withCompleteEvent_returnsEventItem() {
        String html = """
            <html>
            <head>
                <meta property="event:start_date" content="2025-12-15"/>
                <meta name="description" content="A great musical performance"/>
            </head>
            <body>
                <h1>Test Concert</h1>
                <img src="https://example.com/image.jpg" alt="Event Image"/>
            </body>
            </html>
            """;
        Document doc = Jsoup.parse(html);
        String eventUrl = "https://lincolntheatre.com/events/test-concert/";

        Optional<EventItem> result = parser.parseEvent(doc, eventUrl);

        assertThat(result).isPresent();
        EventItem event = result.get();
        assertThat(event.id()).isEqualTo(eventUrl);
        assertThat(event.title()).isEqualTo("Test Concert");
        assertThat(event.link()).isEqualTo(eventUrl);
        assertThat(event.description()).isEqualTo("A great musical performance");
        assertThat(event.eventDateStart()).isEqualTo(LocalDate.of(2025, Month.DECEMBER, 15));
        assertThat(event.eventDateEnd()).isNull();
        assertThat(event.imageUrl()).isEqualTo("https://example.com/image.jpg");
        assertThat(event.location()).isNull();
    }

    @Test
    void parseEvent_withJsonLdDate_parsesDate() {
        String html = """
            <html>
            <head>
                <script type="application/ld+json">
                {
                    "@type": "Event",
                    "startDate": "2025-12-30T20:00:00-0500"
                }
                </script>
            </head>
            <body>
                <h1 class="entry-title">Concert with JSON-LD</h1>
            </body>
            </html>
            """;
        Document doc = Jsoup.parse(html);
        String eventUrl = "https://lincolntheatre.com/events/event/";

        Optional<EventItem> result = parser.parseEvent(doc, eventUrl);

        assertThat(result).isPresent();
        EventItem event = result.get();
        assertThat(event.eventDateStart()).isEqualTo(LocalDate.of(2025, Month.DECEMBER, 30));
    }

    @Test
    void parseEvent_withMissingOptionalFields_returnsEventWithNulls() {
        String html = """
            <html>
            <head>
                <meta property="event:start_date" content="2025-01-20"/>
            </head>
            <body>
                <h1 class="entry-title">Concert Without Image</h1>
            </body>
            </html>
            """;
        Document doc = Jsoup.parse(html);
        String eventUrl = "https://lincolntheatre.com/events/concert/";

        Optional<EventItem> result = parser.parseEvent(doc, eventUrl);

        assertThat(result).isPresent();
        EventItem event = result.get();
        assertThat(event.title()).isEqualTo("Concert Without Image");
        assertThat(event.description()).isNullOrEmpty();
        assertThat(event.imageUrl()).isNullOrEmpty();
        assertThat(event.eventDateStart()).isEqualTo(LocalDate.of(2025, Month.JANUARY, 20));
    }

    @Test
    void parseEvent_withMultipleImages_usesFirst() {
        String html = """
            <html>
            <head>
                <meta property="event:start_date" content="2025-12-15"/>
            </head>
            <body>
                <h1 class="entry-title">Test Event</h1>
                <img src="https://example.com/first.jpg" alt="First"/>
                <img src="https://example.com/second.jpg" alt="Second"/>
            </body>
            </html>
            """;
        Document doc = Jsoup.parse(html);
        String eventUrl = "https://lincolntheatre.com/events/event/";

        Optional<EventItem> result = parser.parseEvent(doc, eventUrl);

        assertThat(result).isPresent();
        EventItem event = result.get();
        assertThat(event.imageUrl()).isEqualTo("https://example.com/first.jpg");
    }

    @Test
    void parseEvent_withNullTitle_returnsEmpty() {
        String html = """
            <html>
            <head>
                <meta property="event:start_date" content="2025-12-15"/>
            </head>
            <body>
                <div>No title element</div>
            </body>
            </html>
            """;
        Document doc = Jsoup.parse(html);
        String eventUrl = "https://lincolntheatre.com/events/event/";

        Optional<EventItem> result = parser.parseEvent(doc, eventUrl);

        assertThat(result).isEmpty();
    }

    @Test
    void parseEvent_withPreserveHtmlDescription_preservesHtml() {
        EventParserImpl htmlParser = new EventParserImpl(true);
        String html = """
            <html>
            <head>
                <meta property="event:start_date" content="2025-12-15"/>
            </head>
            <body>
                <h1>Test Event</h1>
                <div class="eventDescription">
                    <p>First paragraph</p>
                    <p>Second paragraph</p>
                </div>
            </body>
            </html>
            """;
        Document doc = Jsoup.parse(html);
        String eventUrl = "https://lincolntheatre.com/events/event/";

        Optional<EventItem> result = htmlParser.parseEvent(doc, eventUrl);

        assertThat(result).isPresent();
        EventItem event = result.get();
        assertThat(event.description()).contains("<p>");
    }

    @Test
    void parseEvent_withRelativeImageUrl_convertsToAbsolute() {
        String html = """
            <html>
            <head>
                <meta property="event:start_date" content="2025-12-15"/>
            </head>
            <body>
                <h1 class="entry-title">Test Event</h1>
                <img src="/wp-content/uploads/image.jpg" alt="Image"/>
            </body>
            </html>
            """;
        Document doc = Jsoup.parse(html, "https://lincolntheatre.com/");
        String eventUrl = "https://lincolntheatre.com/events/event/";

        Optional<EventItem> result = parser.parseEvent(doc, eventUrl);

        assertThat(result).isPresent();
        EventItem event = result.get();
        assertThat(event.imageUrl()).startsWith("https://");
    }

    @Test
    void parseEvent_withValidDate_parsesSuccessfully() {
        String html = """
            <html>
            <head>
                <meta property="event:start_date" content="2025-01-15"/>
            </head>
            <body>
                <h1 class="entry-title">Event With Valid Date</h1>
                <div class="entry-content">Description here</div>
            </body>
            </html>
            """;
        Document doc = Jsoup.parse(html);
        String eventUrl = "https://lincolntheatre.com/events/event/";

        Optional<EventItem> result = parser.parseEvent(doc, eventUrl);

        assertThat(result).isPresent();
        EventItem event = result.get();
        assertThat(event.title()).isEqualTo("Event With Valid Date");
        assertThat(event.eventDateStart()).isNotNull();
    }

    @Test
    void parseEvent_withWhitespaceInTitle_trimsWhitespace() {
        String html = """
            <html>
            <head>
                <meta property="event:start_date" content="2025-12-15"/>
            </head>
            <body>
                <h1>  Test Event  </h1>
            </body>
            </html>
            """;
        Document doc = Jsoup.parse(html);
        String eventUrl = "https://lincolntheatre.com/events/event/";

        Optional<EventItem> result = parser.parseEvent(doc, eventUrl);

        assertThat(result).isPresent();
        EventItem event = result.get();
        assertThat(event.title()).isEqualTo("Test Event");
    }

    @Test
    void parseEvent_withoutPreserveHtmlDescription_stripsHtml() {
        String html = """
            <html>
            <head>
                <meta property="event:start_date" content="2025-12-15"/>
            </head>
            <body>
                <h1>Test Event</h1>
                <div class="eventDescription">
                    <p>First paragraph</p>
                    <p>Second paragraph</p>
                </div>
            </body>
            </html>
            """;
        Document doc = Jsoup.parse(html);
        String eventUrl = "https://lincolntheatre.com/events/event/";

        Optional<EventItem> result = parser.parseEvent(doc, eventUrl);

        assertThat(result).isPresent();
        EventItem event = result.get();
        assertThat(event.description()).doesNotContain("<p>");
        assertThat(event.description()).contains("First paragraph");
    }

    @BeforeEach
    void setUp() {
        parser = new EventParserImpl(false);
    }
}
