import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import react.*
import react.dom.p

class Conference : RComponent<ConferenceProps, ConferenceState>() {
    override fun ConferenceState.init() {
        val mainScope = MainScope()
        mainScope.launch {
            for (i in 0..10) {
                val jvbData = fetchData()
                setState {
                    state = jvbData.conferences[props.id]
                }
                delay(1000)
            }
        }
    }

    override fun RBuilder.render() {
        +"Conference id ${props.id}"
        p {
            if (!state.state) {
                +"No data received yet"
                return
            }
        }
        epIds.forEach { epId ->
            child(Endpoint::class) {
                attrs {
                    id = epId
                    data = state.state.endpoints[epId]
                }
            }
        }
    }

    private val epIds: Array<String>
        get() {
            if (!state.state) {
                return arrayOf()
            }
            return keys(state.state.endpoints)
        }

    private suspend fun fetchData(): dynamic {
        return window.fetch("${props.baseUrl}/${props.id}")
            .await()
            .json()
            .await()
    }
}

external interface ConferenceState : RState {
    var state: dynamic
}


external interface ConferenceProps : RProps {
    var baseUrl: String
    var id: String
}
