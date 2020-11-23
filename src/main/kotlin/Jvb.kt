import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import react.*
import react.dom.div
import react.dom.p
import kotlin.js.Date

class Jvb : RComponent<JvbProps, JvbState>() {
    private suspend fun CoroutineScope.fetchDataLoop() {
        while (isActive) {
            try {
                val jvbData = fetchData()
                setState {
                    state = jvbData
                    error = null
                }
            } catch (t: Throwable) {
                // TODO: add a maximum number of retries?
                setState {
                    error = "Error retrieving JVB data: ${t.message}"
                }
            }
            delay(props.updateIntervalMs ?: 1000)
        }
    }

    override fun componentDidMount() {
        GlobalScope.launch { fetchDataLoop() }
    }

    override fun RBuilder.render() {
        if (state.error != null) {
            +state.error!!
            return
        }
        if (!state.state) {
            +"No data received yet"
            return
        }
        val date = Date(state.state.time.unsafeCast<Number>())
        p {
            +date.toUTCString()
        }
        div {
            child(JvbStressStatusBadge::class) {
                attrs {
                    value = state.state["load-management"].stress.unsafeCast<Double>()
                }
            }
        }
        conferenceIds.forEach { confId ->
            child(Conference::class) {
                key = confId
                attrs {
                    baseUrl = props.url
                    id = confId
                }
            }
        }
    }

    private val conferenceIds: Array<String>
        get() {
            if (!state.state) {
                return arrayOf()
            }
            return keys(state.state.conferences)
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
    var error: String?
}

external interface JvbProps : RProps {
    var url: String
    var updateIntervalMs: Long?
}

