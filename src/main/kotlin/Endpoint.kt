import highcharts.*
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.setState


// TODO: better way to do this?
fun getValue(obj: dynamic, path: String): dynamic {
    if (path.isBlank()) {
        return obj
    }
    val paths = path.split(".")
    return getValue(obj[paths.first()], paths.drop(1).joinToString("."))
}

class Endpoint : RComponent<EpProps, EpState>() {
    private var numPacketsReceived = mutableListOf<Point>()
    private var graphChannel = Channel<TimeSeriesPoint>()
    private var graphChannels = mutableMapOf<String, Channel<TimeSeriesPoint>>()
    private var graphExtractors = mutableMapOf<String, String>().apply {
        put("numPacketsReceived", "iceTransport.num_packets_received")
    }
    override fun EpState.init() {
        MainScope().launch {
            while (true) {
                val epData = props.channel.receive()
                console.log("got ep data", epData)
                val time = epData.timestamp
                graphChannels.forEach {  (valuePath, channel) ->
                    val value = getValue(epData.data, valuePath)
                    channel.send(TimeSeriesPoint(time, value))
                }
                // Do we need to set this state?
//                setState {
//                    this.epData = epData
//                }
            }
        }
    }

    override fun shouldComponentUpdate(nextProps: EpProps, nextState: EpState): Boolean {
        return false
    }

    override fun RBuilder.render() {
        +"Endpoint ${props.id}"
//        if (!props.data) {
//            +"No data"
//            return
//        }
//        numPacketsReceived.add(Point(props.timestamp, props.data.iceTransport.num_packets_received))
//        val channel = Channel<TimeSeriesPoint>()
        child(LiveGraphRef::class) {
            attrs.channel = graphChannels.getOrPut("iceTransport.num_packets_received") { Channel() }
            attrs.info = GraphInfo("numPacketsReceived", js("{}"))
        }
        child(LiveGraphRef::class) {
            attrs.channel = graphChannels.getOrPut("iceTransport.num_packets_sent") { Channel() }
            attrs.info = GraphInfo("numPacketsSent", js("{}"))
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
    var epData: EndpointData
}

inline fun jsObject(init: dynamic.() -> Unit): dynamic {
    val o = js("{}")
    init(o)
    return o
}
