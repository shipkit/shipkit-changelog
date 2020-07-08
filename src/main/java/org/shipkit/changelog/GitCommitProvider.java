package org.shipkit.changelog;

import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Logger;

class GitCommitProvider {

    private static final Logger LOG = Logger.getLogger(GitCommitProvider.class.getName());
    private final GitLogProvider logProvider;

    GitCommitProvider(GitLogProvider logProvider) {
        this.logProvider = logProvider;
    }

    Collection<GitCommit> getCommits(String fromRev, String toRev) {
        LOG.info("Loading all commits between " + fromRev + " and " + toRev);

        LinkedList<GitCommit> commits = new LinkedList<>();
        String commitToken = "@@commit@@";
        String infoToken = "@@info@@";
        // %H: commit hash
        // %ae: author email
        // %an: author name
        // %B: raw body (unwrapped subject and body)
        // %N: commit notes
        String log = logProvider.getLog(fromRev, toRev, "--pretty=format:%H" + infoToken + "%ae" + infoToken + "%an" + infoToken + "%B%N" + commitToken);

        for (String entry : log.split(commitToken)) {
            String[] entryParts = entry.split(infoToken);
            if (entryParts.length == 4) {
                String author = entryParts[2].trim();
                String message = entryParts[3].trim();
                commits.add(new GitCommit(author, message));
            }
        }
        return commits;
    }
}
