package lincolntheatre.events.feed;

import static java.util.Objects.requireNonNull;
import static lincolntheatre.events.feed.RssElementNames.CHANNEL;
import static lincolntheatre.events.feed.RssElementNames.DESCRIPTION;
import static lincolntheatre.events.feed.RssElementNames.ENCLOSURE;
import static lincolntheatre.events.feed.RssElementNames.ENCODING_UTF8;
import static lincolntheatre.events.feed.RssElementNames.EVENT_NAMESPACE_URI;
import static lincolntheatre.events.feed.RssElementNames.EV_ENDDATE;
import static lincolntheatre.events.feed.RssElementNames.EV_STARTDATE;
import static lincolntheatre.events.feed.RssElementNames.GUID;
import static lincolntheatre.events.feed.RssElementNames.IMAGE_JPEG_TYPE;
import static lincolntheatre.events.feed.RssElementNames.INDENT_AMOUNT;
import static lincolntheatre.events.feed.RssElementNames.IS_PERMALINK_ATTR;
import static lincolntheatre.events.feed.RssElementNames.ITEM;
import static lincolntheatre.events.feed.RssElementNames.LANGUAGE;
import static lincolntheatre.events.feed.RssElementNames.LANGUAGE_VALUE;
import static lincolntheatre.events.feed.RssElementNames.LAST_BUILD_DATE;
import static lincolntheatre.events.feed.RssElementNames.LINK;
import static lincolntheatre.events.feed.RssElementNames.PUB_DATE;
import static lincolntheatre.events.feed.RssElementNames.RSS;
import static lincolntheatre.events.feed.RssElementNames.RSS_VERSION;
import static lincolntheatre.events.feed.RssElementNames.TITLE;
import static lincolntheatre.events.feed.RssElementNames.TRUE_VALUE;
import static lincolntheatre.events.feed.RssElementNames.TYPE_ATTR;
import static lincolntheatre.events.feed.RssElementNames.URL_ATTR;
import static lincolntheatre.events.feed.RssElementNames.VERSION_ATTR;
import static lincolntheatre.events.feed.RssElementNames.XMLNS_EV_ATTR;
import static lincolntheatre.events.feed.RssElementNames.XSLT_INDENT_PROPERTY;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import lincolntheatre.events.config.ScraperConfiguration;
import lincolntheatre.events.domain.EventItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * RSS feed manager implementation with XML security.
 */
public class RssFeedManagerImpl implements RssFeedManager {
    private static final Logger LOG = LoggerFactory.getLogger(RssFeedManagerImpl.class);
    private static final DateTimeFormatter RFC_822_FORMATTER =
        DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z");
    private final ScraperConfiguration config;
    private final EventFilter eventFilter;
    private final XmlSecurityConfigurer securityConfigurer;

    /**
     * Creates a new RssFeedManagerImpl.
     *
     * @param config the scraper configuration
     * @throws NullPointerException if config is null
     */
    public RssFeedManagerImpl(final ScraperConfiguration config) {
        this.config = requireNonNull(config, "config must not be null");
        this.securityConfigurer = new XmlSecurityConfigurer();
        this.eventFilter = new EventFilter(config);
    }

