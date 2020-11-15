import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import react.*
import react.dom.p

class Conference : RComponent<ConferenceProps, ConferenceState>() {
    private val epChannels = mutableMapOf<String, Channel<EndpointData>>()
    override fun ConferenceState.init() {
        val mainScope = MainScope()
        mainScope.launch {
            for (i in 0..10) {
                val jvbData = fetchData()
                val now = jvbData.time as Number
                val confData = jvbData.conferences[props.id]
                val epIds = getEpIds(confData)
                // TODO: how to handle eps getting added or removed?
                epChannels.forEach { (epId, epChannel) ->
                    val epData = confData.endpoints[epId]
                    epChannel.send(EndpointData(now, epData))
                }
                setState {
                    this.epIds = epIds
                }
                delay(1000)
            }
        }
    }

    override fun RBuilder.render() {
        +"Conference id ${props.id}"
        if (state.epIds == undefined || state.epIds.isEmpty()) {
            p {
                +"No data received yet"
                return
            }
        }
        state.epIds.forEach { epId ->
            val epChannel = epChannels.getOrPut(epId) { Channel()}
            child(Endpoint::class) {
                attrs {
                    id = epId
                    channel = epChannel
                }
            }
            return
        }
    }

//    private val epIds: Array<String>
//        get() {
//            if (!state.state) {
//                return arrayOf()
//            }
//            return keys(state.state.endpoints)
//        }

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
}


external interface ConferenceProps : RProps {
    var baseUrl: String
    var id: String
}
