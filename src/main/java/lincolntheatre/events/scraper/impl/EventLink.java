package lincolntheatre.events.scraper.impl;

import java.time.LocalDate;

/**
 * Simple record to hold event link and date from listing page.
 *
 * @param url  the event URL
 * @param date the event date from the listing
 */
public record EventLink(String url, LocalDate date) {
}
