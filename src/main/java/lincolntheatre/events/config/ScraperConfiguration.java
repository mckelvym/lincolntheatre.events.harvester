package lincolntheatre.events.config;

import java.time.Duration;

/**
 * Configuration interface for event scraping operations.
 */
public interface ScraperConfiguration {
    /**
     * Returns the base URL for the events listing page.
     *
     * @return the base URL
     */
    String getBaseUrl();

    /**
     * Returns the RSS feed description.
     *
     * @return feed description
     */
    String getFeedDescription();

    /**
     * Returns the RSS feed link.
     *
     * @return feed link URL
     */
    String getFeedLink();

    /**
     * Returns the RSS feed title.
     *
     * @return feed title
     */
    String getFeedTitle();

    /**
     * Gets the timeout duration for page loads.
     *
     * @return the timeout duration
     */
    Duration getPageLoadTimeout();

    /**
     * Returns the number of days to retain events before filtering them out.
     *
     * @return the retention period in days
     */
    int getRetentionDays();

    /**
     * Returns the user agent string to use for web requests.
     *
     * @return the user agent string
     */
    String getUserAgent();

    /**
     * Returns whether to preserve HTML in event descriptions.
     *
     * @return true to preserve HTML, false to extract plain text
     */
    boolean useHtmlDescription();
}
