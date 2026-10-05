package lincolntheatre.events.parser.impl;

import static lincolntheatre.events.parser.impl.CssSelectors.DATE_CLASS;
import static lincolntheatre.events.parser.impl.CssSelectors.META_START_DATE;
import static lincolntheatre.events.parser.impl.CssSelectors.TIME_DATETIME;
import static lincolntheatre.events.parser.impl.HtmlConstants.CONTENT_ATTR;
import static lincolntheatre.events.parser.impl.HtmlConstants.DATETIME_ATTR;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Extracts event dates from Lincoln Theatre HTML.
 */
public final class DateExtractor {

    // Pattern to find startDate in JSON-LD: "startDate": "2025-12-30T20:00:00-0500"
    private static final Pattern JSON_LD_START_DATE =
        Pattern.compile("\"startDate\"\\s*:\\s*\"([^\"]+)\"");
    private static final String SCRIPT_TYPE_APPLICATION_LD_JSON = "script[type=application/ld"
        + "+json]";
    private final DateParser dateParser;

    /**
     * Creates a DateExtractor with default DateParser.
     */
    public DateExtractor() {
        this.dateParser = new DateParser();
    }

    /**
     * Extracts the event date using multiple strategies.
     *
     * @param doc the parsed HTML document
     * @return the extracted date, or null if not found
     */
    public LocalDate extractLocalDate(final Document doc) {
        LocalDate date = tryJsonLdStartDate(doc);
        if (date != null) {
            return date;
        }

        date = tryMetaStartDate(doc);
        if (date != null) {
            return date;
        }

        date = tryTimeElements(doc);
        if (date != null) {
            return date;
        }

        return tryDateClasses(doc);
    }

    private LocalDate tryDateClasses(final Document doc) {
        final Elements dateElements = doc.select(DATE_CLASS);
        for (Element el : dateElements) {
            final String text = el.text().trim();
            final LocalDate date = dateParser.parse(text);
            if (date != null) {
                return date;
            }
        }
        return null;
    }

    private LocalDate tryJsonLdStartDate(final Document doc) {
        // Look for JSON-LD script tags with Event schema
        Elements scripts = doc.select(SCRIPT_TYPE_APPLICATION_LD_JSON);
        for (Element script : scripts) {
            String json = script.html();
            Matcher matcher = JSON_LD_START_DATE.matcher(json);
            if (matcher.find()) {
                String startDate = matcher.group(1);
                LocalDate date = dateParser.parseIsoDateTime(startDate);
                if (date != null) {
                    return date;
                }
            }
        }
        return null;
    }

    private LocalDate tryMetaStartDate(final Document doc) {
        final Element meta = doc.selectFirst(META_START_DATE);
        if (meta != null) {
            final String content = meta.attr(CONTENT_ATTR).trim();
            return dateParser.parse(content);
        }
        return null;
    }

    private LocalDate tryTimeElements(final Document doc) {
        final Elements timeElements = doc.select(TIME_DATETIME);
        for (Element time : timeElements) {
            final String datetime = time.attr(DATETIME_ATTR).trim();
            final LocalDate date = dateParser.parse(datetime);
            if (date != null) {
                return date;
            }
        }
        return null;
    }
}
