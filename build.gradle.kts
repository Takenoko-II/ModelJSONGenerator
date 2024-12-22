plugins {
    id("java")
    id("application")
}

group = "com.gmail.takenokoii78"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

application {
    mainClass.set("com.gmail.takenokoii78.Main")
}

dependencies {
    implementation("org.jetbrains", "annotations-java5", "24.1.0")
}

tasks {
    withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        options.compilerArgs.add("-Xlint:none");
    }

    jar {
        manifest {
            attributes(
                "Main-Class" to "com.gmail.takenokoii78.Main"
            )

            attributes(
                "Class-Path" to "."
            )
        }
    }
}
