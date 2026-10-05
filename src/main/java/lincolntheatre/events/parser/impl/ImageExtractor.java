package lincolntheatre.events.parser.impl;

import static lincolntheatre.events.parser.impl.CssSelectors.EVENT_IMAGE;
import static lincolntheatre.events.parser.impl.CssSelectors.IMG_SRC;
import static lincolntheatre.events.parser.impl.CssSelectors.META_OG_IMAGE;
import static lincolntheatre.events.parser.impl.HtmlConstants.CONTENT_ATTR;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Extracts event image URLs from Lincoln Theatre HTML.
 */
public final class ImageExtractor {
    private static final String ABS_SRC = "abs:src";

    /**
     * Extracts the event image URL using multiple strategies.
     *
     * @param doc the parsed HTML document
     * @return the extracted image URL, or null if not found
     */
    public String extractImageUrl(final Document doc) {
        String imageUrl = tryEventImage(doc);
        if (imageUrl != null) {
            return imageUrl;
        }

        imageUrl = tryMetaOgImage(doc);
        if (imageUrl != null) {
            return imageUrl;
        }

        imageUrl = tryFirstImage(doc);
        return imageUrl;
    }

    private String tryEventImage(final Document doc) {
        final Element img = doc.selectFirst(EVENT_IMAGE);
        if (img != null) {
            final String src = img.attr(ABS_SRC).trim();
            if (!src.isBlank()) {
                return src;
            }
        }
        return null;
    }

    private String tryFirstImage(final Document doc) {
        final Element img = doc.selectFirst(IMG_SRC);
        if (img != null) {
            final String src = img.attr(ABS_SRC).trim();
            if (!src.isBlank()) {
                return src;
            }
        }
        return null;
    }

    private String tryMetaOgImage(final Document doc) {
        final Element meta = doc.selectFirst(META_OG_IMAGE);
        if (meta != null) {
            final String content = meta.attr(CONTENT_ATTR).trim();
            if (!content.isBlank()) {
                return content;
            }
        }
        return null;
    }
}
