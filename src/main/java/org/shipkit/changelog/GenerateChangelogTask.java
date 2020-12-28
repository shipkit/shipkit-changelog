package org.shipkit.changelog;

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.*;

import java.io.File;
import java.util.*;

/**
 * Generates changelog based on the GitHub ticked ids found in commit messages.
 */
public class GenerateChangelogTask extends DefaultTask {

    private final static Logger LOG = Logging.getLogger(GenerateChangelogTask.class);

    private String ghUrl;
    private File outputFile;
    private File workingDir;
    private String githubToken;
    private String ghApiUrl;
    private String repository;
    private String previousRevision;
    private String version;
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
    public String getGhUrl() {
        return ghUrl;
    }

    public void setGhUrl(String ghUrl) {
        this.ghUrl = ghUrl;
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
    public String getGhApiUrl() {
        return ghApiUrl;
    }

    public void setGhApiUrl(String ghApiUrl) {
        this.ghApiUrl = ghApiUrl;
    }

    @OutputFile
    public File getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    @InputDirectory
    public File getWorkingDir() {
        return workingDir;
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
    public String getGithubToken() {
        return githubToken;
    }

    /**
     * GitHub token used to pull GitHub issues.
     * The same token is used to post a new release:
     * {@link org.shipkit.gh.release.GitHubReleaseTask#setGithubToken(String)}
     */
    public void setGithubToken(String githubToken) {
        this.githubToken = githubToken;
    }

    @TaskAction public void generateChangelog() {
        ProcessRunner runner = new ProcessRunner(workingDir);
        GitLogProvider logProvider = new GitLogProvider(runner);

        String previousRevision = this.previousRevision != null? this.previousRevision : "master";
        LOG.lifecycle("Finding commits between {}..{} in dir: {}", previousRevision, revision, workingDir);
        Collection<GitCommit> commits = new GitCommitProvider(logProvider).getCommits(previousRevision, revision);

        LOG.lifecycle("Collecting ticket ids from {} commits.", commits.size());
        List<String> ticketIds = new LinkedList<>();
        Set<String> contributors = new TreeSet<>();
        for (GitCommit c : commits) {
            ticketIds.addAll(c.getTickets());
            contributors.add(c.getAuthor());
        }

        LOG.lifecycle("Fetching ticket info from {}/{} based on {} ids {}", ghApiUrl, repository, ticketIds.size(), ticketIds);

        GitHubTicketFetcher fetcher = new GitHubTicketFetcher(ghApiUrl, repository, githubToken);
        Collection<Ticket> improvements = fetcher.fetchTickets(ticketIds);

        LOG.lifecycle("Generating changelog based on {} tickets from GitHub", improvements.size());
        String changelog = ChangelogFormat.formatChangelog(contributors, improvements, commits.size(), version,
                previousRevision, ghUrl + "/" + repository, date);

        LOG.lifecycle("Saving changelog to file: {}", outputFile);
        IOUtil.writeFile(outputFile, changelog.trim());
    }
}
