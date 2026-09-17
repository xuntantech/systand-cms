plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    api(project(":cms-application"))
    implementation("org.springframework.boot:spring-boot-starter-web:4.1.0")
    implementation("org.springframework.boot:spring-boot-starter-validation:4.1.0")
    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")
    testImplementation("org.springframework.boot:spring-boot-starter-test:4.1.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.12.1")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name.set("Systand CMS Web MVC")
                description.set("Configurable CMS management HTTP endpoints.")
            }
        }
    }
}
