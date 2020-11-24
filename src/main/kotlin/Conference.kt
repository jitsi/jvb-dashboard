import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.css.paddingLeft
import kotlinx.css.pct
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.div
import react.dom.h2
import react.dom.p
import react.setState
import styled.css
import styled.styledDiv

class Conference : RComponent<ConferenceProps, ConferenceState>() {
    private val epChannels = mutableMapOf<String, Channel<EndpointData>>()
    init {
        state.epIds = arrayOf()
        state.expanded = false
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
                        this.name = confData.name.unsafeCast<String>().substringBefore('@')
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

    override fun shouldComponentUpdate(nextProps: ConferenceProps, nextState: ConferenceState): Boolean {
        return (state.expanded != nextState.expanded) ||
                (state.name != nextState.name) ||
                (!state.epIds.contentEquals(nextState.epIds))
    }

    override fun RBuilder.render() {
        console.log("conference ${props.id} rendering")
        div {
            key = props.id
            h2 {
                if (state.expanded) {
                    +"▾"
                } else {
                    +"▸"
                }
                if (state.name != undefined) {
                    +"Conference \"${state.name}\" (${props.id})"
                } else {
                    +"Conference (${props.id})"
                }
                attrs {
                    onClickFunction = { _ ->
                        console.log("toggling expand")
                        setState {
                            expanded = !expanded
                        }
                    }
                }
            }
            if (state.expanded) {
                if (state.epIds == undefined || state.epIds.isEmpty()) {
                    p {
                        +"No data received yet"
                        return
                    }
                }
                state.epIds.forEach { epId ->
                    val epChannel = epChannels.getOrPut(epId) { Channel() }
                    styledDiv {
                        css {
                            paddingLeft = 2.pct
                        }
                        key = epId
                        child(Endpoint::class) {
                            attrs {
                                id = epId
                                channel = epChannel
                            }
                        }
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
    var expanded: Boolean
}

external interface ConferenceProps : RProps {
    var baseUrl: String
    var id: String
}
