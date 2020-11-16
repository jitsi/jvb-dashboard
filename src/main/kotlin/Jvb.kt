import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import react.*
import react.dom.p
import kotlin.js.Date

class Jvb : RComponent<JvbProps, JvbState>() {
    init {
        GlobalScope.launch {
            while (true) {
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
        val date = Date(state.state.time.unsafeCast<Number>())
        p {
            +date.toUTCString()
        }
        p {
            +"conferences: ${keys(state.state.conferences)}"
        }
        conferenceIds.forEach { confId ->
            child(Conference::class) {
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
}

external interface JvbProps : RProps {
    var url: String
}