    private void addChannelMetadata(final Document doc, final Element channel) {
        addTextElement(doc, channel, TITLE, config.getFeedTitle());
        addTextElement(doc, channel, LINK, config.getFeedLink());
        addTextElement(doc, channel, DESCRIPTION, config.getFeedDescription());
        addTextElement(doc, channel, LANGUAGE, LANGUAGE_VALUE);
        addTextElement(doc, channel, LAST_BUILD_DATE,
            ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
    }

    /**
     * Adds a description element wrapped in CDATA, omitting it when empty.
     *
     * @param doc         the XML document
     * @param item        the item element to add to
     * @param description the description HTML or text
     */
    private void addDescriptionElement(final Document doc, final Element item,
                                       final String description) {
        if (description.isEmpty()) {
            return;
        }
        final Element element = doc.createElement(DESCRIPTION);
        element.appendChild(doc.createCDATASection(description));
        item.appendChild(element);
    }

    /**
     * Adds the machine-readable event dates (RSS Event module) used for retention.
     *
     * @param doc   the XML document
     * @param item  the item element to add to
     * @param event the event whose dates to add
     */
    private void addEventDateElements(final Document doc, final Element item,
                                      final EventItem event) {
        final Element startDate = doc.createElement(EV_STARTDATE);
        startDate.setTextContent(event.eventDateStart().toString());
        item.appendChild(startDate);
        if (event.eventDateEnd() != null) {
            final Element endDate = doc.createElement(EV_ENDDATE);
            endDate.setTextContent(event.eventDateEnd().toString());
            item.appendChild(endDate);
        }
    }

    private void addEventItem(
        final Document doc,
        final Element channel,
        final EventItem event
    ) {
        final Element item = doc.createElement(ITEM);

        final String title = formatTitleWithDate(event);
        addTextElement(doc, item, TITLE, title);
        addTextElement(doc, item, LINK, event.link());
        addGuidElement(doc, item, event);
        addEventDateElements(doc, item, event);

        addDescriptionElement(doc, item, event.sanitizedDescription());

        // pubDate reflects the harvest time (when the event was added to the feed)
        final String pubDate = ZonedDateTime.now()
            .format(RFC_822_FORMATTER);
        addTextElement(doc, item, PUB_DATE, pubDate);

        if (event.hasImage()) {
            final Element enclosure = doc.createElement(ENCLOSURE);
            enclosure.setAttribute(URL_ATTR, event.imageUrl());
            enclosure.setAttribute(TYPE_ATTR, IMAGE_JPEG_TYPE);
            item.appendChild(enclosure);
        }

        channel.appendChild(item);
    }

    /**
     * Adds the item GUID, which is always the event URL and therefore a permalink.
     *
     * @param doc   the XML document
     * @param item  the item element to add to
     * @param event the event whose GUID to add
     */
    private void addGuidElement(final Document doc, final Element item, final EventItem event) {
        final Element guid = doc.createElement(GUID);
        guid.setAttribute(IS_PERMALINK_ATTR, TRUE_VALUE);
        guid.setTextContent(event.guid());
        item.appendChild(guid);
    }

    private void addTextElement(
        final Document doc,
        final Element parent,
        final String tagName,
        final String textContent
    ) {
        final Element element = doc.createElement(tagName);
        element.setTextContent(textContent);
        parent.appendChild(element);
    }

    private String formatTitleWithDate(final EventItem event) {
        if (event.eventDateStart() == null) {
            return event.title();
        }
        return event.title() + " (" + event.eventDateStart() + ")";
    }

    @Override
    public void generateFeed(
        final String filePath,
        final List<EventItem> newEvents,
        final String existingFilePath
    )
        throws Exception {
        requireNonNull(filePath, "filePath must not be null");
        requireNonNull(newEvents, "newEvents must not be null");
        requireNonNull(existingFilePath, "existingFilePath must not be null");

        final File feedFile = new File(filePath);
        final File existingFeedFile = new File(existingFilePath);

        final DocumentBuilderFactory factory =
            securityConfigurer.createSecureDocumentBuilderFactory();

        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document doc = builder.newDocument();

        final Element rss = doc.createElement(RSS);
        rss.setAttribute(VERSION_ATTR, RSS_VERSION);
        rss.setAttribute(XMLNS_EV_ATTR, EVENT_NAMESPACE_URI);
        doc.appendChild(rss);

        final Element channel = doc.createElement(CHANNEL);
        rss.appendChild(channel);

        addChannelMetadata(doc, channel);

        // Add new events (sorted by eventDateStart descending), skipping any past retention
        List<EventItem> sortedEvents = new ArrayList<>(newEvents);
        sortedEvents.sort(Comparator.comparing(EventItem::eventDateStart).reversed());

        for (EventItem event : sortedEvents) {
            if (eventFilter.shouldKeep(event)) {
                addEventItem(doc, channel, event);
            }
        }

        importExistingEvents(doc, channel, existingFeedFile);

        writeXmlToFile(doc, feedFile);

        LOG.info("Generated RSS feed with {} items to {}",
            channel.getElementsByTagName(ITEM).getLength(), filePath);
    }

    /**
     * Imports items from the existing feed, dropping those past the retention period.
     *
     * <p>Errors are logged rather than thrown so a scheduled run still publishes new events.
     *
     * @param doc              the new feed document
     * @param channel          the channel to append items to
     * @param existingFeedFile the existing feed file (may not exist)
     */
    private void importExistingEvents(final Document doc, final Element channel,
                                      final File existingFeedFile) {
        if (!existingFeedFile.exists()) {
            return;
        }
        try {
            final DocumentBuilder builder =
                securityConfigurer.createSecureDocumentBuilderFactory().newDocumentBuilder();
            final NodeList items = builder.parse(existingFeedFile).getElementsByTagName(ITEM);
            int imported = 0;
            for (int i = 0; i < items.getLength(); i++) {
                final Element item = (Element) items.item(i);
                if (eventFilter.shouldKeep(item)) {
                    final Node importedNode = doc.importNode(item, true);
                    removeWhitespaceNodes(importedNode);
                    channel.appendChild(importedNode);
                    imported++;
                }
            }
            LOG.info("Imported {} existing events, dropped {} past retention",
                imported, items.getLength() - imported);
        } catch (final Exception e) {
            LOG.error("Failed to import existing events from {}: {}",
                existingFeedFile, e.getMessage(), e);
        }
    }

    @Override
    public Set<String> loadExistingGuids(final String filePath)
        throws Exception {
        requireNonNull(filePath, "filePath must not be null");
        final Set<String> guids = new HashSet<>();
        final File feedFile = new File(filePath);

        if (!feedFile.exists()) {
            LOG.info("Feed file does not exist: {}", filePath);
            return guids;
        }

        final DocumentBuilderFactory factory =
            securityConfigurer.createSecureDocumentBuilderFactory();

        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document doc = builder.parse(feedFile);

        final NodeList items = doc.getElementsByTagName(ITEM);
        for (int i = 0; i < items.getLength(); i++) {
            final Element item = (Element) items.item(i);
            final NodeList guidNodes = item.getElementsByTagName(GUID);
            if (guidNodes.getLength() > 0) {
                final String guid = guidNodes.item(0).getTextContent();
                guids.add(guid);
            }
        }

        LOG.info("Loaded {} existing GUIDs from {}", guids.size(), filePath);
        return guids;
    }

    /**
     * Removes whitespace-only text nodes from a DOM tree.
     *
     * <p>This is necessary to ensure proper indentation when writing XML
     * and prevents whitespace accumulation across multiple runs.
     *
     * @param node The root node to clean
     */
    private void removeWhitespaceNodes(final Node node) {
        final Deque<Node> stack = new ArrayDeque<>();
        stack.push(node);

        while (!stack.isEmpty()) {
            final Node current = stack.pop();
            final NodeList children = current.getChildNodes();

            for (int i = children.getLength() - 1; i >= 0; i--) {
                final Node child = children.item(i);
                if (child.getNodeType() == Node.TEXT_NODE) {
                    if (child.getTextContent().trim().isEmpty()) {
                        current.removeChild(child);
                    }
                } else if (child.getNodeType() == Node.ELEMENT_NODE) {
                    stack.push(child);
                }
            }
        }
    }

    private void writeXmlToFile(final Document doc, final File file)
        throws IOException, TransformerException {
        final TransformerFactory transformerFactory =
            securityConfigurer.createSecureTransformerFactory();

        final Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(XSLT_INDENT_PROPERTY, INDENT_AMOUNT);
        transformer.setOutputProperty(OutputKeys.ENCODING, ENCODING_UTF8);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            transformer.transform(new DOMSource(doc), new StreamResult(fos));
        }
    }
}
