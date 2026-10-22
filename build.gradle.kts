plugins {
    java
}

sourceSets.main {
    java.setSrcDirs(listOf("src"))
}

// Версия Mindustry для разработки и тестов
val mindustryVersion = "v160"
val isWindows = System.getProperty("os.name").lowercase().contains("windows")
val sdkRoot: String? = System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT")
val projectName = project.name

val nameAndroidBuild = "-android"
val nameDesktopBuild = "-desktop"
val nameAllBuild = ""

repositories {
    mavenCentral()

    // Репозиторий Анукена
    ivy {
        url = uri("https://github.com")
        patternLayout {
            val pattern = when(mindustryVersion) {
                "latest" -> "/[organisation]/[module]/releases/[revision]/download/dependencies.jar"
                "be" -> "/[organisation]/[module]/releases/download/master/[revision].jar"
                else -> "/[organisation]/[module]/releases/download/[revision]/[classifier].jar"
            }
            val path = pattern.substring(0, pattern.lastIndexOf('/'))

            artifact("$path/[classifier].jar")
            artifact(pattern.replaceFirst(Regex("""\.jar$"""), "(-[classifier]).jar"))
        }
        metadataSources{ artifact() }

        content{
            if (mindustryVersion == "be") {
                includeVersion("Anuken", "MindustryBuilds", "latest")
            } else {
                includeVersion("Anuken", "Mindustry", mindustryVersion)
            }
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    compileOnly("Anuken:Mindustry:$mindustryVersion:dependencies")
}

val testGameConfig by configurations.creating

dependencies {
    testGameConfig("Anuken:Mindustry:$mindustryVersion:Mindustry")
}

tasks.jar {
    archiveFileName.set("${projectName}$nameDesktopBuild.jar")

    from(configurations.runtimeClasspath.map { files ->
        files.map{ if(it.isDirectory) it else zipTree(it) }
    })

    from("assets/") {
        include("**")
    }
}

val jarAndroid = tasks.register("jarAndroid") {
    dependsOn(tasks.jar)

    doLast {
        val sdk = sdkRoot?.let{ File(it) }
        if(sdk == null || !sdk.exists()) {
            throw GradleException("No valid Android SDK found. Ensure that ANDROID_HOME is set to your Android SDK directory.")
        }

        val platformRoot = File(sdk, "platforms").listFiles()
            ?.sortedDescending()
            ?.firstOrNull { File(it, "android.jar").exists() }
            ?: throw GradleException("No android.jar found. Ensure that you have an Android platform installed.")

        val classpath = (configurations.compileClasspath.get().files +
                configurations.runtimeClasspath.get().files +
                File(platformRoot, "android.jar"))

        val d8 = if(isWindows) "d8.bat" else "d8"

        val command = buildList {
            add(d8)
            classpath.forEach{ addAll(listOf("--classpath", it.path)) }
            addAll(listOf("--min-api", "21"))
            addAll(listOf("--output", "${projectName}$nameAndroidBuild.jar"))
            add("${projectName}$nameDesktopBuild.jar")
        }

        val exitCode = ProcessBuilder(command)
            .directory(layout.buildDirectory.dir("libs").get().asFile)
            .inheritIO()
            .start()
            .waitFor()

        if(exitCode != 0) throw GradleException("d8 failed with exit code $exitCode")
    }
}

tasks.register<Jar>("deploy"){
    dependsOn(jarAndroid, tasks.jar)
    archiveFileName.set("$projectName.jar")

    val libs = layout.buildDirectory.dir("libs")
    from(provider{
        listOf(
            zipTree(libs.get().file("${projectName}$nameDesktopBuild.jar")),
            zipTree(libs.get().file("${projectName}$nameAndroidBuild.jar"))
        )
    })

    doLast{
        libs.get().file("${projectName}$nameAndroidBuild.jar").asFile.delete()
    }
}
// Копируем полноценный скачанный клиент игры в нашу папку песочницы
val setupTestClient = tasks.register<Copy>("setupTestClient") {
    from(testGameConfig)
    into(file("test-env"))
    rename { "Mindustry.jar" }

    doLast {
        println("[$projectName] Полноценный клиент игры успешно загружен в тестовое окружение!")
    }
}

val deployToTest = tasks.register<Copy>("deployToTest") {
    dependsOn(tasks.jar, setupTestClient)
    from(layout.buildDirectory.dir("libs").get().file("${projectName}$nameDesktopBuild.jar"))

    // Подстраиваемся под хитрый путь Миндустрии на Linux
    into(file("test-env/.local/share/Mindustry/mods"))
}

tasks.register<JavaExec>("runClient") {
    dependsOn(deployToTest)

    mainClass.set("-jar")
    args(file("test-env/Mindustry.jar").absolutePath, "--dev")

    systemProperty("user.home", file("test-env").absolutePath)
    jvmArgs("-XX:+AlwaysPreTouch")
    workingDir = file("test-env")
}
