plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    api(project(":cms-api"))

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name.set("Systand CMS Core")
                description.set("Host-independent CMS page lifecycle and section definition rules.")
            }
        }
    }
}
