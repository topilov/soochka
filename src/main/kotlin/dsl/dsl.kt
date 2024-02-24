package me.topilov.dsl

import me.topilov.context.PresetContext

object preset {

    lateinit var realmType: String
    var realmId: Int = 1
    var assignedPort = 17770

    operator fun invoke(init: PresetContext.() -> Unit): PresetContext {
        return PresetContext().also(init)
    }
}