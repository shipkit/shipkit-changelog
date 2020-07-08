package org.shipkit.changelog;

class GitLogProvider {

    private final ProcessRunner runner;

    GitLogProvider(ProcessRunner runner) {
        this.runner = runner;
    }

    public String getLog(String fromRev, String toRev, String format) {
        String fetch = "+refs/tags/" + fromRev + ":refs/tags/" + fromRev;
        String log = fromRev + ".." + toRev;

        runner.run("git", "fetch", "origin", fetch);
        return runner.run("git", "log", format, log);
    }
}
