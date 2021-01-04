import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.await
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.w3c.fetch.CORS
import org.w3c.fetch.RequestInit
import org.w3c.fetch.RequestMode
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.div
import react.dom.hr
import react.dom.p
import react.setState
import kotlin.js.Date

class Jvb : RComponent<JvbProps, JvbState>() {
    private var job: Job? = null

    override fun componentDidMount() {
        job = GlobalScope.launch { fetchDataLoop() }
    }

    override fun componentWillUnmount() {
        job?.cancel("Unmounting")
    }

    override fun RBuilder.render() {
        if (state.error != null) {
            +state.error!!
            return
        }
        if (!hasData()) {
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
        div {
            child(FeatureToggle::class) {
                attrs {
                    featureName = "Pool Stats"
                    url = "${props.url}/features/jvb/pool-stats"
                }
            }
            child(FeatureToggle::class) {
                attrs {
                    featureName = "Queue Stats"
                    url = "${props.url}/features/jvb/queue-stats"
                }
            }
            child(FeatureToggle::class) {
                attrs {
                    featureName = "Payload verification"
                    url = "${props.url}/features/jvb/payload-verification"
                }
            }
            child(FeatureToggle::class) {
                attrs {
                    featureName = "Node stats"
                    url = "${props.url}/features/jvb/node-stats"
                }
            }
            child(FeatureToggle::class) {
                attrs {
                    featureName = "Node tracing"
                    url = "${props.url}/features/jvb/node-tracing"
                }
            }
        }
        hr {}
        conferenceIds.forEach { confId ->
            child(Conference::class) {
                key = confId
                attrs {
                    baseRestApiUrl = props.url
                    id = confId
                }
            }
        }
    }

    private val conferenceIds: Array<String>
        get() {
            if (!hasData()) {
                return arrayOf()
            }
            return keys(state.state.conferences)
        }

    private fun hasData(): Boolean = state.state != undefined

    private suspend fun CoroutineScope.fetchDataLoop() {
        while (isActive) {
            try {
                val jvbData = window.fetch(props.url, RequestInit(mode = RequestMode.CORS))
                    .await()
                    .json()
                    .await()
                    .asDynamic()
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
}

external interface JvbState : RState {
    var state: dynamic
    var error: String?
}

external interface JvbProps : RProps {
    var url: String
    var updateIntervalMs: Long?
}

