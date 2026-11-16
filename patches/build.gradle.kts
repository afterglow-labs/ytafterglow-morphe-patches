group = "app.afterglow"

patches {
    about {
        name = "Afterglow"
        description = "Afterglow for YouTube on Android. The iOS feature port is in progress."
        source = "git@github.com:afterglow-labs/ytafterglow-morphe-patches.git"
        author = "afterglow-labs"
        contact = "https://github.com/afterglow-labs/ytafterglow-morphe-patches/issues"
        website = "https://github.com/afterglow-labs/ytafterglow-morphe-patches"
        license = "GPLv3"
    }
}

// Separate configuration so gson is available at runtime for the
// generatePatchesList task but never bundled into the APK.
val patchListGeneratorClasspath = configurations.create("patchListGeneratorClasspath")

dependencies {
    compileOnly(libs.gson)
    patchListGeneratorClasspath(libs.gson)
}

tasks {
    processResources {
        from(rootProject.file("NOTICE"))
        from(rootProject.file("LICENSE"))
        from(rootProject.file("NOTICE")) { into("afterglow/licenses") }
        from(rootProject.file("LICENSE")) { into("afterglow/licenses") }
    }

    register<JavaExec>("generatePatchesList") {
        description = "Build patch with patch list"

        dependsOn(build)

        classpath = sourceSets["main"].runtimeClasspath + patchListGeneratorClasspath
        mainClass.set("util.PatchListGeneratorKt")
    }

    publish {
        dependsOn("generatePatchesList")
    }
}
