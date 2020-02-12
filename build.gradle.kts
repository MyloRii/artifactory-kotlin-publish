import groovy.lang.GroovyObject
import org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig
import org.jfrog.gradle.plugin.artifactory.dsl.ResolverConfig

plugins {
    java
    kotlin("jvm") version "1.3.41"
    `maven-publish`
    id("com.jfrog.artifactory") version "4.13.0"
}

group = "com.yohji"
version = "2.0"

repositories {
    jcenter()
}

dependencies {
    testImplementation("junit:junit:4.7")
}

val sourcesJar by tasks.registering(Jar::class) {
    classifier = "sources"
    from(sourceSets.main.get().allSource)
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            artifact(sourcesJar.get())
        }
    }
}

artifactory {
    setContextUrl("https://yohji.jfrog.io/yohji")
    publish(delegateClosureOf<PublisherConfig> {
        repository(delegateClosureOf<GroovyObject> {
            val targetRepoKey = "libs-release-local"
            setProperty("repoKey", targetRepoKey)
            setProperty("username", project.findProperty("artifactory_user") ?: "nouser")
            setProperty("password", project.findProperty("artifactory_password") ?: "nopass")
            setProperty("maven", true)
        })
        defaults(delegateClosureOf<GroovyObject> {
            invokeMethod("publications", "mavenJava")
        })
    })
    resolve(delegateClosureOf<ResolverConfig> {
        setProperty("repoKey", "repo")
    })
}

fun buildTagFor(version: String): String =
    when (version.substringAfterLast('-')) {
        "SNAPSHOT" -> "snapshot"
        in Regex("""M\d+[a-z]*$""") -> "milestone"
        else -> "release"
    }

operator fun Regex.contains(s: String) = matches(s)