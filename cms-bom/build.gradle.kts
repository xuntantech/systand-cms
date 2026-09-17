plugins {
    `java-platform`
    `maven-publish`
}

javaPlatform {
    allowDependencies()
}

dependencies {
    constraints {
        api(project(":cms-api"))
        api(project(":cms-core"))
        api(project(":cms-persistence-mybatis"))
        api(project(":cms-application"))
        api(project(":cms-webmvc"))
        api(project(":cms-spring-boot-starter"))
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["javaPlatform"])
            pom {
                name.set("Systand CMS BOM")
                description.set("Dependency alignment for Systand CMS artifacts.")
            }
        }
    }
}
