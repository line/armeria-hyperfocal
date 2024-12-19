plugins {
    id("java")
}

group = "hyperfocal"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("com.linecorp.armeria:armeria-bom:1.31.3"))
    implementation("com.linecorp.armeria:armeria")
    implementation("ch.qos.logback:logback-classic:1.4.14")

    testImplementation(platform("org.junit:junit-bom:5.11.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("com.linecorp.armeria:armeria-junit5")
}

tasks.test {
    useJUnitPlatform()
}
