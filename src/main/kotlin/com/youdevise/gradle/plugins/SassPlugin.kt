package com.youdevise.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

open class SassPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("compileSass", CompileSassTask::class)
    }
}
