plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    api(project(":cms-api"))
    implementation(project(":cms-core"))
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.validation)
    compileOnly(libs.lombok)
    compileOnly(libs.mapstruct)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.mapstruct.processor)
    annotationProcessor(libs.lombok.mapstruct.binding)
    testImplementation(libs.spring.boot.starter.test)
    testRuntimeOnly(libs.junit.platform.launcher)
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
