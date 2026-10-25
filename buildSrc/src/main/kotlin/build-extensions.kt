import org.gradle.api.Project

val Project.mod: ModData get() = ModData(this)
fun Project.prop(key: String): String? = findProperty(key)?.toString()

// Stonecutter is not set up yet, so every property lives in the root gradle.properties.
// Once it is, this should resolve to the version node of the `common` project instead.
val Project.commonProject: Project get() = rootProject
val Project.commonMod get() = commonProject.mod

@JvmInline
value class ModData(private val project: Project) {
    val id: String get() = modProp("id")
    val name: String get() = modProp("name")
    val version: String get() = modProp("version")
    val group: String get() = modProp("group")
    val author: String get() = modProp("author")
    val description: String get() = modProp("description")
    val license: String get() = modProp("license")
    val credits: String get() = modPropOrNull("credits") ?: ""

    fun propOrNull(key: String) = project.prop(key)
    fun prop(key: String) = requireNotNull(propOrNull(key)) { "Missing '$key'" }
    fun modPropOrNull(key: String) = project.prop("mod.$key")
    fun modProp(key: String) = requireNotNull(modPropOrNull(key)) { "Missing 'mod.$key'" }
    fun depOrNull(key: String): String? = project.prop("deps.$key")?.takeIf { it.isNotEmpty() }
    fun dep(key: String) = requireNotNull(depOrNull(key)) { "Missing 'deps.$key'" }
    fun modrinth(name: String, version: String) = "maven.modrinth:$name:$version"
}
