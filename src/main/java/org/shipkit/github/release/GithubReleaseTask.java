package org.shipkit.github.release;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;
import org.shipkit.changelog.GithubApi;
import org.shipkit.changelog.IOUtil;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class GithubReleaseTask extends DefaultTask {

    private final static Logger LOG = Logging.getLogger(GithubReleaseTask.class);

    private String githubApiUrl = null;
    private String repository = null;
    private String releaseName = null;
    private String releaseTag = null;
    private File changelog = null;
    private String githubToken = null;
    private String newTagRevision = null;

    @Input
    public String getGithubApiUrl() {
        return githubApiUrl;
    }

    public void setGithubApiUrl(String githubApiUrl) {
        this.githubApiUrl = githubApiUrl;
    }

    @Input
    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    @Input
    public String getReleaseName() {
        return releaseName;
    }

    public void setReleaseName(String releaseName) {
        this.releaseName = releaseName;
    }

    /**
     * Release tag, for example "v1.2.3".
     * One of the parameters of the GitHub API call that creates GitHub release and the Git tag.
     */
    @Input
    public String getReleaseTag() {
        return releaseTag;
    }

    /**
     * See {@link #getReleaseTag()}
     */
    public void setReleaseTag(String releaseTag) {
        this.releaseTag = releaseTag;
    }

    @InputFile
    public File getChangelog() {
        return changelog;
    }

    public void setChangelog(File changelog) {
        this.changelog = changelog;
    }

    /**
     * Deprecated, please use {@link #getGithubToken()}
     */
    @Input
    @Deprecated
    public String getWriteToken() {
        return getGithubToken();
    }

    /**
     * Deprecated, please use {@link #setGithubToken(String)}
     */
    @Deprecated
    public void setWriteToken(String writeToken) {
        this.setGithubToken(writeToken);
    }

    /**
     * See {@link #setGithubToken(String)}
     */
    @Input
    public String getGithubToken() {
        return githubToken;
    }

    /**
     * Token required by Github API to post a new release.
     * This token should have *write* permission to the repo.
     *
     * @param githubToken token with write permissions
     */
    public void setGithubToken(String githubToken) {
        this.githubToken = githubToken;
    }

    /**
     * See {@link #setNewTagRevision(String)}
     */
    @Input
    public String getNewTagRevision() {
        return newTagRevision;
    }

    /**
     * Property required to specify revision for the new tag.
     * The property's value is passed to Github API's
     * 'target_commitish' parameter in {@link #postRelease()} method.
     */
    public void setNewTagRevision(String newTagRevision) {
        this.newTagRevision = newTagRevision;
    }

    @TaskAction public void postRelease() {
        String url = githubApiUrl + "/repos/" + repository + "/releases";

        JsonObject body = new JsonObject();
        body.add("tag_name", releaseTag);
        body.add("name", releaseName);
        body.add("target_commitish", newTagRevision);
        String releaseNotesTxt = IOUtil.readFully(changelog);
        body.add("body", releaseNotesTxt);

        GithubApi githubApi = new GithubApi(githubToken);

        try {
            LOG.lifecycle("Checking if release exists for tag {}...", releaseTag);
            Optional<Integer> existingRelease = existingRelease(githubApi, url, releaseTag);
            final String htmlUrl = performRelease(existingRelease, githubApi, url, body.toString());
            LOG.lifecycle("Posted release to Github: " + htmlUrl);
        } catch (IOException e) {
            throw new GradleException("Unable to post release to Github.\n" +
                    "  * url: " + url + "\n" +
                    "  * release tag: " + releaseTag + "\n" +
                    "  * release name: " + releaseName + "\n" +
                    "  * token: " + githubToken.substring(0, 3) + "...\n" +
                    "  * content:\n" + releaseNotesTxt + "\n\n" +
                    "  * underlying problem: " + e.getMessage() + "\n" +
                    "  * troubleshooting: please run Gradle with '-s' to see the full stack trace or inspect the build scan\n" +
                    "  * thank you for using Shipkit!"
                    , e);
        }
    }

    /**
     * Updates an existing release or creates a new release.
     * @param existingReleaseId if empty, new release will created.
     *                          If it contains release ID (internal GH identifier) it will update that release
     * @param githubApi the GH api object
     * @param url the url to use
     * @param body payload
     * @return String with JSON contents
     * @throws IOException when something goes wrong with REST call / HTTP connectivity
     */
    String performRelease(Optional<Integer> existingReleaseId, GithubApi githubApi, String url, String body) throws IOException {
        final String htmlUrl;
        if (existingReleaseId.isPresent()) {
            LOG.lifecycle("Release already exists for tag {}! Updating the release notes...", releaseTag);

            String response = githubApi.patch(url + "/" + existingReleaseId.get(), body);
            htmlUrl = Json.parse(response).asObject().getString("html_url", "");
        } else {
            String response = githubApi.post(url, body);
            htmlUrl = Json.parse(response).asObject().getString("html_url", "");
        }
        return htmlUrl;
    }

    /**
     * Finds out if the release for given tag already exists
     *
     * @param githubApi api object
     * @param url main REST url
     * @param releaseTag the tag name, will be appended to the url
     * @return existing release ID or empty optional if there is no release for the given tag
     * @throws IOException when something goes wrong with REST call / HTTP connectivity
     */
    Optional<Integer> existingRelease(GithubApi githubApi, String url, String releaseTag) throws IOException {
        try {
            GithubApi.Response r = githubApi.get(url + "/tags/" + releaseTag);
            JsonValue result = Json.parse(r.getContent());
            int releaseId = result.asObject().getInt("id", -1);
            return Optional.of(releaseId);
        } catch (GithubApi.ResponseException e) {
            return Optional.empty();
        }
    }
}
