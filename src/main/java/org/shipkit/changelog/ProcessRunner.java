package org.shipkit.changelog;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

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
            output = readFully(new BufferedReader(new InputStreamReader(process.getInputStream())));
            exitValue = process.waitFor();
        } catch (Exception e) {
            throw new RuntimeException("Problems executing command:\n  " + Arrays.toString(commandLine), e);
        }

        if (exitValue != 0) {
            throw new RuntimeException(
                    "Problems executing command (exit code: " + exitValue + "): " + Arrays.toString(commandLine) + "\n" +
                    "Output:\n" + output);
        }

        return output;
    }

    static String readFully(BufferedReader reader) throws IOException {
        try {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } finally {
            reader.close();
        }
    }
}
