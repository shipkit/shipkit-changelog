package org.shipkit.changelog;

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.*;

import static java.util.Arrays.asList;

/**
 * Generates changelog based on the GitHub ticked ids found in commit messages.
 */
public class GenerateChangelogTask extends DefaultTask {

    private final static Logger LOG = Logging.getLogger(GenerateChangelogTask.class);

    private String ghUrl;
    private File outputFile;
    private File workingDir;
    private String readOnlyToken;
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

    @Input
    public String getPreviousRevision() {
        return previousRevision;
    }

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

    @Input
    public String getRevision() {
        return revision;
    }

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

    @Input
    public File getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(File workingDir) {
        this.workingDir = workingDir;
    }

    /**
     * Read-only GitHub token used to pull GitHub issues
     */
    @Input
    public String getReadOnlyToken() {
        return readOnlyToken;
    }

    public void setReadOnlyToken(String readOnlyToken) {
        this.readOnlyToken = readOnlyToken;
    }

    @TaskAction public void generateChangelog() {
        ProcessRunner runner = new ProcessRunner(workingDir);
        GitLogProvider logProvider = new GitLogProvider(runner);

        LOG.lifecycle("Finding commits between {}..{} in dir: {}", previousRevision, revision, workingDir);
        Collection<GitCommit> commits = new GitCommitProvider(logProvider).getCommits(previousRevision, revision);

        LOG.lifecycle("Collecting ticket ids from {} commits.", commits.size());
        List<String> tickets = new LinkedList<>();
        Set<String> contributors = new TreeSet<>();
        for (GitCommit c : commits) {
            tickets.addAll(c.getTickets());
            contributors.add(c.getAuthorName());
        }

        LOG.lifecycle("Fetching ticket info from {}/{} based on {} ids {}", ghApiUrl, repository, tickets.size(), tickets);

        GitHubTicketFetcher fetcher = new GitHubTicketFetcher();
        Collection<Ticket> improvements = fetcher.fetchTickets(ghApiUrl, repository, readOnlyToken,
                tickets, asList(), false);

        LOG.lifecycle("Generating changelog based on {} tickets from GitHub", improvements.size());
        String changelog = ChangelogFormat.formatChangelog(contributors, improvements, commits.size(), version,
                revision, previousRevision, ghUrl + "/" + repository, date);

        LOG.lifecycle("Saving changelog to file: {}", outputFile);
        IOUtil.writeFile(outputFile, changelog.trim());
    }
}