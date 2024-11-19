package com.github.nousxiong.cloverservergenerator.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@Service
@State(
    name = "CloverServerGeneratorSettings",
    storages = [Storage("CloverServerGeneratorSettings.xml")]
)
class PluginSettings : PersistentStateComponent<PluginSettings.State> {

    data class State(
        var root: String = "src/main",
        var poPkgRoot: String = "games.lightboat.clover.entity",
        var daoPkgRoot: String = "games.lightboat.clover.dao",
        var servicePkgRoot: String = "games.lightboat.clover.service",
        var mapperRoot: String = "mapper"
    )

    private var state = State()

    override fun getState(): State {
        return state
    }

    override fun loadState(state: State) {
        this.state = state
    }

    companion object {
        fun getInstance(): PluginSettings = ApplicationManager.getApplication().getService(PluginSettings::class.java)
    }
}
