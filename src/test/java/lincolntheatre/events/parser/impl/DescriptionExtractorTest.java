package lincolntheatre.events.parser.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

class DescriptionExtractorTest {

    @Test
    void extractDescription_withCapitalClass_returnsContent() {
        String html = "<html><body>"
            + "<div class=\"eventDescription\">Event info</div>"
            + "</body></html>";
        Document doc = Jsoup.parse(html);
        DescriptionExtractor extractor = new DescriptionExtractor(false);

        String result = extractor.extract(doc);

        assertThat(result).isEqualTo("Event info");
    }

    @Test
    void extractDescription_withClass_returnsContent() {
        String html = "<html><body>"
            + "<div class=\"event-description\">Full event details</div>"
            + "</body></html>";
        Document doc = Jsoup.parse(html);
        DescriptionExtractor extractor = new DescriptionExtractor(false);

        String result = extractor.extract(doc);

        assertThat(result).isEqualTo("Full event details");
    }

    @Test
    void extractDescription_withMeta_returnsMetaContent() {
        String html = "<html><head>"
            + "<meta name=\"description\" content=\"Concert at Lincoln Theatre\" />"
            + "</head><body></body></html>";
        Document doc = Jsoup.parse(html);
        DescriptionExtractor extractor = new DescriptionExtractor(false);

        String result = extractor.extract(doc);

        assertThat(result).isEqualTo("Concert at Lincoln Theatre");
    }

    @Test
    void extractDescription_withMultipleStrategies_prefersMeta() {
        String html = "<html><head>"
            + "<meta name=\"description\" content=\"Meta content\" />"
            + "</head><body>"
            + "<div id=\"evSubHead\">Subheader content</div>"
            + "</body></html>";
        Document doc = Jsoup.parse(html);
        DescriptionExtractor extractor = new DescriptionExtractor(false);

        String result = extractor.extract(doc);

        assertThat(result).isEqualTo("Meta content");
    }

    @Test
    void extractDescription_withNoMetaOrSubheader_fallsBackTo() {
        String html = "<html><body>"
            + "<div class=\"description\">Description content</div>"
            + "</body></html>";
        Document doc = Jsoup.parse(html);
        DescriptionExtractor extractor = new DescriptionExtractor(false);

        String result = extractor.extract(doc);

        assertThat(result).isEqualTo("Description content");
    }

    @Test
    void extract_withBlankMeta_skipsToNextStrategy() {
        String html = "<html><head>"
            + "<meta name=\"description\" content=\"   \" />"
            + "</head><body>"
            + "<div id=\"evSubHead\">Subheader content</div>"
            + "</body></html>";
        Document doc = Jsoup.parse(html);
        DescriptionExtractor extractor = new DescriptionExtractor(false);

        String result = extractor.extract(doc);

        assertThat(result).isEqualTo("Subheader content");
    }

    @Test
    void extract_withEventSubHeaderClassText_returnsText() {
        String html = "<html><body>"
            + "<div class=\"eventSubHeader\">Live music event</div>"
            + "</body></html>";
        Document doc = Jsoup.parse(html);
        DescriptionExtractor extractor = new DescriptionExtractor(false);

        String result = extractor.extract(doc);

        assertThat(result).isEqualTo("Live music event");
    }

    @Test
    void extract_withNoMatchingElements_returnsNull() {
        String html = "<html><body><p>Some content</p></body></html>";
        Document doc = Jsoup.parse(html);
        DescriptionExtractor extractor = new DescriptionExtractor(false);

        String result = extractor.extract(doc);

        assertThat(result).isNull();
    }

    @Test
    void extract_withNoMeta_fallsBackToSubheader() {
        String html = "<html><body>"
            + "<div id=\"evSubHead\">Subheader content</div>"
            + "<div class=\"description\">Description content</div>"
            + "</body></html>";
        Document doc = Jsoup.parse(html);
        DescriptionExtractor extractor = new DescriptionExtractor(false);

        String result = extractor.extract(doc);

        assertThat(result).isEqualTo("Subheader content");
    }

    @Test
    void extract_withSubheaderHtml_returnsHtml() {
        String html = "<html><body>"
            + "<div id=\"evSubHead\"><p>Event with <b>bold</b> text</p></div>"
            + "</body></html>";
        Document doc = Jsoup.parse(html);
        DescriptionExtractor extractor = new DescriptionExtractor(true);

        String result = extractor.extract(doc);

        assertThat(result).contains("<p>").contains("<b>bold</b>");
    }

    @Test
    void extract_withSubheaderIdText_returnsText() {
        String html = "<html><body>"
            + "<div id=\"evSubHead\">Special performance</div>"
            + "</body></html>";
        Document doc = Jsoup.parse(html);
        DescriptionExtractor extractor = new DescriptionExtractor(false);

        String result = extractor.extract(doc);

        assertThat(result).isEqualTo("Special performance");
    }

    @Test
    void extract_withWhitespace_returnsTrimmedContent() {
        String html = "<html><head>"
            + "<meta name=\"description\" content=\"  Event description  \" />"
            + "</head></html>";
        Document doc = Jsoup.parse(html);
        DescriptionExtractor extractor = new DescriptionExtractor(false);

        String result = extractor.extract(doc);

        assertThat(result).isEqualTo("Event description");
    }
}
