package theme

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction

/** Regenerates all widget theme resources (res XML, WidgetTheme.kt, WidgetThemeIds.kt). */
abstract class GenerateWidgetThemeResourcesTask : DefaultTask() {

    @get:InputDirectory
    abstract val resDir: DirectoryProperty

    @get:InputDirectory
    abstract val modelDir: DirectoryProperty

    @get:InputDirectory
    abstract val utilDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val output = WidgetThemeResourcesGenerator.Output(
            resDir = resDir.get().asFile,
            modelDir = modelDir.get().asFile,
            utilDir = utilDir.get().asFile,
        )
        val files = WidgetThemeResourcesGenerator.generate(output)
        files.forEach { file ->
            file.path.parentFile?.mkdirs()
            file.path.writeText(file.content)
        }
        logger.lifecycle("WidgetThemeResources: generated ${files.size} files")
    }
}

/** Fails the build if the committed widget theme resources drift from the code generator output. */
abstract class VerifyWidgetThemeResourcesTask : DefaultTask() {

    @get:InputDirectory
    abstract val resDir: DirectoryProperty

    @get:InputDirectory
    abstract val modelDir: DirectoryProperty

    @get:InputDirectory
    abstract val utilDir: DirectoryProperty

    @TaskAction
    fun verify() {
        val output = WidgetThemeResourcesGenerator.Output(
            resDir = resDir.get().asFile,
            modelDir = modelDir.get().asFile,
            utilDir = utilDir.get().asFile,
        )
        val expected = WidgetThemeResourcesGenerator.generate(output)
        val stale = expected.filter { file -> !file.path.exists() || file.path.readText() != file.content }
        if (stale.isNotEmpty()) {
            val details = stale.joinToString("\n") { "  - ${it.path}" }
            throw GradleException(
                "Generated widget theme resources are out of date:\n$details\n\n" +
                    "Run ./gradlew :app:generateWidgetThemeResources and commit the result."
            )
        }
        logger.lifecycle("WidgetThemeResources: verified ${expected.size} files are up to date")
    }
}