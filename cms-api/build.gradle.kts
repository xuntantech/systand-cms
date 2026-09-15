plugins {
    `java-library`
    `maven-publish`
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name.set("Systand CMS API")
                description.set("Stable host integration contracts and shared CMS value types.")
            }
        }
    }
}
