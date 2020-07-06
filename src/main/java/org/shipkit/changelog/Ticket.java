package org.shipkit.changelog;

/**
 * Simple POJO that contains all the information of an improvement
 */
public class Ticket {

    private final Long id;
    private final String title;
    private final String url;
    private final boolean isPullRequest;

    public Ticket(Long id, String title, String url, boolean isPullRequest) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.isPullRequest = isPullRequest;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public boolean isPullRequest() {
        return isPullRequest;
    }

    @Override
    public String toString() {
        return "DefaultImprovement{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", isPullRequest=" + isPullRequest +
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

        if (isPullRequest != that.isPullRequest) {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        if (title != null ? !title.equals(that.title) : that.title != null) {
            return false;
        }
        return url != null ? url.equals(that.url) : that.url == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (isPullRequest ? 1 : 0);
        return result;
    }
}
