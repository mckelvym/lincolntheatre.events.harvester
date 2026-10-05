package lincolntheatre.events.parser.impl;


import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.Optional;
import lincolntheatre.events.domain.EventItem;
import lincolntheatre.events.parser.EventParser;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses and extracts event fields.
 */
public final class EventParserImpl implements EventParser {
    private static final Logger LOG = LoggerFactory.getLogger(EventParserImpl.class);
    private final DateExtractor dateExtractor;
    private final DescriptionExtractor descriptionExtractor;
    private final ImageExtractor imageExtractor;
    private final TitleExtractor titleExtractor;

    /**
     * Creates a new EventParserImpl with default extractors.
     *
     * @param preserveHtmlDescription true to preserve HTML in descriptions
     */
    public EventParserImpl(final boolean preserveHtmlDescription) {
        this.titleExtractor = new TitleExtractor();
        this.dateExtractor = new DateExtractor();
        this.descriptionExtractor = new DescriptionExtractor(preserveHtmlDescription);
        this.imageExtractor = new ImageExtractor();
    }

    @Override
    public Optional<EventItem> parseEvent(final Document doc, final String eventUrl) {
        requireNonNull(doc, "doc must not be null");
        requireNonNull(eventUrl, "eventUrl must not be null");
        final String title = titleExtractor.extractTitle(doc);
        if (title == null || title.isBlank()) {
            LOG.warn("Could not extract title from {}", eventUrl);
            return Optional.empty();
        }

        final LocalDate date = dateExtractor.extractLocalDate(doc);
        final String description = descriptionExtractor.extract(doc);
        final String imageUrl = imageExtractor.extractImageUrl(doc);

        return Optional.of(new EventItem(
            eventUrl,
            title,
            eventUrl,
            description,
            date,
            null,
            imageUrl,
            null
        ));
    }
}
