package org.shipkit.gh.release;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;
import org.shipkit.changelog.GitHubApi;
import org.shipkit.changelog.IOUtil;

import java.io.File;
import java.io.IOException;

public class GitHubReleaseTask extends DefaultTask {

    private final static Logger LOG = Logging.getLogger(GitHubReleaseTask.class);

    private String ghApiUrl = null;
    private String repository = null;
    private String releaseName = null;
    private String releaseTag = null;
    private File changelog = null;
    private String writeToken = null;
    private String newTagRevision = null;

    @Input
    public String getGhApiUrl() {
        return ghApiUrl;
    }

    public void setGhApiUrl(String ghApiUrl) {
        this.ghApiUrl = ghApiUrl;
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

    @Input
    public String getReleaseTag() {
        return releaseTag;
    }

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

    @Input
    public String getWriteToken() {
        return writeToken;
    }

    public void setWriteToken(String writeToken) {
        this.writeToken = writeToken;
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
     * The property's value is passed to GitHub API's
     * 'target_commitish' parameter in {@link #postRelease()} method.
     */
    public void setNewTagRevision(String newTagRevision) {
        this.newTagRevision = newTagRevision;
    }

    @TaskAction public void postRelease() {
        String url = ghApiUrl + "/repos/" + repository + "/releases";

        JsonObject body = new JsonObject();
        body.add("tag_name", releaseTag);
        body.add("name", releaseName);
        body.add("target_commitish", newTagRevision);
        String releaseNotesTxt = IOUtil.readFully(changelog);
        body.add("body", releaseNotesTxt);

        GitHubApi gitHubApi = new GitHubApi(writeToken);
        try {
            String response = gitHubApi.post(url, body.toString());
            String htmlUrl = Json.parse(response).asObject().getString("html_url", "");
            LOG.lifecycle("Posted release to GitHub: " + htmlUrl);
        } catch (IOException e) {
            throw new GradleException("Unable to post release to GitHub.\n" +
                    "  - url: " + url + "\n" +
                    "  - release tag: " + releaseTag + "\n" +
                    "  - release name: " + releaseName + "\n" +
                    "  - token: " + writeToken.substring(0, 3) + "...\n" +
                    "  - content:\n" + releaseNotesTxt + "\n"
                    , e);
        }
    }
}
