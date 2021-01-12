import kotlinx.browser.window
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.await
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
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
    private val epChannels: MutableMap<String, Channel<dynamic>> = mutableMapOf()
    private var job: Job? = null

    init {
        state.epIds = arrayOf()
        state.expanded = false
        state.dataByEp = null
    }

    override fun componentDidMount() {
        // Start the fetch data job if there's a URL to query
        props.baseRestApiUrl?.let { baseRestApiUrl ->
            job = GlobalScope.launch { fetchDataLoop(baseRestApiUrl) }
        }
        if ((state.epIds == undefined || state.epIds.isEmpty()) && props.confData != null) {
            val confData = props.confData!!
            // We need to extract all epIds present in all the data
            val allEpIds = confData.flatMap {
                val epIds = getEpIds(it).toList()
                epIds
            }.toSet()
            val dataByEp = mutableMapOf<String, MutableList<dynamic>>()
            confData.forEach { confDataEntry ->
                val timestamp = confDataEntry.timestamp as Number
                val epIds = getEpIds(confDataEntry)
                epIds.forEach { epId ->
                    val epData = confDataEntry.endpoints[epId]
                    val existingEpData = dataByEp.getOrPut(epId) { mutableListOf() }
                    existingEpData.add(EndpointData(timestamp, epData))
                }
            }
            val name = confData.asSequence().map { it.name }.first { it != undefined } ?: "No conf name found"
            setState {
                epIds = allEpIds.toTypedArray()
                this.dataByEp = dataByEp
                this.name = name
            }
        }
    }

    override fun componentWillUnmount() {
        job?.cancel("Unmounting")
        epChannels.forEach { (_, channel) ->
            channel.close()
        }
    }

    // TODO: we need this function because react doesn't do a deep comparison on the epIds array to know whether
    //  or not it changed.  We could introduce an 'EndpointList' component whose only state was the IDs, and then
    //  get rid of this override here and move it there (which would be a bit cleaner, since here we may add more state
    //  and would have to remember to take it into account in this method)
    override fun shouldComponentUpdate(nextProps: ConferenceProps, nextState: ConferenceState): Boolean {
        return (state.expanded != nextState.expanded) ||
            (state.name != nextState.name) ||
            (!state.epIds.contentEquals(nextState.epIds))
    }

    override fun RBuilder.render() {
        console.log("conference ${props.id} renderingz")
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
                                confId = props.id
                                id = epId
                                baseRestApiUrl = props.baseRestApiUrl
                                channel = epChannel
                                state.dataByEp?.get(epId)?.let { existingEpData ->
                                    console.log("have data for ep ", epId)
                                    data = existingEpData
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // The baseRestApiUrl in props may be null, so we call this (with a non-null version) only when it isn't null
    private suspend fun CoroutineScope.fetchDataLoop(baseRestApiUrl: String) {
        while (isActive) {
            try {
                val jvbData = window.fetch("$baseRestApiUrl/${props.id}")
                    .await()
                    .json()
                    .await()
                    .asDynamic()
                val now = jvbData.time.unsafeCast<Number>()
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
            } catch (c: CancellationException) {
                console.log("Conference ${props.id} fetch loop cancelled")
                throw c
            } catch (t: Throwable) {
                console.log("Conference ${props.id} fetch loop error: ", t)
            }
        }
    }
}

private fun getEpIds(confData: dynamic): Array<String> {
    return if (confData.endpoints != undefined) { keys(confData.endpoints) } else emptyArray()
}

external interface ConferenceState : RState {
    var epIds: Array<String>
    var name: String
    var expanded: Boolean
    var dataByEp: MutableMap<String, MutableList<EndpointData>>?
}

data class ConfData(
    val timestamp: Number,
    val data: dynamic
)

external interface ConferenceProps : RProps {
    var baseRestApiUrl: String?
    var id: String
    var confData: List<dynamic>?
}
