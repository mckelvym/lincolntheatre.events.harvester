package lincolntheatre.events.parser.impl;

import static lincolntheatre.events.parser.impl.CssSelectors.EVENT_TITLE_H2;
import static lincolntheatre.events.parser.impl.CssSelectors.H1;
import static lincolntheatre.events.parser.impl.CssSelectors.META_OG_TITLE;
import static lincolntheatre.events.parser.impl.CssSelectors.TITLE_CLASS;
import static lincolntheatre.events.parser.impl.HtmlConstants.CONTENT_ATTR;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts event titles from Lincoln Theatre HTML.
 */
public final class TitleExtractor {
    private static final Logger LOG = LoggerFactory.getLogger(TitleExtractor.class);

    /**
     * Extracts the event title using multiple strategies.
     *
     * @param doc the parsed HTML document
     * @return the extracted title, or null if not found
     */
    public String extractTitle(final Document doc) {
        String title = tryH1Selection(doc);
        if (title != null) {
            return title;
        }

        title = tryH2WithId(doc);
        if (title != null) {
            return title;
        }

        title = tryTitleClass(doc);
        if (title != null) {
            return title;
        }

        title = tryMetaOgTitle(doc);
        if (title != null) {
            return title;
        }

        LOG.warn("Could not extract title using any strategy");
        return null;
    }

    private String tryH1Selection(final Document doc) {
        final Element h1 = doc.selectFirst(H1);
        if (h1 != null) {
            final String text = h1.text().trim();
            if (!text.isBlank()) {
                return text;
            }
        }
        return null;
    }

    private String tryH2WithId(final Document doc) {
        final Element h2 = doc.selectFirst(EVENT_TITLE_H2);
        if (h2 != null) {
            final String text = h2.text().trim();
            if (!text.isBlank()) {
                return text;
            }
        }
        return null;
    }

    private String tryMetaOgTitle(final Document doc) {
        final Element meta = doc.selectFirst(META_OG_TITLE);
        if (meta != null) {
            final String content = meta.attr(CONTENT_ATTR).trim();
            if (!content.isBlank()) {
                return content;
            }
        }
        return null;
    }

    private String tryTitleClass(final Document doc) {
        final Element titleEl = doc.selectFirst(TITLE_CLASS);
        if (titleEl != null) {
            final String text = titleEl.text().trim();
            if (!text.isBlank()) {
                return text;
            }
        }
        return null;
    }
}
