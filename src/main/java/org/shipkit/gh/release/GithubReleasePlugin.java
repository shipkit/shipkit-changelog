package org.shipkit.gh.release;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * The plugin, ideally with zero business logic, but only the Gradle integration code
 */
public class GithubReleasePlugin implements Plugin<Project> {

    public void apply(Project project) {
        project.getTasks().register("githubRelease", GithubReleaseTask.class, t -> {
            t.setGhApiUrl("https://api.github.com");
            String tagName = "v" + project.getVersion();
            t.setReleaseTag(tagName);
            t.setReleaseName(tagName);
        });
    }
}
