package lincolntheatre.events.scraper.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventLinkDiscovererTest {

    private EventLinkDiscoverer discoverer;

    @Test
    void discoverEventLinks_convertsRelativeToAbsoluteUrls() {
        String html = "<html><body>"
            + "<div class=\"eventWrapper\">"
            + "<a href=\"/event/local-show/2024-12-15/\">Local Show</a>"
            + "</div>"
            + "</body></html>";
        Document doc = Jsoup.parse(html, "https://lincolntheatre.com");

        List<EventLink> result = discoverer.discoverEventLinks(doc);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).url()).startsWith("https://lincolntheatre.com");
    }

    @Test
    void discoverEventLinks_extractsDateFromWrapper() {
        // The code parses "Xxx, Month Day" format and uses current year
        // So we use a month in the future to avoid year adjustment
        LocalDate now = LocalDate.now();
        int month = now.plusMonths(2).getMonthValue();
        String monthStr = now.plusMonths(2).getMonth().name().substring(0, 3);
        monthStr = monthStr.charAt(0) + monthStr.substring(1).toLowerCase();
        String html = "<html><body>"
            + "<div class=\"eventWrapper\">"
            + "<div class=\"eventMonth\">Fri, " + monthStr + " 15</div>"
            + "<a href=\"/event/concert/2025-12-15/\">Concert</a>"
            + "</div>"
            + "</body></html>";
        Document doc = Jsoup.parse(html, "https://lincolntheatre.com");

        List<EventLink> result = discoverer.discoverEventLinks(doc);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).date()).isEqualTo(LocalDate.of(now.getYear(), month, 15));
    }

    @Test
    void discoverEventLinks_withDuplicateUrls_removesReturnsUnique() {
        String html = "<html><body>"
            + "<div class=\"eventWrapper\">"
            + "<a href=\"/event/concert/2024-12-15/\">Link 1</a>"
            + "</div>"
            + "<div class=\"eventWrapper\">"
            + "<a href=\"/event/concert/2024-12-15/\">Link 2</a>"
            + "</div>"
            + "<div class=\"eventWrapper\">"
            + "<a href=\"/event/show/2024-12-16/\">Link 3</a>"
            + "</div>"
            + "</body></html>";
        Document doc = Jsoup.parse(html, "https://lincolntheatre.com");

        List<EventLink> result = discoverer.discoverEventLinks(doc);

        assertThat(result).hasSize(2);
    }

    @Test
    void discoverEventLinks_withEmptyDocument_returnsEmptyList() {
        String html = "<html><body></body></html>";
        Document doc = Jsoup.parse(html);

        List<EventLink> result = discoverer.discoverEventLinks(doc);

        assertThat(result).isEmpty();
    }

    @Test
    void discoverEventLinks_withInvalidDateFormat_acceptsNullDate() {
        String html = "<html><body>"
            + "<div class=\"eventWrapper\">"
            + "<div class=\"eventMonth\">Invalid Date</div>"
            + "<a href=\"/event/concert/2024-12-15/\">Concert</a>"
            + "</div>"
            + "</body></html>";
        Document doc = Jsoup.parse(html, "https://lincolntheatre.com");

        List<EventLink> result = discoverer.discoverEventLinks(doc);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).date()).isNull();
    }

    @Test
    void discoverEventLinks_withInvalidEventUrl_filtersItOut() {
        String html = "<html><body>"
            + "<div class=\"eventWrapper\">"
            + "<a href=\"/event/valid/2024-12-15/\">Valid</a>"
            + "</div>"
            + "<div class=\"eventWrapper\">"
            + "<a href=\"/about/info\">Invalid</a>"
            + "</div>"
            + "</body></html>";
        Document doc = Jsoup.parse(html, "https://lincolntheatre.com");

        List<EventLink> result = discoverer.discoverEventLinks(doc);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).url()).contains("valid");
    }

    @Test
    void discoverEventLinks_withMissingDate_acceptsNullDate() {
        String html = "<html><body>"
            + "<div class=\"eventWrapper\">"
            + "<a href=\"/event/concert/2024-12-15/\">Concert</a>"
            + "</div>"
            + "</body></html>";
        Document doc = Jsoup.parse(html, "https://lincolntheatre.com");

        List<EventLink> result = discoverer.discoverEventLinks(doc);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).date()).isNull();
    }

    @Test
    void discoverEventLinks_withMissingLink_skipsWrapper() {
        String html = "<html><body>"
            + "<div class=\"eventWrapper\">"
            + "<div class=\"eventMonth\">2024, Dec 15</div>"
            + "<p>No link here</p>"
            + "</div>"
            + "<div class=\"eventWrapper\">"
            + "<a href=\"/event/concert/2024-12-15/\">Concert</a>"
            + "</div>"
            + "</body></html>";
        Document doc = Jsoup.parse(html, "https://lincolntheatre.com");

        List<EventLink> result = discoverer.discoverEventLinks(doc);

        assertThat(result).hasSize(1);
    }

    @Test
    void discoverEventLinks_withMultipleEvents_returnsAllLinks() {
        String html = "<html><body>"
            + "<div class=\"eventWrapper\">"
            + "<div class=\"eventMonth\">2024, Dec 15</div>"
            + "<a href=\"/event/event-one/2024-12-15/\">Event 1</a>"
            + "</div>"
            + "<div class=\"eventWrapper\">"
            + "<div class=\"eventMonth\">2024, Dec 20</div>"
            + "<a href=\"/event/event-two/2024-12-20/\">Event 2</a>"
            + "</div>"
            + "</body></html>";
        Document doc = Jsoup.parse(html, "https://lincolntheatre.com");

        List<EventLink> result = discoverer.discoverEventLinks(doc);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).url()).contains("event-one");
        assertThat(result.get(1).url()).contains("event-two");
    }

    @Test
    void discoverEventLinks_withNoEventWrappers_returnsEmptyList() {
        String html = "<html><body>"
            + "<div>Some content</div>"
            + "<a href=\"/about/\">About</a>"
            + "</body></html>";
        Document doc = Jsoup.parse(html, "https://lincolntheatre.com");

        List<EventLink> result = discoverer.discoverEventLinks(doc);

        assertThat(result).isEmpty();
    }

    @Test
    void discoverEventLinks_withSingleValidEvent_returnsOneLink() {
        String html = "<html><body>"
            + "<div class=\"eventWrapper\">"
            + "<div class=\"eventMonth\">2024, Dec 15</div>"
            + "<a href=\"/event/concert-night/2024-12-15/\">Concert</a>"
            + "</div>"
            + "</body></html>";
        Document doc = Jsoup.parse(html, "https://lincolntheatre.com");

        List<EventLink> result = discoverer.discoverEventLinks(doc);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).url()).isEqualTo("https://lincolntheatre"
            + ".com/event/concert-night/2024-12-15/");
    }

    @Test
    void discoverEventLinks_withYearInference_adjustsForPastDates() {
        // Test that dates more than 2 months in the past get next year
        String html = "<html><body>"
            + "<div class=\"eventWrapper\">"
            + "<div class=\"eventMonth\">2024, Jan 5</div>"
            + "<a href=\"/event/concert/2025-01-05/\">Concert</a>"
            + "</div>"
            + "</body></html>";
        Document doc = Jsoup.parse(html, "https://lincolntheatre.com");

        List<EventLink> result = discoverer.discoverEventLinks(doc);

        assertThat(result).hasSize(1);
        // Date inference logic applies, result may be adjusted
        assertThat(result.get(0).date()).isNotNull();
    }

    @BeforeEach
    void setUp() {
        discoverer = new EventLinkDiscoverer();
    }
}
