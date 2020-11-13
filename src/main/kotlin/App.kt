import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import react.*
import react.dom.h1

class App : RComponent<RProps, AppState>() {
    override fun AppState.init() {
        conferenceIds = listOf()
        jvbInfo = js("{}")

        val mainScope = MainScope()
        mainScope.launch {
            val jvbState = fetchData()
            setState {
                jvbInfo = jvbState
            }
        }
    }

    override fun RBuilder.render() {
        h1 {
            +"Hello, world!"
        }
    }

    private suspend fun fetchData(): dynamic {
        println("fetching data")
        return window.fetch("http://127.0.0.1:4443/debug") //, RequestInit(mode = RequestMode.NO_CORS)).await()
            .await()
            .json()
            .await()
            .asDynamic()
    }
}

external interface AppState : RState {
    var jvbInfo: dynamic
    var conferenceIds: List<String>
}

//data class JvbData(
//   val shutdownInProgress: Boolean,
//   val timestampMs: Long,
//   val health: String,
//)

//data class LoadManagement(
//    val state: String,
//    val stress: Double,
//    val reducer_enabled: Boolean,
//)

