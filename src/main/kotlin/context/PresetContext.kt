package me.topilov.context

import me.topilov.dsl.PresetDslMarker
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.system.exitProcess

typealias Environment = MutableMap<String, Any>

@PresetDslMarker
class PresetContext(
    private var env: Environment = mutableMapOf(),
    private var java: JavaContext = JavaContext(),
) {
    companion object {
        lateinit var contentRoots: List<File>
        lateinit var realmDir: File
    }

    fun execute() {
        val command = java.getExecutionCommand()

        try {
            val builder = ProcessBuilder(command).directory(realmDir)
            env.forEach { (k, v) -> builder.environment()[k] = v.toString() }

            builder.inheritIO()
            val start = builder.start()
            start.waitFor()
        } catch (throwable: Throwable) {
            println("An error occurred while running command: ${command.joinToString(" ")}")
            throwable.printStackTrace()
            exitProcess(0)
        }
    }

    fun java(block: JavaContext.() -> Unit) {
        java.apply(block)
    }

    infix fun String.env(value: Any) {
        env[this] = value
    }

    fun resource(name: String): File {
        if (contentRoots.isEmpty()) throw IllegalStateException("No content roots specified!")

        contentRoots.forEach { contentRoot ->
            val file = File(contentRoot, name)
            if (file.exists()) return file.absoluteFile
        }

        throw RuntimeException("Unable to find resource '$name'!")
    }

    fun resourceCopy(source: String) {
        source resourceCopy ""
    }

    infix fun String.resourceCopy(destination: String) {
        val resource = resource(this)
        val destinationFile = File(realmDir, destination)
        copy(resource, destinationFile)
    }

    fun copy(sourceFile: File, destinationFile: File) {
        var destination = destinationFile

        if (sourceFile.isDirectory) {
            sourceFile.listFiles()?.forEach { file ->
                copy(file, File(destinationFile, file.name))
            }
            return
        }

        if (destinationFile.isDirectory) {
            destination = File(destination, sourceFile.name)
        }
        destination.parentFile.mkdirs()
        Files.copy(sourceFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING)
    }

    fun delete(obj: Any) {
        if (obj is File) {
            if (obj.isDirectory) {
                delete(*obj.listFiles() as Array<out Any>)
            }
            obj.delete()
            return
        }

        var path = "/${obj.toString().replace("\\", "/")}"
        if ("/../" in path) throw RuntimeException("Refusing to delete from suspicious path: $obj")
        while (path.startsWith("/")) path = path.substring(1)
        delete(File(realmDir, path))
    }

    fun delete(vararg objects: Any) {
        objects.forEach(::delete)
    }
}