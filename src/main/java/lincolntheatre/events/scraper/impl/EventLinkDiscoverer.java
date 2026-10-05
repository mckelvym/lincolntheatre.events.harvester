package lincolntheatre.events.scraper.impl;

import static java.util.Objects.requireNonNull;
import static lincolntheatre.events.parser.impl.CssSelectors.EVENT_LINK;
import static lincolntheatre.events.parser.impl.CssSelectors.EVENT_MONTH;
import static lincolntheatre.events.parser.impl.CssSelectors.EVENT_WRAPPER;
import static lincolntheatre.events.parser.impl.HtmlConstants.ABS_HREF_ATTR;

import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovers event links and dates from Lincoln Theatre event listing pages.
 */
public final class EventLinkDiscoverer {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM"
        + " d yyyy", Locale.ENGLISH);
    private static final Pattern EVENT_URL_PATTERN =
        Pattern.compile(".*/event/[^/]+/.*");
    private static final Logger LOG = LoggerFactory.getLogger(EventLinkDiscoverer.class);

    /**
     * Discovers all event links and dates from the given page document.
     *
     * @param doc the parsed HTML document
     * @return list of event links with dates
     */
    public List<EventLink> discoverEventLinks(final Document doc) {
        requireNonNull(doc, "doc must not be null");
        final Map<String, EventLink> eventLinks = new LinkedHashMap<>();

        for (final Element wrapper : doc.select(EVENT_WRAPPER)) {
            extractEventUrl(wrapper).ifPresent(href ->
                eventLinks.computeIfAbsent(href, url -> createEventLink(url, wrapper)));
        }

        LOG.info("Discovered {} event links from page", eventLinks.size());
        return new ArrayList<>(eventLinks.values());
    }

    private EventLink createEventLink(final String url, final Element wrapper) {
        final LocalDate date = extractDateFromWrapper(wrapper);
        if (date == null) {
            LOG.warn("No date found in listing for: {}", url);
        }
        return new EventLink(url, date);
    }

    private LocalDate extractDateFromWrapper(final Element wrapper) {
        final Element dateEl = wrapper.selectFirst(EVENT_MONTH);
        if (dateEl == null) {
            return null;
        }

        final String dateText = dateEl.text().trim();
        return parseListingDate(dateText);
    }

    private Optional<String> extractEventUrl(final Element wrapper) {
        return Optional.ofNullable(wrapper.selectFirst(EVENT_LINK))
            .map(linkEl -> linkEl.attr(ABS_HREF_ATTR))
            .filter(this::isValidEventUrl);
    }

    private boolean isValidEventUrl(final String url) {
        if (url == null || url.isBlank()) {
            return false;
        }

        return EVENT_URL_PATTERN.matcher(url).matches();
    }

    private LocalDate parseListingDate(final String dateText) {
        try {
            final String[] parts = dateText.split(",\\s+");
            if (parts.length < 2) {
                LOG.warn("Invalid date format: {}", dateText);
                return null;
            }

            final String monthDay = parts[1].trim();
            final int currentYear = Year.now().getValue();
            final LocalDate parsed = LocalDate.parse(
                monthDay + " " + currentYear,
                DATE_TIME_FORMATTER
            );

            if (parsed.isBefore(LocalDate.now().minusMonths(2))) {
                return parsed.plusYears(1);
            }

            return parsed;
        } catch (DateTimeParseException e) {
            LOG.warn("Could not parse date from listing: {}", dateText);
            return null;
        }
    }
}
