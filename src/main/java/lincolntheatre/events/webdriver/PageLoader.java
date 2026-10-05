package lincolntheatre.events.webdriver;

import static java.util.Objects.requireNonNull;

import java.time.Duration;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads web pages using Selenium and parses them with JSoup.
 */
public record PageLoader(WebDriver driver, Duration timeout) {
    private static final Logger LOG = LoggerFactory.getLogger(PageLoader.class);

    /**
     * Creates a new PageLoader with the given driver and timeout.
     *
     * @param driver  the WebDriver to use
     * @param timeout the page load timeout
     */
    public PageLoader(final WebDriver driver, final Duration timeout) {
        this.driver = requireNonNull(driver);
        this.timeout = requireNonNull(timeout);
    }

    /**
     * Loads a page and returns the parsed JSoup document.
     *
     * @param url      the URL to load
     * @param selector the CSS selector to wait for
     * @return the parsed JSoup document
     */
    public Document loadPage(final String url, final String selector) {
        requireNonNull(url, "url must not be null");
        requireNonNull(selector, "selector must not be null");
        LOG.info("Loading page: {}", url);
        driver.get(url);

        final WebDriverWait wait = new WebDriverWait(driver, timeout);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(selector)));

        final String pageSource = driver.getPageSource();
        return Jsoup.parse(requireNonNull(pageSource));
    }
}
