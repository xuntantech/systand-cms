plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    api(project(":cms-core"))
    api(project(":cms-webmvc"))
    implementation(project(":cms-application"))
    implementation(project(":cms-persistence-mybatis"))
    implementation(libs.spring.boot.autoconfigure)
    implementation(libs.mybatis.plus.spring.boot4.starter)
    compileOnly(libs.mapstruct)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.spring.context)
    testImplementation(libs.spring.boot.starter.test)
    testRuntimeOnly(libs.junit.platform.launcher)
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
