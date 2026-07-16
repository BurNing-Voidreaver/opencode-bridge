package com.opencode.bridge.auth

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import java.security.SecureRandom
import java.util.Base64

@State(
    name = "com.opencode.bridge.auth.TokenManager",
    storages = [Storage("opencode-bridge.xml")]
)
class TokenManager : PersistentStateComponent<TokenManager.State> {
    data class State(
        var token: String = generateToken(),
        var port: Int = 0
    )

    companion object {
        fun getInstance(): TokenManager =
            ApplicationManager.getApplication().getService(TokenManager::class.java)

        private fun generateToken(): String {
            val bytes = ByteArray(32)
            SecureRandom().nextBytes(bytes)
            return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        }
    }

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    fun getToken(): String = myState.token

    fun getPort(): Int = myState.port

    fun setPort(port: Int) {
        myState.port = port
    }
}
