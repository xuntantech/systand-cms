plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    api(project(":cms-api"))

    testImplementation(platform("org.junit:junit-bom:5.12.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
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
