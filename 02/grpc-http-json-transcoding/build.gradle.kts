import com.google.protobuf.gradle.id

plugins {
    id("java")
    id("com.google.protobuf") version("0.9.4")
}

group = "hyperfocal"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType(JavaCompile::class) {
    options.release = 21
}

dependencies {

    compileOnly("javax.annotation:javax.annotation-api:1.3.2")

    implementation(platform("com.linecorp.armeria:armeria-bom:1.30.0"))
    implementation("com.linecorp.armeria:armeria")
    implementation("com.linecorp.armeria:armeria-grpc")

    implementation("com.google.api.grpc:proto-google-common-protos:2.43.0")

    implementation("ch.qos.logback:logback-classic:1.5.7")

    testImplementation(platform("org.junit:junit-bom:5.11.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("com.linecorp.armeria:armeria-junit5")
}


protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.1"
    }

    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.65.1"
        }
    }

    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                id("grpc") { }
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
