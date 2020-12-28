package org.shipkit.changelog;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

class GitLogProvider {

    private final static Logger LOG = Logging.getLogger(GitLogProvider.class);

    private final ProcessRunner runner;

    GitLogProvider(ProcessRunner runner) {
        this.runner = runner;
    }


    String getLog(String fromRev, String toRev, String format) {
        String fetch = "+refs/tags/" + fromRev + ":refs/tags/" + fromRev;
        String log = fromRev + ".." + toRev;

        try {
            runner.run("git", "fetch", "origin", fetch);
        } catch (Exception e) {
            //This is a non blocking problem because we still are able to run git log locally
            LOG.info("'git fetch' did not work, continuing running 'git log' locally.");
            //To avoid confusion, no stack trace in debug log, just the message:
            LOG.debug("'git fetch' problem: {}", e.getMessage());
        }
        return runner.run("git", "log", format, log);
    }
}
