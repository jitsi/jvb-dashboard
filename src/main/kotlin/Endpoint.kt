import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.html.js.onClickFunction
import react.*
import react.dom.button
import react.dom.div

class Endpoint : RComponent<EpProps, EpState>() {
    // The Endpoint broadcasts its received data onto this channel for all the graphs to receive
    private val broadcastChannel = BroadcastChannel<EndpointData>(5)
    // A list of all possible key paths in the stats for this endpoint
    private var availableGraphs: List<String> = listOf()
    private var job: Job? = null
    private var nextGraphId: Int = 0

    init {
        state.graphs = listOf()
        state.allKeys = listOf()
    }

    override fun componentWillUnmount() {
        job?.cancel()
        broadcastChannel.close()
    }

    override fun componentDidMount() {
        job = GlobalScope.launch {
            try {
                while (isActive) {
                    val epData = props.channel.receive()
                    console.log("ep got data")
                    if (availableGraphs.isEmpty()) {
                        availableGraphs = getAllKeys(epData.data)
                        console.log("Got all keys: ", availableGraphs)
                        setState {
                            allKeys = availableGraphs
                        }
                    }
                    // Pass the epData down to all graphs
                    broadcastChannel.send(epData)
                    console.log("ep broadcasted data")
                }
            } catch (c: CancellationException) {
                console.log("endpoint data send loop cancelled")
                throw c
            } catch (t: Throwable) {
                console.log("endpoint data send loop error: ", t)
            }
        }
    }

    // Our props are static, so we don't re-update...all data updates come on the channel
    override fun shouldComponentUpdate(nextProps: EpProps, nextState: EpState): Boolean {
        return nextState.graphs != state.graphs
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
        +"Endpoint ${props.id}"
        button {
            attrs.text("Add graph")
            attrs.onClickFunction = { event ->
                addGraph()
            }
        }
        state.graphs.forEach { graph ->
            console.log("rendering graph ${graph.id}")
            div {
                button {
                    attrs.value = graph.id.toString()
                    attrs.text("Remove graph")
                    attrs.onClickFunction = { _ ->
                        removeGraph(graph.id)
                    }
                }
                child(GraphFilter::class) {
                    key = graph.id.toString()
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
}

data class Graph(
    val id: Int,
    val channel: ReceiveChannel<EndpointData>
)

