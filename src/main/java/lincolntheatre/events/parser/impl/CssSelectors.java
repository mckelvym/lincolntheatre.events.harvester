package lincolntheatre.events.parser.impl;

/**
 * Constants for CSS selectors used in HTML parsing.
 *
 * <p>This class centralizes all CSS selector strings used throughout the
 * parser implementation to avoid magic strings and improve maintainability.
 */
public final class CssSelectors {

    // EventLinkDiscoverer selectors
    public static final String EVENT_WRAPPER = "div.eventWrapper";
    public static final String EVENT_LINK = "a[href*='/event/']";
    public static final String EVENT_MONTH = "div.eventMonth";

    // PaginationParser selectors
    public static final String PAGE_NUMBERS = "a.page-numbers";
    public static final String NEXT_LINK = "link[rel=next]";

    // TitleExtractor selectors
    public static final String H1 = "h1";
    public static final String EVENT_TITLE_H2 = "#eventTitle h2";
    public static final String TITLE_CLASS = "[class*=title]";
    public static final String META_OG_TITLE = "meta[property=og:title]";

    // DateExtractor selectors
    public static final String META_START_DATE = "meta[property=event:start_date]";
    public static final String TIME_DATETIME = "time[datetime]";
    public static final String DATE_CLASS = "[class*=date], [class*=Date]";

    // DescriptionExtractor selectors
    public static final String META_DESCRIPTION = "meta[name=description]";
    public static final String SUBHEADER = "#evSubHead, .eventSubHeader";
    public static final String DESCRIPTION_CLASS = "[class*=description], [class*=Description]";

    // ImageExtractor selectors
    public static final String EVENT_IMAGE = ".eventListImage, .rhp-events-event-image img";
    public static final String META_OG_IMAGE = "meta[property=og:image]";
    public static final String IMG_SRC = "img[src]";

    // Page loading selectors
    /**
     * Selector for basic page load (body element).
     */
    public static final String PAGE_LOAD_SELECTOR = "body";

    private CssSelectors() {
        // Utility class - prevent instantiation
    }
}
