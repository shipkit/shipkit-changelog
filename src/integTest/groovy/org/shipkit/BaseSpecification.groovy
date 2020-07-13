package org.shipkit

import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification

class BaseSpecification extends Specification {

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

    GradleRunner runner(String... args) {
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments(args)
        runner.withProjectDir(rootDir)
        runner
    }
}
