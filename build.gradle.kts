plugins {
    id("java")
    id("application")
}

group = "main"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("lox.Lox") // Change this to match your actual main class
}

tasks.register<JavaExec>("runSilent") {
    group = "application"
    description = "Runs the application without Gradle logs"
    mainClass.set(application.mainClass)
    classpath = sourceSets["main"].runtimeClasspath
    standardOutput = System.out
    errorOutput = System.err
    args = listOf("src/main/resources/script.lox") // Pass the file as an argument
}

tasks.register<JavaExec>("runGeneratorTool") {
    group = "application"
    description = "Runs the generator tool"
    mainClass.set("tool.GenerateAst")
    classpath = sourceSets["main"].runtimeClasspath
    standardOutput = System.out
    errorOutput = System.err
    args = listOf("src/main/java/lox")
}

tasks.register<JavaExec>("runSilentPrompt") {
    group = "application"
    description = "Runs the application without Gradle logs and allows interactive input"
    mainClass.set(application.mainClass)
    classpath = sourceSets["main"].runtimeClasspath
    standardOutput = System.out
    errorOutput = System.err
    standardInput = System.`in` // Enables interactive input
    isIgnoreExitValue = true // Prevents Gradle from failing if the program exits with a non-zero code
}