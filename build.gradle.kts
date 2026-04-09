plugins {
    id("java")
    id("com.gradleup.shadow") version "9.4.1"
}

group = "dev.felnull"
version = "1.0-SNAPSHOT"


repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.lavalink.dev")
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("net.dv8tion:JDA:6.4.1")
    implementation("io.github.cdimascio:dotenv-java:3.2.0")
    implementation("tools.jackson.core:jackson-databind:3.1.1")
    implementation("dev.felnull:felnull-java-library:1.75")
    implementation("dev.arbjerg:lavaplayer:2.2.6")
    runtimeOnly("club.minnced:jdave-native-win-x86-64:0.1.8")
    runtimeOnly("club.minnced:jdave-native-linux-x86-64:0.1.8")
    implementation("club.minnced:jdave-api:0.1.8")
    implementation("org.jetbrains:annotations:26.0.2-1")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "bot.HotarudaBot"
    }
}