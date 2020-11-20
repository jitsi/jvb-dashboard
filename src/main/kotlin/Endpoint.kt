import graphs.LiveZoomAdjustment
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.html.js.onClickFunction
import react.*
import react.dom.button
import react.dom.div
import react.dom.h3
import react.dom.key

class Endpoint : RComponent<EpProps, EpState>() {
    // The Endpoint broadcasts its received data onto this channel for all the graphs to receive
    private val broadcastChannel = BroadcastChannel<Any>(5)
    // A list of all possible key paths in the stats for this endpoint
    private var availableGraphs: List<String> = listOf()
    private var job: Job? = null
    private var nextGraphId: Int = 0

    init {
        state.graphs = listOf()
        state.allKeys = listOf()
        state.statsId = null
    }

    override fun componentWillUnmount() {
        job?.cancel()
        broadcastChannel.close()
    }

    private suspend fun CoroutineScope.handleMessages() {
        try {
            while (isActive) {
                val epData = props.channel.receive()
                if (availableGraphs.isEmpty()) {
                    availableGraphs = getAllKeys(epData.data)
                    console.log("Got all keys: ", availableGraphs)
                    setState {
                        allKeys = availableGraphs
                    }
                }
                if (state.statsId == null) {
                    console.log("Setting stats id to ", epData.data.statsId)
                    setState {
                        statsId = epData.data.statsId.unsafeCast<String>()
                    }
                }
                // Pass the epData down to all graphs
                broadcastChannel.send(epData)
            }
        } catch (c: CancellationException) {
            console.log("endpoint data send loop cancelled")
            throw c
        } catch (t: Throwable) {
            console.log("endpoint data send loop error: ", t)
        }
    }

    override fun componentDidMount() {
        job = GlobalScope.launch { handleMessages() }
    }

    private fun addGraph() {
        val newGraph = Graph(nextGraphId++, broadcastChannel.openSubscription())
        setState {
            graphs += newGraph
        }
    }

    private fun removeGraph(graphId: Int) {
        setState {
            graphs = graphs.filter { it.id != graphId }
        }
    }

    override fun RBuilder.render() {
        console.log("endpoint ${props.id} rendering")
        h3 {
            if (!state.statsId.isNullOrEmpty()) {
                +"Endoint ${props.id} (${state.statsId})   "
            } else {
                +"Endpoint ${props.id}   "
            }
            button {
                attrs.text("Add graph")
                attrs.onClickFunction = { _ ->
                    addGraph()
                }
            }
            button {
                attrs {
                    text("5 secs")
                    onClickFunction = {
                        GlobalScope.launch {
                            broadcastChannel.send(LiveZoomAdjustment(5))
                        }
                    }
                }
            }
            button {
                attrs {
                    text("30 secs")
                    onClickFunction = {
                        GlobalScope.launch {
                            broadcastChannel.send(LiveZoomAdjustment(30))
                        }
                    }
                }
            }
        }
        state.graphs.forEach { graph ->
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
                    attrs.name = "Graph ${graph.id}"
                    attrs.allKeys = availableGraphs
                    attrs.channel = graph.channel
                }
            }
        }
    }
}

external interface EpProps : RProps {
    var id: String
    var channel: ReceiveChannel<EndpointData>
}

data class EndpointData(
    val timestamp: Number,
    val data: dynamic
)

// Endpoints don't retrieve their own data, the conference makes a single
// request and updates the props of the ep components
external interface EpState : RState {
    var allKeys: List<String>
    var graphs: List<Graph>
    var statsId: String?
}

data class Graph(
    val id: Int,
    val channel: ReceiveChannel<Any>
)

