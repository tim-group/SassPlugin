plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`
}

group = "com.timgroup"
val buildNumber: String? by extra { System.getenv("ORIGINAL_BUILD_NUMBER") ?: System.getenv("BUILD_NUMBER") }
if (buildNumber != null) version = "1.1.${buildNumber}"

repositories {
    jcenter()
}

gradlePlugin {
    plugins {
        register("sass") {
            id = "com.youdevise.sass"
            implementationClass = "com.youdevise.gradle.plugins.SassPlugin"
        }
    }
}

publishing {
    val repoUrl: String by project
    val repoUsername: String by project
    val repoPassword: String by project
    repositories {
        maven(url = "${repoUrl}/repositories/yd-release-candidates") {
            credentials {
                username = repoUsername
                password = repoPassword
            }
        }
    }

}

afterEvaluate {
    publishing {
        publications.named("pluginMaven", MavenPublication::class).configure {
            artifact(tasks["sourcesJar"])
        }
    }
}

tasks.register("sourcesJar", Jar::class) {
    dependsOn("classes")
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

dependencies {
    implementation(gradleApi())
    runtimeOnly("org.jruby:jruby-complete:1.7.27")
    runtimeOnly("me.n4u.sass:sass-gems:3.1.16")
    testImplementation("junit:junit:4.12")
    testImplementation(kotlin("test-junit"))
    testImplementation("com.natpryce:hamkrest:1.7.0.0")
}

tasks.withType(JavaCompile::class).configureEach {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
    options.encoding = "UTF-8"
    options.isIncremental = true
    options.isDeprecation = true
    options.compilerArgs = listOf("-parameters")
}
