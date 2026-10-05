package lincolntheatre.events.scraper.impl;

import static lincolntheatre.events.parser.impl.CssSelectors.NEXT_LINK;
import static lincolntheatre.events.parser.impl.CssSelectors.PAGE_NUMBERS;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses pagination information from Lincoln Theatre event listing pages.
 */
public final class PaginationParser {
    private static final Logger LOG = LoggerFactory.getLogger(PaginationParser.class);

    /**
     * Determines the total number of pages from the pagination controls.
     *
     * @param doc the parsed HTML document
     * @return the total number of pages (at least 1)
     */
    public int getTotalPages(final Document doc) {
        try {
            final Elements pageLinks = doc.select(PAGE_NUMBERS);
            int maxPage = 1;

            for (Element link : pageLinks) {
                final String text = link.text().trim();
                try {
                    final int pageNum = Integer.parseInt(text);
                    maxPage = Math.max(maxPage, pageNum);
                } catch (NumberFormatException e) {
                    // Not a number, skip
                }
            }

            final Element nextLink = doc.selectFirst(NEXT_LINK);
            if (nextLink != null) {
                maxPage = Math.max(maxPage, 2);
            }

            LOG.info("Total pages: {}", maxPage);
            return maxPage;
        } catch (Exception e) {
            LOG.warn("Error parsing pagination, defaulting to 1 page", e);
            return 1;
        }
    }
}
