package org.shipkit.changelog

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

/**
 * Only smoke test, forever! Don't add more tests here, instead cover the complexity in lower level unit tests.
 */
class ChangelogPluginIntegTest extends Specification {

    @Rule TemporaryFolder tmp = new TemporaryFolder()

    def setup() {
        file("settings.gradle")
        file("build.gradle") << """
            plugins {  id('org.shipkit.changelog')}
        """
    }

    def "applies plugin cleanly"() {
        given:
        def result = run("tasks")

        expect:
        result.output.contains("Applying plugin...")
    }

    File file(String path) {
        def f = new File(rootDir, path)
        if (!f.exists()) {
            f.parentFile.mkdirs()
            f.createNewFile()
            assert f.exists()
        }
        return f
    }

    File getRootDir() {
        return tmp.root
    }

    def run(String ...args) {
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments(args)
        runner.withProjectDir(rootDir)
        return runner.build()
    }
}
