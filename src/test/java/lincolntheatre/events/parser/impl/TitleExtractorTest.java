package lincolntheatre.events.parser.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TitleExtractorTest {

    private TitleExtractor extractor;

    @Test
    void extractTitle_withBlankH1_usesEventTitleH2() {
        String html = "<html><body>"
            + "<h1>   </h1>"
            + "<div id=\"eventTitle\"><h2>Fallback Title</h2></div>"
            + "</body></html>";
        Document doc = Jsoup.parse(html);

        String result = extractor.extractTitle(doc);

        assertThat(result).isEqualTo("Fallback Title");
    }

    @Test
    void extractTitle_withBlankMetaOgTitle_returnsNull() {
        String html = "<html><head><meta property=\"og:title\" content=\"   \"/></head><body>"
            + "<h1>   </h1>"
            + "<div id=\"eventTitle\"><h2>  </h2></div>"
            + "<span class=\"title\">  </span>"
            + "</body></html>";
        Document doc = Jsoup.parse(html);

        String result = extractor.extractTitle(doc);

        assertThat(result).isNull();
    }

    @Test
    void extractTitle_withBlankTitleClass_usesMetaOgTitle() {
        String html = "<html><head><meta property=\"og:title\" content=\"OG Title\"/></head><body>"
            + "<h1>   </h1>"
            + "<div id=\"eventTitle\"><h2>  </h2></div>"
            + "<span class=\"title\">   </span>"
            + "</body></html>";
        Document doc = Jsoup.parse(html);

        String result = extractor.extractTitle(doc);

        assertThat(result).isEqualTo("OG Title");
    }

    @Test
    void extractTitle_withComplexPageStructure_returnsH1() {
        String html = "<html><head><meta property=\"og:title\" content=\"OG Title\"/></head><body>"
            + "<header><h1>Page Title</h1></header>"
            + "<main>"
            + "<h2>Section Title</h2>"
            + "</main>"
            + "</body></html>";
        Document doc = Jsoup.parse(html);

        String result = extractor.extractTitle(doc);

        assertThat(result).isEqualTo("Page Title");
    }

    @Test
    void extractTitle_withEmptyH1_usesTitleClass() {
        String html = "<html><body>"
            + "<h1></h1>"
            + "<div id=\"eventTitle\"><h2>  </h2></div>"
            + "<span class=\"event-title\">Event Name</span>"
            + "</body></html>";
        Document doc = Jsoup.parse(html);

        String result = extractor.extractTitle(doc);

        assertThat(result).isEqualTo("Event Name");
    }

    @Test
    void extractTitle_withEmptyMetaOgTitle_attribute_returnsNull() {
        String html = "<html><head><meta property=\"og:title\" content=\"\"/></head><body>"
            + "<h1>   </h1>"
            + "<div id=\"eventTitle\"><h2>  </h2></div>"
            + "<span class=\"title\">  </span>"
            + "</body></html>";
        Document doc = Jsoup.parse(html);

        String result = extractor.extractTitle(doc);

        assertThat(result).isNull();
    }

    @Test
    void extractTitle_withEventTitleH2_returnsTitle() {
        String html = "<html><body><div id=\"eventTitle\"><h2>Concert "
            + "Night</h2></div></body></html>";
        Document doc = Jsoup.parse(html);

        String result = extractor.extractTitle(doc);

        assertThat(result).isEqualTo("Concert Night");
    }

    @Test
    void extractTitle_withH1Element_returnsTitle() {
        String html = "<html><body><h1>Theater Event</h1></body></html>";
        Document doc = Jsoup.parse(html);

        String result = extractor.extractTitle(doc);

        assertThat(result).isEqualTo("Theater Event");
    }

    @Test
    void extractTitle_withH1Priority_ignoresEventTitleH2() {
        String html = "<html><body>"
            + "<h1>Primary Title</h1>"
            + "<div id=\"eventTitle\"><h2>Secondary Title</h2></div>"
            + "</body></html>";
        Document doc = Jsoup.parse(html);

        String result = extractor.extractTitle(doc);

        assertThat(result).isEqualTo("Primary Title");
    }

    @Test
    void extractTitle_withH1Whitespace_returnsTrimmedTitle() {
        String html = "<html><body><h1>  Live Performance  </h1></body></html>";
        Document doc = Jsoup.parse(html);

        String result = extractor.extractTitle(doc);

        assertThat(result).isEqualTo("Live Performance");
    }

    @Test
    void extractTitle_withMetaOgTitle_returnsTitle() {
        String html = "<html><head><meta property=\"og:title\" content=\"Meta "
            + "Title\"/></head><body></body></html>";
        Document doc = Jsoup.parse(html);

        String result = extractor.extractTitle(doc);

        assertThat(result).isEqualTo("Meta Title");
    }

    @Test
    void extractTitle_withMultipleH1_returnsFirstOne() {
        String html = "<html><body>"
            + "<h1>First Title</h1>"
            + "<h1>Second Title</h1>"
            + "</body></html>";
        Document doc = Jsoup.parse(html);

        String result = extractor.extractTitle(doc);

        assertThat(result).isEqualTo("First Title");
    }

    @Test
    void extractTitle_withMultipleTitleClasses_returnsFirstOne() {
        String html = "<html><body>"
            + "<h1>   </h1>"
            + "<div id=\"eventTitle\"><h2>  </h2></div>"
            + "<span class=\"title\">First Title</span>"
            + "<span class=\"page-title\">Second Title</span>"
            + "</body></html>";
        Document doc = Jsoup.parse(html);

        String result = extractor.extractTitle(doc);

        assertThat(result).isEqualTo("First Title");
    }

    @Test
    void extractTitle_withNestedTitleClass_returnsTitle() {
        String html = "<html><body>"
            + "<h1>   </h1>"
            + "<div id=\"eventTitle\"><h2>  </h2></div>"
            + "<div><span class=\"title\">Nested Title</span></div>"
            + "</body></html>";
        Document doc = Jsoup.parse(html);

        String result = extractor.extractTitle(doc);

        assertThat(result).isEqualTo("Nested Title");
    }

    @Test
    void extractTitle_withNoElements_returnsNull() {
        String html = "<html><body><p>Some content</p></body></html>";
        Document doc = Jsoup.parse(html);

        String result = extractor.extractTitle(doc);

        assertThat(result).isNull();
    }

    @Test
    void extractTitle_withTitleClass_returnsTitle() {
        String html = "<html><body><span class=\"page-title\">Show Title</span></body></html>";
        Document doc = Jsoup.parse(html);

        String result = extractor.extractTitle(doc);

        assertThat(result).isEqualTo("Show Title");
    }

    @BeforeEach
    void setUp() {
        extractor = new TitleExtractor();
    }
}
