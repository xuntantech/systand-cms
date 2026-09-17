plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    api(project(":cms-core"))
    api(project(":cms-persistence-mybatis"))
    api(project(":cms-webmvc"))
    implementation("org.springframework.boot:spring-boot-autoconfigure:4.1.0")
    implementation("com.baomidou:mybatis-plus-spring-boot4-starter:3.5.17")

    testImplementation(platform("org.junit:junit-bom:5.12.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.springframework:spring-context:7.0.5")
    testImplementation("org.springframework.boot:spring-boot-starter-test:4.1.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name.set("Systand CMS Spring Boot Starter")
                description.set("Spring Boot auto-configuration for the Systand CMS core.")
            }
        }
    }
}
