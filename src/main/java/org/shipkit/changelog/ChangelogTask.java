package org.shipkit.changelog;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

public class ChangelogTask extends DefaultTask {

    private String fromRev;
    private String toRev;
    private File outputFile;

    @Internal
    public String getFromRev() {
        return fromRev;
    }

    public void setFromRev(String fromRev) {
        this.fromRev = fromRev;
    }

    @Internal
    public String getToRev() {
        return toRev;
    }

    public void setToRev(String toRev) {
        this.toRev = toRev;
    }

    @OutputFile
    public File getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    @TaskAction public void generateChangelog() {
        System.out.println("Generating changelog!");
    }
}
