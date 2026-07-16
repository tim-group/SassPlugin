plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    id("com.gradle.plugin-publish") version "2.1.1"
    id("com.adarshr.test-logger") version "4.0.0"
}

val buildNumber = providers.environmentVariable("ORIGINAL_BUILD_NUMBER")
    .orElse(providers.environmentVariable("BUILD_NUMBER"))
val githubUrl by extra("https://github.com/tim-group/gradle-webpack-plugin")

group = "com.timgroup"
if (buildNumber.isPresent) version = "1.1.${buildNumber.get()}"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
    withSourcesJar()
}

dependencies {
    implementation(gradleApi())
    runtimeOnly("org.jruby:jruby-complete:10.1.0.0")
    runtimeOnly("me.n4u.sass:sass-gems:3.1.16")
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(kotlin("test-junit5"))
    testImplementation("com.natpryce:hamkrest:1.7.0.0")
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }

    withType<Test>().configureEach {
        useJUnitPlatform()
    }
}

gradlePlugin {
    plugins {
        register("sass") {
            id = "com.youdevise.sass"
            implementationClass = "com.youdevise.gradle.plugins.SassPlugin"
        }
    }
}

pluginBundle {
    website = githubUrl
    vcsUrl = githubUrl
    tags = setOf("nodejs", "sass")
}

val nexusRepoUrl = providers.gradleProperty("repoUrl")
val nexusRepoUsername = providers.gradleProperty("repoUsername")
val nexusRepoPassword = providers.gradleProperty("repoPassword")
val codeartifactUrl = providers.environmentVariable("CODEARTIFACT_URL")
    .orElse(providers.gradleProperty("codeartifact.url"))
    .orElse("https://timgroup-148217964156.d.codeartifact.eu-west-1.amazonaws.com/maven/jars/")
val codeartifactToken = providers.environmentVariable("CODEARTIFACT_TOKEN")
    .orElse(providers.gradleProperty("codeartifact.token"))

publishing {
    repositories {
        if (nexusRepoUrl.isPresent && nexusRepoUsername.isPresent && nexusRepoPassword.isPresent) {
            maven("${nexusRepoUrl.get()}/repositories/yd-release-candidates") {
                name = "nexus"
                credentials {
                    username = nexusRepoUsername.get()
                    password = nexusRepoPassword.get()
                }
                isAllowInsecureProtocol = true
            }
        }
        if (codeartifactUrl.isPresent && codeartifactToken.isPresent) {
            maven(url = codeartifactUrl.get()) {
                name = "codeartifact"
                credentials {
                    username = "aws"
                    password = codeartifactToken.get()
                }
            }
        }
    }
}
