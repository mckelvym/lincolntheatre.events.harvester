package lincolntheatre.events.parser;


import java.util.Optional;
import lincolntheatre.events.domain.EventItem;
import org.jsoup.nodes.Document;

/**
 * Interface for parsing event information from HTML elements.
 */
public interface EventParser {
    /**
     * Parses an event from the given URL and HTML document.
     *
     * @param doc      the parsed HTML document
     * @param eventUrl the event URL
     * @return an Optional containing the parsed EventItem, or empty if parsing fails
     */
    Optional<EventItem> parseEvent(Document doc, String eventUrl);
}
