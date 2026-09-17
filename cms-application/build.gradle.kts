plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    api(project(":cms-core"))
    api(project(":cms-persistence-mybatis"))
    implementation("org.springframework.boot:spring-boot-starter:4.1.0")
    implementation("org.springframework.boot:spring-boot-starter-validation:4.1.0")
    implementation("org.springframework:spring-jdbc:7.0.5")

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
                name.set("Systand CMS Application")
                description.set("Reusable CMS page, section, and site business services.")
            }
        }
    }
}
