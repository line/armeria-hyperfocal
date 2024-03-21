plugins {
    id("java")
    id("com.palantir.graal") version("0.12.0")
}

group = "hyperfocal"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
        vendor.set(JvmVendorSpec.GRAAL_VM)
    }
}

dependencies {
    implementation(platform("com.linecorp.armeria:armeria-bom:1.27.2"))
    implementation("com.linecorp.armeria:armeria")
    implementation("ch.qos.logback:logback-classic:1.4.14")

    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("com.linecorp.armeria:armeria-junit5")
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("-agentlib:native-image-agent=config-output-dir=$projectDir/src/main/resources/META-INF/native-image")
}

val mainClassName = "native_image.Main"
graal {
    graalVersion("22.3.3")
    javaVersion("17")
    mainClass(mainClassName)
    outputName("armeria-native-image")
    option("--no-fallback")
    option("--trace-object-instantiation=ch.qos.logback.classic.Logger")
    option("--initialize-at-build-time=org.slf4j.LoggerFactory,ch.qos.logback")
}
