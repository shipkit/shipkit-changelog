package org.shipkit.changelog;

import java.util.Collection;

/**
 * Simple POJO that contains all the information of an improvement
 */
public class Improvement {

    private static final String JSON_FORMAT = "{ \"id\": \"%s\", \"title\": \"%s\", \"url\": \"%s\", \"labels\": [%s], \"isPullRequest\": %s }";

    private final Long id;
    private final String title;
    private final String url;
    private final Collection<String> labels;
    private final boolean isPullRequest;

    public Improvement(Long id, String title, String url, Collection<String> labels, boolean isPullRequest) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.labels = labels;
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

    public Collection<String> getLabels() {
        return labels;
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
                ", labels=" + labels +
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

        Improvement that = (Improvement) o;

        if (isPullRequest != that.isPullRequest) {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        if (title != null ? !title.equals(that.title) : that.title != null) {
            return false;
        }
        if (url != null ? !url.equals(that.url) : that.url != null) {
            return false;
        }
        return labels != null ? labels.equals(that.labels) : that.labels == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (labels != null ? labels.hashCode() : 0);
        result = 31 * result + (isPullRequest ? 1 : 0);
        return result;
    }
}
