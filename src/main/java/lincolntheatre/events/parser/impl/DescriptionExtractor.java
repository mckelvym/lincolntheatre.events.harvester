package lincolntheatre.events.parser.impl;

import static java.util.Objects.requireNonNull;
import static lincolntheatre.events.parser.impl.CssSelectors.DESCRIPTION_CLASS;
import static lincolntheatre.events.parser.impl.CssSelectors.META_DESCRIPTION;
import static lincolntheatre.events.parser.impl.CssSelectors.SUBHEADER;
import static lincolntheatre.events.parser.impl.HtmlConstants.CONTENT_ATTR;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Extracts event descriptions from Lincoln Theatre HTML.
 */
public final class DescriptionExtractor {
    private final boolean preserveHtml;

    /**
     * Creates a new DescriptionExtractor.
     *
     * @param preserveHtml true to preserve HTML, false for plain text
     */
    public DescriptionExtractor(final boolean preserveHtml) {
        this.preserveHtml = preserveHtml;
    }

    /**
     * Extracts the event description using multiple strategies.
     *
     * @param doc the parsed HTML document
     * @return the extracted description, or null if not found
     */
    public String extract(final Document doc) {
        requireNonNull(doc);
        String description = tryMetaDescription(doc);
        if (description != null) {
            return description;
        }

        description = trySubheaderClass(doc);
        if (description != null) {
            return description;
        }

        description = tryDescriptionClass(doc);
        return description;
    }

    private String tryDescriptionClass(final Document doc) {
        final Element descEl = doc.selectFirst(DESCRIPTION_CLASS);
        if (descEl != null) {
            final String content = preserveHtml ? descEl.html().trim() : descEl.text().trim();
            if (!content.isBlank()) {
                return content;
            }
        }
        return null;
    }

    private String tryMetaDescription(final Document doc) {
        final Element meta = doc.selectFirst(META_DESCRIPTION);
        if (meta != null) {
            final String content = meta.attr(CONTENT_ATTR).trim();
            if (!content.isBlank()) {
                return content;
            }
        }
        return null;
    }

    private String trySubheaderClass(final Document doc) {
        final Element subheader = doc.selectFirst(SUBHEADER);
        if (subheader != null) {
            final String content = preserveHtml ? subheader.html().trim() : subheader.text().trim();
            if (!content.isBlank()) {
                return content;
            }
        }
        return null;
    }
}
