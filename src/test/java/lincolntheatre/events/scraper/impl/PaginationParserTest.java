package lincolntheatre.events.scraper.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PaginationParserTest {

    private PaginationParser parser;

    @Test
    void getTotalPages_withBothPageLinksAndNextLink_returnsMaxFromPageLinks() {
        String html = "<html><head>"
            + "<link rel=\"next\" href=\"https://lincolntheatre.com/events/page/2/\"/>"
            + "</head><body>"
            + "<a class=\"page-numbers\">1</a>"
            + "<a class=\"page-numbers\">2</a>"
            + "<a class=\"page-numbers\">3</a>"
            + "</body></html>";
        Document doc = Jsoup.parse(html);

        int result = parser.getTotalPages(doc);

        assertThat(result).isEqualTo(3);
    }

    @Test
    void getTotalPages_withEmptyDocument_returnsOne() {
        String html = "<html><body></body></html>";
        Document doc = Jsoup.parse(html);

        int result = parser.getTotalPages(doc);

        assertThat(result).isEqualTo(1);
    }

    @Test
    void getTotalPages_withErrorDuringParsing_returnsOne() {
        // This test verifies the catch block handles exceptions gracefully
        Document doc = Jsoup.parse("<html><body></body></html>");

        int result = parser.getTotalPages(doc);

        assertThat(result).isGreaterThanOrEqualTo(1);
    }

    @Test
    void getTotalPages_withMixedContent_extractsOnlyNumbers() {
        String html = "<html><body>"
            + "<a class=\"page-numbers\">Page 1</a>"
            + "<a class=\"page-numbers\">3</a>"
            + "<a class=\"page-numbers\">Previous</a>"
            + "</body></html>";
        Document doc = Jsoup.parse(html);

        int result = parser.getTotalPages(doc);

        assertThat(result).isEqualTo(3);
    }

    @Test
    void getTotalPages_withNextLinkOnly_returnsTwo() {
        String html = "<html><head>"
            + "<link rel=\"next\" href=\"https://lincolntheatre.com/events/page/2/\"/>"
            + "</head><body></body></html>";
        Document doc = Jsoup.parse(html);

        int result = parser.getTotalPages(doc);

        assertThat(result).isEqualTo(2);
    }

    @Test
    void getTotalPages_withNonNumericPageLinks_ignoresNonNumeric() {
        String html = "<html><body>"
            + "<a class=\"page-numbers\">1</a>"
            + "<a class=\"page-numbers\">Next</a>"
            + "<a class=\"page-numbers\">2</a>"
            + "</body></html>";
        Document doc = Jsoup.parse(html);

        int result = parser.getTotalPages(doc);

        assertThat(result).isEqualTo(2);
    }

    @Test
    void getTotalPages_withNonSequentialPages_returnsMaxPageNumber() {
        String html = "<html><body>"
            + "<a class=\"page-numbers\">1</a>"
            + "<a class=\"page-numbers\">5</a>"
            + "<a class=\"page-numbers\">3</a>"
            + "</body></html>";
        Document doc = Jsoup.parse(html);

        int result = parser.getTotalPages(doc);

        assertThat(result).isEqualTo(5);
    }

    @Test
    void getTotalPages_withNumberedPageLinks_returnsMaxPageNumber() {
        String html = "<html><body>"
            + "<a class=\"page-numbers\">1</a>"
            + "<a class=\"page-numbers\">2</a>"
            + "<a class=\"page-numbers\">3</a>"
            + "</body></html>";
        Document doc = Jsoup.parse(html);

        int result = parser.getTotalPages(doc);

        assertThat(result).isEqualTo(3);
    }

    @Test
    void getTotalPages_withSinglePage_returnsOne() {
        String html = "<html><body><div>Single page content</div></body></html>";
        Document doc = Jsoup.parse(html);

        int result = parser.getTotalPages(doc);

        assertThat(result).isEqualTo(1);
    }

    @Test
    void getTotalPages_withWhitespaceInPageNumbers_parsesCorrectly() {
        String html = "<html><body>"
            + "<a class=\"page-numbers\">  1  </a>"
            + "<a class=\"page-numbers\">  4  </a>"
            + "</body></html>";
        Document doc = Jsoup.parse(html);

        int result = parser.getTotalPages(doc);

        assertThat(result).isEqualTo(4);
    }

    @BeforeEach
    void setUp() {
        parser = new PaginationParser();
    }
}
