package org.shipkit.changelog;

import java.io.File;
import java.util.Arrays;

import static org.shipkit.changelog.IOUtil.readFully;

class ProcessRunner {

    private final File workDir;

    ProcessRunner(File workDir) {
        this.workDir = workDir;
    }

    String run(String... commandLine) {
        int exitValue;
        String output;
        try {
            Process process = new ProcessBuilder(commandLine).directory(workDir).redirectErrorStream(true).start();
            output = readFully(process.getInputStream());
            exitValue = process.waitFor();
        } catch (Exception e) {
            throw new RuntimeException("Problems executing command:\n  " + Arrays.toString(commandLine), e);
        }

        if (exitValue != 0) {
            throw new RuntimeException(
                    "Problems executing command (exit code: " + exitValue + "):\n" +
                            "  command: " + String.join(" ", commandLine) + "\n" +
                            "  working dir: " + workDir.getAbsolutePath() + "\n" +
                    "  output:\n" + output);
        }

        return output;
    }
}
