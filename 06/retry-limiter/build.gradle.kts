plugins {
    id("java")
}

group = "hyperfocal"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("com.linecorp.armeria:armeria-bom:1.34.1"))
    implementation("com.linecorp.armeria:armeria")
    implementation("ch.qos.logback:logback-classic:1.5.13")

    testImplementation(platform("org.junit:junit-bom:5.11.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("com.linecorp.armeria:armeria-junit5")
}

tasks.test {
    useJUnitPlatform()
}
