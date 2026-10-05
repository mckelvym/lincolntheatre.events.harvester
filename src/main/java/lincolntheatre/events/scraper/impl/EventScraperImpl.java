package lincolntheatre.events.scraper.impl;

import static java.util.Objects.requireNonNull;
import static lincolntheatre.events.parser.impl.CssSelectors.EVENT_WRAPPER;
import static lincolntheatre.events.parser.impl.CssSelectors.PAGE_LOAD_SELECTOR;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lincolntheatre.events.config.ScraperConfiguration;
import lincolntheatre.events.domain.EventItem;
import lincolntheatre.events.parser.EventParser;
import lincolntheatre.events.scraper.EventScraper;
import lincolntheatre.events.webdriver.PageLoader;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scrapes events using paginated link discovery.
 * Implements the unified 3-phase flow: discover, filter, parse.
 */
public record EventScraperImpl(ScraperConfiguration config,
                               PageLoader pageLoader,
                               EventParser eventParser,
                               PaginationParser paginationParser,
                               EventLinkDiscoverer linkDiscoverer) implements EventScraper {
    private static final Logger LOG = LoggerFactory.getLogger(EventScraperImpl.class);

    public EventScraperImpl {
        requireNonNull(config, "config must not be null");
        requireNonNull(pageLoader, "pageLoader must not be null");
        requireNonNull(eventParser, "eventParser must not be null");
        requireNonNull(paginationParser, "paginationParser must not be null");
        requireNonNull(linkDiscoverer, "linkDiscoverer must not be null");
    }

    /**
     * Adds event links keyed by URL, keeping the first occurrence unless it lacks a date.
     *
     * @param allEventLinks accumulated event links keyed by URL
     * @param eventLinks    event links discovered on one page
     */
    private void addEventLinks(final Map<String, EventLink> allEventLinks,
                               final List<EventLink> eventLinks) {
        for (final EventLink eventLink : eventLinks) {
            allEventLinks.merge(eventLink.url(), eventLink,
                (existing, candidate) -> existing.date() == null ? candidate : existing);
        }
    }

    /**
     * Phase 1: Discovers all event URLs with dates across all pages.
     *
     * @return List of discovered EventLink objects containing URLs and dates
     */
    private List<EventLink> discoverEventUrls() {
        final Document firstPage = pageLoader.loadPage(config.getBaseUrl(), EVENT_WRAPPER);
        final int totalPages = paginationParser.getTotalPages(firstPage);

        // Keyed by URL (the GUID) so an event listed on multiple pages is kept once
        final Map<String, EventLink> allEventLinks = new LinkedHashMap<>();
        addEventLinks(allEventLinks, linkDiscoverer.discoverEventLinks(firstPage));

        for (int page = 2; page <= totalPages; page++) {
            final String pageUrl = config.getBaseUrl() + "page/" + page + "/";
            LOG.info("Discovering links on page {}/{}", page, totalPages);

            final Document pageDoc = pageLoader.loadPage(pageUrl, EVENT_WRAPPER);
            addEventLinks(allEventLinks, linkDiscoverer.discoverEventLinks(pageDoc));
        }

        return new ArrayList<>(allEventLinks.values());
    }

    /**
     * Phase 2: Filters EventLinks to only those not in existingGuids.
     *
     * @param eventLinks    All discovered EventLink objects
     * @param existingGuids Set of existing event GUIDs
     * @return Filtered list of new EventLinks
     */
    private List<EventLink> filterNewUrls(final List<EventLink> eventLinks,
                                          final Set<String> existingGuids) {
        return eventLinks.stream()
            .filter(eventLink -> !existingGuids.contains(eventLink.url()))
            .toList();
    }

    /**
     * Parses a single event and adds it to results.
     *
     * @param eventLink EventLink containing URL and date
     * @param current   Current event number (1-based)
     * @param total     Total number of events
     * @param results   List to add parsed event to
     */
    private void parseAndAddEvent(final EventLink eventLink, final int current, final int total,
                                  final List<EventItem> results) {
        final Document eventDoc = pageLoader.loadPage(eventLink.url(), PAGE_LOAD_SELECTOR);
        final Optional<EventItem> eventOpt = eventParser.parseEvent(eventDoc, eventLink.url());

        if (eventOpt.isPresent()) {
            final EventItem event = resolveEventDate(eventOpt.get(), eventLink);
            results.add(event);
            LOG.info("Event {}/{}: {} ({})", current, total,
                event.title(), event.eventDateStart());
        } else {
            LOG.warn("No event returned for: {}", eventLink.url());
        }
    }

    /**
     * Phase 3: Parses events from the filtered EventLinks.
     *
     * @param eventLinks EventLinks to parse
     * @return List of successfully parsed EventItem objects
     */
    private List<EventItem> parseEvents(final List<EventLink> eventLinks) {
        final List<EventItem> events = new ArrayList<>();
        int current = 0;
        for (final EventLink eventLink : eventLinks) {
            current++;
            try {
                parseAndAddEvent(eventLink, current, eventLinks.size(), events);
            } catch (final Exception e) {
                LOG.error("Failed to parse event from {}: {}", eventLink.url(), e.getMessage(),
                    e);
            }
        }
        return events;
    }

    /**
     * Resolves the final event date by preferring the listing page date over the detail page
     * date.
     *
     * @param event     Parsed event from detail page
     * @param eventLink EventLink containing date from listing page
     * @return EventItem with resolved date
     */
    private EventItem resolveEventDate(final EventItem event, final EventLink eventLink) {
        final LocalDate finalDate = eventLink.date() != null
            ? eventLink.date() : event.eventDateStart();

        if (eventLink.date() == null && event.eventDateStart() == null) {
            LOG.warn("No date found for event: {} - {}", event.title(), eventLink.url());
        } else if (eventLink.date() == null) {
            LOG.warn("Using fallback date from detail page for: {}", event.title());
        }

        return new EventItem(
            event.id(),
            event.title(),
            event.link(),
            event.description(),
            finalDate,
            null,
            event.imageUrl(),
            null
        );
    }

    @Override
    public List<EventItem> scrapeEvents(final Set<String> existingGuids) {
        requireNonNull(existingGuids, "existingGuids must not be null");

        LOG.info("Starting event scraping from: {}", config.getBaseUrl());

        try {
            // PHASE 1: Discover event URLs with dates
            final List<EventLink> allEventLinks = discoverEventUrls();
            LOG.info("Phase 1 complete: Discovered {} event links", allEventLinks.size());

            // PHASE 2: Filter to new URLs only
            final List<EventLink> newEventLinks = filterNewUrls(allEventLinks, existingGuids);
            LOG.info("Phase 2 complete: {} new events after filtering", newEventLinks.size());

            // PHASE 3: Parse each event
            final List<EventItem> events = parseEvents(newEventLinks);
            LOG.info("Phase 3 complete: Parsed {} events", events.size());

            return events;
        } catch (final Exception e) {
            LOG.error("Unable to parse events", e);
            return List.of();
        }
    }
}
