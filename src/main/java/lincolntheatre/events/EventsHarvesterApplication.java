package lincolntheatre.events;

import java.util.List;
import java.util.Set;
import lincolntheatre.events.config.ScraperConfiguration;
import lincolntheatre.events.config.impl.ScraperConfigurationImpl;
import lincolntheatre.events.domain.EventItem;
import lincolntheatre.events.feed.RssFeedManager;
import lincolntheatre.events.feed.RssFeedManagerImpl;
import lincolntheatre.events.parser.EventParser;
import lincolntheatre.events.parser.impl.EventParserImpl;
import lincolntheatre.events.scraper.EventScraper;
import lincolntheatre.events.scraper.impl.EventLinkDiscoverer;
import lincolntheatre.events.scraper.impl.EventScraperImpl;
import lincolntheatre.events.scraper.impl.PaginationParser;
import lincolntheatre.events.webdriver.ChromeDriverManager;
import lincolntheatre.events.webdriver.PageLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * Main application for scraping and generating RSS feed.
 */
public final class EventsHarvesterApplication {

    private static final String DEFAULT_OUTPUT_FILE = "events.xml";
    private static final Logger LOG =
        LoggerFactory.getLogger(EventsHarvesterApplication.class);

    private EventsHarvesterApplication() {
        // utility
    }

    private static void configureLogging() {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    /**
     * Main entry point for the application.
     *
     * @param args command-line arguments (optional: output filename)
     */
    public static void main(final String[] args) {
        configureLogging();

        final String outputFilename = args.length > 0 ? args[0] : DEFAULT_OUTPUT_FILE;

        LOG.info("Starting Lincoln Theatre Events Harvester");
        LOG.info("Output file: {}", outputFilename);

        final ScraperConfiguration config = new ScraperConfigurationImpl();
        final RssFeedManager feedManager = new RssFeedManagerImpl(config);

        try {
            LOG.info("Loading existing feed");
            final Set<String> existingGuids = feedManager.loadExistingGuids(outputFilename);
            LOG.info("Found {} existing events", existingGuids.size());

            final List<EventItem> newEvents;
            try (ChromeDriverManager driverManager = new ChromeDriverManager(config)) {
                final PageLoader pageLoader = new PageLoader(
                    driverManager.getDriver(),
                    config.getPageLoadTimeout()
                );
                final EventParser eventParser = new EventParserImpl(
                    config.useHtmlDescription()
                );

                final EventScraper scraper = new EventScraperImpl(
                    config,
                    pageLoader,
                    eventParser,
                    new PaginationParser(),
                    new EventLinkDiscoverer()
                );

                newEvents = scraper.scrapeEvents(existingGuids);
                LOG.info("Scraped {} new events", newEvents.size());
            }

            feedManager.generateFeed(
                outputFilename,
                newEvents,
                outputFilename
            );
            LOG.info("RSS feed generation complete");
            LOG.info("Harvesting completed successfully");
        } catch (Exception e) {
            LOG.error("Application failed", e);
            System.exit(1);
        }
    }
}
