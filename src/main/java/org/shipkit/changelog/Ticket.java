package org.shipkit.changelog;

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
}
