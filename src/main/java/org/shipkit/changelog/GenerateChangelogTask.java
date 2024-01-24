package org.shipkit.changelog;

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.*;
import org.shipkit.github.release.GithubReleaseTask;

import java.io.File;
import java.util.*;

/**
 * Generates changelog based on the Github ticked ids found in commit messages.
 */
public class GenerateChangelogTask extends DefaultTask {

    private final static Logger LOG = Logging.getLogger(GenerateChangelogTask.class);

    private String githubUrl;
    private File outputFile;
    private File workingDir;
    private String githubToken;
    private String githubApiUrl;
    private String repository;
    private String previousRevision;
    private String version;
    private String releaseTag;
    private String revision;
    private String date;

    /**
     * The release date
     */
    @Input
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Input
    public String getGithubUrl() {
        return githubUrl;
    }

    public void setGithubUrl(String githubUrl) {
        this.githubUrl = githubUrl;
    }

    /**
     * Previous revision for changelog generation.
     * The changelog is generated between {@code #getPreviousRevision()} and {@link #getRevision()}.
     *
     * This property is marked as {@code Optional} because the {@code null} value is permitted.
     * In this case the task will use "HEAD" as previous revision.
     * This way, the task behaves gracefully when generating changelog for the first time (very first version).
     */
    @Input
    @Optional
    public String getPreviousRevision() {
        return previousRevision;
    }

    /**
     * See {@link #getPreviousRevision()}
     */
    public void setPreviousRevision(String previousRevision) {
        this.previousRevision = previousRevision;
    }

    @Input
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Release tag, for example "v1.2.3".
     * It is used to construct a GitHub link to a diff between previous revision and the new release tag.
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

    /**
     * Target revision for changelog generation.
     * The changelog is generated between {@link #getPreviousRevision()} and {@code #getRevision()}.
     */
    @Input
    public String getRevision() {
        return revision;
    }

    /**
     * See {@link #getRevision()}
     */
    public void setRevision(String revision) {
        this.revision = revision;
    }

    @Input
    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    @Input
    public String getGithubApiUrl() {
        return githubApiUrl;
    }

    public void setGithubApiUrl(String githubApiUrl) {
        this.githubApiUrl = githubApiUrl;
    }

    @OutputFile
    public File getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    @Internal
    public File getWorkingDir() {
        return workingDir;
    }

    @Optional
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public File getGitDir() {
        return provideGitDir(workingDir);
    }

    static File provideGitDir(File workingDir) {
        if (workingDir == null) {
            return null;
        }

        File gitDir = new File(workingDir, ".git");
        return gitDir.isDirectory() ? gitDir : null;
    }

    public void setWorkingDir(File workingDir) {
        this.workingDir = workingDir;
    }

    /**
     * Deprecated, please use {@link #getGithubToken()}
     */
    @Input
    @Optional
    @Deprecated
    public String getReadOnlyToken() {
        return getGithubToken();
    }

    /**
     * Deprecated, please use {@link #setGithubToken(String)}
     */
    @Deprecated
    public void setReadOnlyToken(String readOnlyToken) {
        this.setGithubToken(readOnlyToken);
    }

    /**
     * See {@link #setGithubToken(String)}
     */
    @Input
    @Optional
    public String getGithubToken() {
        return githubToken;
    }

    /**
     * Github token used to pull Github issues.
     * The same token is used to post a new release:
     * {@link GithubReleaseTask#setGithubToken(String)}
     */
    public void setGithubToken(String githubToken) {
        this.githubToken = githubToken;
    }

    @TaskAction public void generateChangelog() {
        ProcessRunner runner = new ProcessRunner(workingDir);
        GitLogProvider logProvider = new GitLogProvider(runner);

        Collection<GitCommit> commits = new LinkedList<>();
        Collection<Ticket> improvements = new LinkedList<>();
        Set<String> contributors = new TreeSet<>();

        if (previousRevision != null) {
            LOG.lifecycle("Finding commits between {}..{} in dir: {}", previousRevision, revision, workingDir);
            commits = new GitCommitProvider(logProvider).getCommits(previousRevision, revision);

            LOG.lifecycle("Collecting ticket ids from {} commits.", commits.size());
            List<String> ticketIds = new LinkedList<>();
            for (GitCommit c : commits) {
                ticketIds.addAll(c.getTickets());
                contributors.add(c.getAuthor());
            }

            LOG.lifecycle("Fetching ticket info from {}/{} based on {} ids {}", githubApiUrl, repository, ticketIds.size(), ticketIds);

            GithubTicketFetcher fetcher = new GithubTicketFetcher(githubApiUrl, repository, githubToken);
            improvements = fetcher.fetchTickets(ticketIds);
        }

        LOG.lifecycle("Generating changelog based on {} tickets from Github", improvements.size());
        String changelog = ChangelogFormat.formatChangelog(contributors, improvements, commits.size(), releaseTag, version,
                previousRevision, githubUrl + "/" + repository, date);

        LOG.lifecycle("Saving changelog to file: {}", outputFile);
        IOUtil.writeFile(outputFile, changelog.trim());
    }
}
