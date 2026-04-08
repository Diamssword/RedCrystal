import java.util.Properties

/**
 * NOTE: This is entirely optional and basics can be done in `settings.gradle.kts`
 */
val props = Properties()
file("gradle.properties").inputStream().use { props.load(it) }
// 2. Access your property
val modVersion = props.getProperty("modVersion")
repositories {
    maven {
        name = "hM"
        url = uri("https://maven.hytale-mods.dev/releases")
    }

}

dependencies {
    implementation("com.buuz135:MultipleHUD:1.0.7")
}
tasks.jar {
    archiveBaseName.set("RedCrystal")
    archiveVersion.set(modVersion)
    //archiveClassifier.set("") // optional (removes "-all" or similar suffixes)
}