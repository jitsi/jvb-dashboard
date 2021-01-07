import graphs.GraphFilter
import graphs.LiveZoomAdjustment
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.css.paddingLeft
import kotlinx.css.paddingTop
import kotlinx.css.pct
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.button
import react.dom.div
import react.dom.h3
import react.setState
import styled.css
import styled.styledDiv

class Endpoint : RComponent<EpProps, EpState>() {
    // The Endpoint broadcasts its received data onto this channel for all the graphs to receive
    private val broadcastChannel = BroadcastChannel<Any>(5)
    private var job: Job? = null
    private var nextGraphId: Int = 0

    init {
        state.graphInfos = listOf()
        state.numericalKeys = listOf()
        state.statsId = null
    }

    override fun componentDidMount() {
        job = GlobalScope.launch { handleMessages() }
        if (state.numericalKeys.isEmpty() && props.data != undefined) {
            console.log("ep allkeys is not set, and we have data in props, filling out there")
            setState {
                // TODO: find all keys from all entries, not just the first one
                numericalKeys = getAllKeys(props.data!!.first().data)
            }
        }
    }

    override fun componentWillUnmount() {
        job?.cancel("Unmounting")
        broadcastChannel.close()
    }

    private suspend fun CoroutineScope.handleMessages() {
        try {
            while (isActive) {
                val epData = props.channel.receive()
                if (state.numericalKeys.isEmpty()) {
                    // Build the list of available keys the first time we get data.
                    // NOTE: This means keys that didn't show up later won't be displayed,
                    // if we need to cover that then we can add any missing ones each time
                    val allKeys = getAllKeys(epData.data).filter { key ->
                        isNumber(getValue(epData.data, key))
                    }
                    console.log("Endpoint ${props.id}: Got all (numerical) keys: ", allKeys)
                    setState {
                        this.numericalKeys = allKeys
                    }
                }
                // Set the stats ID if it isn't already set
                if (state.statsId == null) {
                    setState {
                        statsId = epData.data.statsId.unsafeCast<String>()
                    }
                }
                // Pass the epData down to all graphs
                broadcastChannel.send(epData)
            }
        } catch (c: CancellationException) {
            console.log("endpoint data send loop cancelled: ${c.message}")
            throw c
        } catch (t: Throwable) {
            console.log("endpoint data send loop error: ", t)
        }
    }

    private fun addGraph() {
        val newGraph = GraphInfo(nextGraphId++, broadcastChannel.openSubscription())
        setState {
            graphInfos += newGraph
        }
    }

    private fun removeGraph(graphId: Int) {
        setState {
            graphInfos = graphInfos.filterNot { it.id == graphId }
        }
    }

    override fun RBuilder.render() {
        console.log("Endpoint ${props.id}: rendering")
        div {
            h3 {
                if (!state.statsId.isNullOrEmpty()) {
                    +"Endpoint ${props.id} (${state.statsId})   "
                } else {
                    +"Endpoint ${props.id}   "
                }
            }
            if (usingLiveData()) {
                div {
                    child(FeatureToggle::class) {
                        attrs {
                            featureName = "PCAP dump"
                            url = "${props.baseRestApiUrl}/features/endpoint/${props.confId}/${props.id}/pcap-dump"
                        }
                    }
                }
            }
            div {
                button {
                    attrs.text("Add graph")
                    attrs.onClickFunction = { _ ->
                        addGraph()
                    }
                }
                if (usingLiveData() && state.graphInfos.isNotEmpty()) {
                    button {
                        attrs {
                            text("1 min")
                            onClickFunction = {
                                GlobalScope.launch {
                                    broadcastChannel.send(LiveZoomAdjustment(60))
                                }
                            }
                        }
                    }
                    button {
                        attrs {
                            text("5 mins")
                            onClickFunction = {
                                GlobalScope.launch {
                                    broadcastChannel.send(LiveZoomAdjustment(300))
                                }
                            }
                        }
                    }
                    button {
                        attrs {
                            text("All")
                            onClickFunction = {
                                GlobalScope.launch {
                                    broadcastChannel.send(LiveZoomAdjustment(Int.MAX_VALUE))
                                }
                            }
                        }
                    }
                }
            }
            styledDiv {
                css {
                    paddingLeft = 2.pct
                    paddingTop = 2.pct
                }
                state.graphInfos.forEach { graph ->
                    console.log("rendering graph ${graph.id}")
                    div {
                        key = graph.id.toString()
                        button {
                            key = "remove-graph-${graph.id}"
                            attrs.value = graph.id.toString()
                            attrs.text("Remove graph")
                            attrs.onClickFunction = { _ ->
                                removeGraph(graph.id)
                            }
                        }
                        child(GraphFilter::class) {
                            key = "graph-filter-${graph.id}"
                            attrs {
                                name = "Graph ${graph.id}"
                                allKeys = state.numericalKeys
                                channel = graph.channel
                                data = props.data
                            }
                        }
                    }
                }
            }
        }
    }

    private fun usingLiveData(): Boolean = props.baseRestApiUrl != null
}

external interface EpProps : RProps {
    var confId: String
    var id: String
    var baseRestApiUrl: String?
    var channel: ReceiveChannel<EndpointData>
    // An optional property to pass pre-existing data (e.g. from a dump file)
    var data: List<EndpointData>?
}

data class EndpointData(
    val timestamp: Number,
    val data: dynamic
)

// Endpoints don't retrieve their own data, the conference makes a single
// request and updates the props of the ep components
external interface EpState : RState {
    var numericalKeys: List<String>
    var graphInfos: List<GraphInfo>
    var statsId: String?
}

data class GraphInfo(
    val id: Int,
    val channel: ReceiveChannel<Any>
)

