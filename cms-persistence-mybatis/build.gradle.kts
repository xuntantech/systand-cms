plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    api(project(":cms-api"))
    api("com.baomidou:mybatis-plus-core:3.5.17")
    implementation("tools.jackson.core:jackson-databind:3.1.4")
    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")

    testImplementation(platform("org.junit:junit-bom:5.12.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name.set("Systand CMS MyBatis persistence")
                description.set("CMS-owned MyBatis persistence infrastructure, independent of platform modules.")
            }
        }
    }
}
