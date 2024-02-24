package me.topilov.context

class JavaContext(
    private var javaPath: String = "java",
    private var mainClass: String? = null,
    private var jvmArgs: ArrayList<Any> = arrayListOf(),
    private var arguments: ArrayList<Any> = arrayListOf(),
    private var classpath: ArrayList<Any> = arrayListOf()
) {

    fun getExecutionCommand(): List<String> {
        val args = arrayListOf<String>()

        args.add(this.javaPath)

        if (mainClass == null) throw RuntimeException("Java: No main class specified")

        args.addAll(jvmArgs.map(Any::toString))

        if (classpath.isNotEmpty()) {
            args.add("-cp")
            args.add(java.lang.String.join(":", classpath.map(Any::toString)))
        }

        mainClass
            ?.also(args::add)

        args.addAll(arguments.map(Any::toString))

        return args
    }

    fun xmx(xmx: String) {
        jvmArgs.add("-Xmx$xmx")
    }

    fun xms(xms: String) {
        jvmArgs.add("-Xms$xms")
    }

    fun arguments(vararg arguments: Any) {
        this.arguments.addAll(arguments)
    }

    fun jvmArgs(vararg arguments: Any) {
        jvmArgs.addAll(arguments)
    }

    fun classpath(vararg arguments: Any) {
        classpath.addAll(arguments)
    }
}
