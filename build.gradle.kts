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
        url = uri("https://www.cursemaven.com")
    }
}

dependencies {
    compileOnly("curse.maven:hyui-1431415:7820303")
    implementation("curse.maven:hyui-1431415:7820303")
}
tasks.jar {
    archiveBaseName.set("RedCrystal")
    archiveVersion.set(modVersion)
    //archiveClassifier.set("") // optional (removes "-all" or similar suffixes)
}