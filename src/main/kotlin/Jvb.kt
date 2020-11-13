import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import react.*
import react.dom.p

class Jvb : RComponent<JvbProps, JvbState>() {
    override fun JvbState.init() {
        val mainScope = MainScope()
        mainScope.launch {
            for (i in 0..10) {
                val jvbData = fetchData()
                setState {
                    state = jvbData
                }
                delay(1000)
            }
        }
    }
    override fun RBuilder.render() {
        if (!state.state) {
            +"No data received yet"
            return
        }
        console.log("state: ", state.state)
        p {
            +"time: ${state.state.time}"
        }
        p {
            +"conferences: ${keys(state.state.conferences)}"
        }
    }

    private suspend fun fetchData(): dynamic {
        return window.fetch(props.url)
            .await()
            .json()
            .await()
            .asDynamic()
    }
}

external interface JvbState : RState {
    var state: dynamic
}

external interface JvbProps : RProps {
    var url: String
}

