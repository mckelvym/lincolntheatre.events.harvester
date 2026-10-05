package lincolntheatre.events.scraper.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class EventLinkTest {

    @Test
    void eventLink_equality_differentDatesNotEqual() {
        String url = "https://lincolntheatre.com/event/concert/";

        EventLink link1 = new EventLink(url, LocalDate.of(2024, 12, 15));
        EventLink link2 = new EventLink(url, LocalDate.of(2024, 12, 16));

        assertThat(link1).isNotEqualTo(link2);
    }

    @Test
    void eventLink_equality_differentUrlsNotEqual() {
        LocalDate date = LocalDate.of(2024, 12, 15);

        EventLink link1 = new EventLink("https://lincolntheatre.com/event/one/", date);
        EventLink link2 = new EventLink("https://lincolntheatre.com/event/two/", date);

        assertThat(link1).isNotEqualTo(link2);
    }

    @Test
    void eventLink_equality_sameValuesAreEqual() {
        String url = "https://lincolntheatre.com/event/concert/";
        LocalDate date = LocalDate.of(2024, 12, 15);

        EventLink link1 = new EventLink(url, date);
        EventLink link2 = new EventLink(url, date);

        assertThat(link1).isEqualTo(link2);
        assertThat(link1.hashCode()).isEqualTo(link2.hashCode());
    }

    @Test
    void eventLink_toString_containsUrlAndDate() {
        String url = "https://lincolntheatre.com/event/concert/";
        LocalDate date = LocalDate.of(2024, 12, 15);

        EventLink link = new EventLink(url, date);

        String result = link.toString();

        assertThat(result).contains(url);
        assertThat(result).contains(date.toString());
    }

    @Test
    void eventLink_withNullDate_allowsNullDate() {
        String url = "https://lincolntheatre.com/event/concert/";

        EventLink link = new EventLink(url, null);

        assertThat(link.url()).isEqualTo(url);
        assertThat(link.date()).isNull();
    }

    @Test
    void eventLink_withValidUrlAndDate_createsInstance() {
        String url = "https://lincolntheatre.com/event/concert/2024-12-15/";
        LocalDate date = LocalDate.of(2024, 12, 15);

        EventLink link = new EventLink(url, date);

        assertThat(link.url()).isEqualTo(url);
        assertThat(link.date()).isEqualTo(date);
    }
}
