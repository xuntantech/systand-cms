plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    api(project(":cms-core"))
    implementation(libs.spring.boot.starter)
    implementation(libs.spring.jdbc)
    compileOnly(libs.mapstruct)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.mapstruct.processor)
    annotationProcessor(libs.lombok.mapstruct.binding)
    testImplementation(libs.mapstruct)
    testImplementation(libs.spring.boot.starter.test)
    testRuntimeOnly(libs.junit.platform.launcher)
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
