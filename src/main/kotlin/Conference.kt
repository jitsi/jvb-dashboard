import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import react.*
import react.dom.p

class Conference : RComponent<ConferenceProps, ConferenceState>() {
    override fun ConferenceState.init() {
        val mainScope = MainScope()
        mainScope.launch {
            for (i in 0..10) {
                val confData = fetchData()
                setState {
                    state = confData.asDynamic()
                }
            }
        }
    }

    override fun RBuilder.render() {
        p {
            if (!state.state) {
                +"No data received yet"
                return
            }
            +"Got data: ${state.state}"
        }
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
