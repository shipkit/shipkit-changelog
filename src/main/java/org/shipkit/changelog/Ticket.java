package org.shipkit.changelog;

import java.util.Objects;

/**
 * Simple POJO that contains all the information of an improvement
 */
class Ticket {

    private final Long id;
    private final String title;
    private final String url;

    Ticket(Long id, String title, String url) {
        this.id = id;
        this.title = title;
        this.url = url;
    }

    Long getId() {
        return id;
    }

    String getTitle() {
        return title;
    }

    String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Ticket that = (Ticket) o;

        if (!Objects.equals(id, that.id)) {
            return false;
        }
        if (!Objects.equals(title, that.title)) {
            return false;
        }
        return Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        return result;
    }
}
