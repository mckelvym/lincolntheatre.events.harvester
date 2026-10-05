package lincolntheatre.events.parser.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ImageExtractorTest {

    private ImageExtractor extractor;

    @Test
    void extractImageUrl_withBlankEventImage_skipsToNextStrategy() {
        String html = "<html><head>"
            + "<meta property=\"og:image\" content=\"https://example.com/og.jpg\" />"
            + "</head><body>"
            + "<img class=\"eventListImage\" src=\"\" />"
            + "</body></html>";
        Document doc = Jsoup.parse(html);

        String result = extractor.extractImageUrl(doc);

        assertThat(result).isEqualTo("https://example.com/og.jpg");
    }

    @Test
    void extractImageUrl_withEventListImage_returnsAbsoluteUrl() {
        String html = "<html><body>"
            + "<img class=\"eventListImage\" src=\"/images/event.jpg\" />"
            + "</body></html>";
        Document doc = Jsoup.parse(html, "https://lincolntheatre.com");

        String result = extractor.extractImageUrl(doc);

        assertThat(result).isEqualTo("https://lincolntheatre.com/images/event.jpg");
    }

    @Test
    void extractImageUrl_withFirstImg_returnsAbsoluteUrl() {
        String html = "<html><body>"
            + "<img src=\"/images/first.jpg\" />"
            + "</body></html>";
        Document doc = Jsoup.parse(html, "https://lincolntheatre.com");

        String result = extractor.extractImageUrl(doc);

        assertThat(result).isEqualTo("https://lincolntheatre.com/images/first.jpg");
    }

    @Test
    void extractImageUrl_withMetaOgImage_returnsContent() {
        String html = "<html><head>"
            + "<meta property=\"og:image\" content=\"https://example.com/og-image.jpg\" />"
            + "</head><body></body></html>";
        Document doc = Jsoup.parse(html);

        String result = extractor.extractImageUrl(doc);

        assertThat(result).isEqualTo("https://example.com/og-image.jpg");
    }

    @Test
    void extractImageUrl_withMultipleStrategies_prefersEventImage() {
        String html = "<html><head>"
            + "<meta property=\"og:image\" content=\"https://example.com/og.jpg\" />"
            + "</head><body>"
            + "<img class=\"eventListImage\" src=\"/event.jpg\" />"
            + "<img src=\"/generic.jpg\" />"
            + "</body></html>";
        Document doc = Jsoup.parse(html, "https://lincolntheatre.com");

        String result = extractor.extractImageUrl(doc);

        assertThat(result).isEqualTo("https://lincolntheatre.com/event.jpg");
    }

    @Test
    void extractImageUrl_withNoEventImage_fallsBackToOgImage() {
        String html = "<html><head>"
            + "<meta property=\"og:image\" content=\"https://example.com/og.jpg\" />"
            + "</head><body>"
            + "<img src=\"/generic.jpg\" />"
            + "</body></html>";
        Document doc = Jsoup.parse(html, "https://lincolntheatre.com");

        String result = extractor.extractImageUrl(doc);

        assertThat(result).isEqualTo("https://example.com/og.jpg");
    }

    @Test
    void extractImageUrl_withNoEventOrOgImage_fallsBackToFirstImg() {
        String html = "<html><body>"
            + "<img src=\"/generic.jpg\" />"
            + "</body></html>";
        Document doc = Jsoup.parse(html, "https://lincolntheatre.com");

        String result = extractor.extractImageUrl(doc);

        assertThat(result).isEqualTo("https://lincolntheatre.com/generic.jpg");
    }

    @Test
    void extractImageUrl_withNoMatchingElements_returnsNull() {
        String html = "<html><body><p>No images</p></body></html>";
        Document doc = Jsoup.parse(html);

        String result = extractor.extractImageUrl(doc);

        assertThat(result).isNull();
    }

    @Test
    void extractImageUrl_withRhpEventsImage_returnsAbsoluteUrl() {
        String html = "<html><body>"
            + "<div class=\"rhp-events-event-image\">"
            + "<img src=\"/posters/show.jpg\" />"
            + "</div>"
            + "</body></html>";
        Document doc = Jsoup.parse(html, "https://lincolntheatre.com");

        String result = extractor.extractImageUrl(doc);

        assertThat(result).isEqualTo("https://lincolntheatre.com/posters/show.jpg");
    }

    @Test
    void extractImageUrl_withWhitespace_returnsTrimmedUrl() {
        String html = "<html><head>"
            + "<meta property=\"og:image\" content=\"  https://example.com/image.jpg  \" />"
            + "</head></html>";
        Document doc = Jsoup.parse(html);

        String result = extractor.extractImageUrl(doc);

        assertThat(result).isEqualTo("https://example.com/image.jpg");
    }

    @BeforeEach
    void setUp() {
        extractor = new ImageExtractor();
    }
}
