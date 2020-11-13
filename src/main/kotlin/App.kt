import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import react.*
import react.dom.h1

class App : RComponent<RProps, AppState>() {
    override fun AppState.init() {
        conferenceIds = listOf()
        jvbInfo = js("{}")

        val mainScope = MainScope()
        mainScope.launch {
            for (i in 0..10) {
                val jvbState = fetchData()
                setState {
                    jvbInfo = jvbState
                }
                delay(1000)
            }
        }
    }

    override fun RBuilder.render() {
        h1 {
            +"JVB dashboard"
        }
        child(Jvb::class) {
            attrs {
                url = "http://127.0.0.1:4443/debug"
            }
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

