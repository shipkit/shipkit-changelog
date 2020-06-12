package org.shipkit.changelog;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

/**
 * The plugin, ideally with zero business logic, but only the Gradle integration code
 */
public class ChangelogPlugin implements Plugin<Project> {

    private static final Logger LOG = Logging.getLogger(ChangelogPlugin.class);

    public void apply(Project project) {
        LOG.lifecycle("Applying plugin...");
    }
}
