import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import kotlinx.html.js.onClickFunction
import react.*
import react.dom.button
import react.dom.div

class Endpoint : RComponent<EpProps, EpState>() {
//    private var graphChannels = mutableMapOf<String, Channel<TimeSeriesPoint>>()
    private val graphChannels = mutableListOf<ReceiveChannel<EndpointData>>()
    private val broadcastChannel = BroadcastChannel<EndpointData>(5)
    // A list of all possible key paths
    private var availableGraphs: List<String> = listOf()

    init {
        state.numGraphs = 0
        state.allKeys = listOf()
    }

    override fun EpState.init() {
        GlobalScope.launch {
            while (true) {
                val epData = props.channel.receive()
                if (availableGraphs.isEmpty()) {
                    availableGraphs = getAllKeys(epData.data)
                    console.log("Got all keys: ", availableGraphs)
                    setState {
                        allKeys = availableGraphs
                    }
                }
                val time = epData.timestamp
                // Pass the epData down to all graphs
                broadcastChannel.send(epData)
            }
        }
    }

    // Our props are static, so we don't re-update...all data updates come on the channel
//    override fun shouldComponentUpdate(nextProps: EpProps, nextState: EpState): Boolean {
//        return false
//    }

    override fun RBuilder.render() {
        +"Endpoint ${props.id}"
//        if (!props.data) {
//            +"No data"
//            return
//        }
        button {
            attrs.text("Add graph")
            attrs.onClickFunction = { event ->
                console.log("button clicked!")
                state.numGraphs++
                // Create a fixed channel here for this new graph, as we need to re-use the same
                // channels every time we re-render (or else it will look like the broadcast channel
                // has lots of receivers, and it will fill up since not all of them will read)
                graphChannels.add(broadcastChannel.openSubscription())
            }
        }
        repeat(state.numGraphs) { index ->
            div {
                child(GraphFilter::class) {
                    attrs.allKeys = availableGraphs
                    attrs.channel = graphChannels[index]
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
    var numGraphs: Int
}

inline fun jsObject(init: dynamic.() -> Unit): dynamic {
    val o = js("{}")
    init(o)
    return o
}
