package org.shipkit.changelog;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.specs.Specs;

import java.io.File;

/**
 * The plugin, ideally with zero business logic, but only the Gradle integration code
 */
public class ChangelogPlugin implements Plugin<Project> {

    private static final Logger LOG = Logging.getLogger(ChangelogPlugin.class);

    public void apply(Project project) {
        project.getTasks().register("generateChangelog", GenerateChangelogTask.class, t -> {
            t.setRevision("HEAD");
            t.setOutputFile(new File(project.getBuildDir(), "changelog.md"));
            t.setGhApiUrl("https://api.github.com");
            t.setGhUrl("https://github.com");
            t.setWorkingDir(project.getProjectDir());
            t.setVersion("" + project.getVersion());
            t.getOutputs().upToDateWhen(Specs.satisfyNone()); //depends on state of Git repo, GitHub, etc.
        });
    }
}
