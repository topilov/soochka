package me.topilov

import kotlinx.cli.*
import me.topilov.context.JavaContext
import me.topilov.context.PresetContext
import me.topilov.dsl.preset
import java.io.File
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

fun main(args: Array<String>) {

    val parser = ArgParser("soochka")

    val presetsDirPath by parser.option(
        ArgType.String,
        shortName = "c",
        description = "Path to soochka config scripts/directories"
    ).default("presets")

    val forestDirPath by parser.option(
        ArgType.String,
        shortName = "f",
        description = "Home directory for soochka"
    ).default(".")

    val realmDirPath by parser.option(
        ArgType.String,
        shortName = "r",
        description = "Directory to store realms data"
    ).default("realms")

    val dirPath by parser.option(
        ArgType.String,
        shortName = "d",
        description = "Add source root"
    ).multiple()

    val presetName by parser.option(
        ArgType.String,
        fullName = "preset",
        shortName = "p",
        description = "Specify preset name to use"
    ).required()

    val realmArg by parser.option(
        ArgType.String,
        fullName = "realm",
        description = "Specify realm address"
    ).required()

    parser.parse(args)

    val forestDir = File(forestDirPath)
        .also(File::mkdirs)

    val split = realmArg.split("-")

    if (split.size != 2) {
        println("Invalid realm address: $realmArg")
        return
    }

    val realmType = split[0]
    val realmId = split[1].toInt()

    val realmDir = when {
        realmDirPath.startsWith("/") -> File(realmDirPath)
        else -> File(forestDir, realmDirPath)
    }.also(File::mkdirs)

    val internalsDir = File(forestDir, ".soochka")
        .also(File::mkdirs)

    val presetsDir = File(presetsDirPath)
        .also(File::mkdirs)

    val presetFile = presetsDir
        .listFiles { file -> file.nameWithoutExtension == presetName }
        ?.firstOrNull()

    if (presetFile == null) {
        println("Unable to find preset: $presetName")
        return
    }

    val dir = dirPath.map(::File).onEach(File::mkdirs)

    preset.apply {
        this.realmId = realmId
        this.realmType = realmType
        this.assignedPort = 17700
    }

    PresetContext.apply {
        this.realmDir = realmDir
        this.contentRoots = dir
    }

    when (val result = readPresetConfiguration(presetFile)) {
        is ResultWithDiagnostics.Success -> {
            val returnValue = result.value.returnValue

            if (returnValue !is ResultValue.Value) {
                println("Unable to parse preset: $presetName")
                return
            }

            val presetContent = returnValue.value as PresetContext

            presetContent.execute()
        }

        is ResultWithDiagnostics.Failure -> {
            println("Error ${result.reports.joinToString(" ")}")
        }
    }
}

fun readPresetConfiguration(file: File): ResultWithDiagnostics<EvaluationResult> {
    val scriptingHost = BasicJvmScriptingHost()

    val compilationConfiguration = ScriptCompilationConfiguration {
        jvm {
            dependenciesFromCurrentContext(wholeClasspath = true)
        }

        defaultImports(preset::class, PresetContext::class, JavaContext::class)
    }

    val sourceCode = file.readText().toScriptSource()
    return scriptingHost.eval(sourceCode, compilationConfiguration, null)
}