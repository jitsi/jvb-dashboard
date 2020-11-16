import kotlinx.browser.window
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import react.*
import react.dom.div
import react.dom.p

class Conference : RComponent<ConferenceProps, ConferenceState>() {
    private val epChannels = mutableMapOf<String, Channel<EndpointData>>()
    init {
        state.epIds = arrayOf()
    }
    override fun ConferenceState.init() {
        GlobalScope.launch {
            try {
                while (true) {
                    val jvbData = fetchData()
                    val now = jvbData.time as Number
                    val confData = jvbData.conferences[props.id]
                    val epIds = getEpIds(confData)
                    setState {
                        this.name = confData.name
                        this.epIds = epIds
                    }
                    epChannels.forEach { (epId, epChannel) ->
                        val epData = confData.endpoints[epId]
                        epChannel.send(EndpointData(now, epData))
                    }
                    delay(1000)
                }
            } catch (t: Throwable) {
                console.log("exception: ", t)
            }
        }
    }

    override fun RBuilder.render() {
        if (state.name != undefined) {
            +"Conference id ${props.id} name ${state.name}"
        } else {
            +"Conference id ${props.id}"
        }
        if (state.epIds == undefined || state.epIds.isEmpty()) {
            p {
                +"No data received yet"
                return
            }
        }
        state.epIds.forEach { epId ->
            val epChannel = epChannels.getOrPut(epId) { Channel() }
            div {
                child(Endpoint::class) {
                    key = epId
                    attrs {
                        id = epId
                        channel = epChannel
                    }
                }
            }
        }
    }

    private suspend fun fetchData(): dynamic {
        return window.fetch("${props.baseUrl}/${props.id}")
            .await()
            .json()
            .await()
    }
}

private fun getEpIds(confData: dynamic): Array<String> {
    return keys(confData.endpoints)
}

external interface ConferenceState : RState {
    var epIds: Array<String>
    var name: String
}


external interface ConferenceProps : RProps {
    var baseUrl: String
    var id: String
}
