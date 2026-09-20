plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    api(project(":cms-api"))
    api(project(":cms-core"))
    implementation(project(":cms-application"))
    api(libs.mybatis.plus.core)
    implementation(libs.jackson.databind)
    implementation(libs.spring.context)
    compileOnly(libs.lombok)
    compileOnly(libs.mapstruct)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.mapstruct.processor)
    annotationProcessor(libs.lombok.mapstruct.binding)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mapstruct)
    testImplementation(libs.spring.boot.starter.test)
    testRuntimeOnly(libs.junit.platform.launcher)
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
