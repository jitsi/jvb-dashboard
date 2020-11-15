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

class Endpoint : RComponent<EpProps, EpState>() {
    private var numPacketsReceived = mutableListOf<Point>()
    private var graphChannel = Channel<TimeSeriesPoint>()
    override fun EpState.init() {
        MainScope().launch {
            while (true) {
                val epData = props.channel.receive()
                console.log("got ep data", epData)
                val time = epData.timestamp
                val numPacketsReceivedValue = epData.data.iceTransport.num_packets_received
                graphChannel.send(TimeSeriesPoint(time, numPacketsReceivedValue))
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
            attrs.channel = graphChannel
            attrs.info = GraphInfo("numPacketsReceived", js("{}"))
        }
//        MainScope().launch {
//            var num = 0
//            while (true) {
//                println("Sending point")
//                channel.send(TimeSeriesPoint(num.toLong(), num))
//                num++
//                delay(1000)
//            }
//        }
//        val chartOpts = ChartOptions(
//            title = Title("numPacketsReceived"),
//            series = arrayOf(
//                Series(
//                    type = "spline",
//                    name = "foo",
//                    data = numPacketsReceived.toTypedArray()
//                )
//            ),
//            xAxis = XAxis("datetime"),
//        )
//        val options = Options().apply {
//            title = Title("numPacketsReceived")
//            series = arrayOf(
//                SeriesOptions(
//                    type = "spline",
//                    name = "foo",
//                    data = arrayOf(
//                        Point(1, 1),
//                        Point(2, 2),
//                        Point(3, 3),
//                    )
//                )
//            )
//            xAxis = XAxis("datetime")
//            chart = ChartOptions().apply {
//                events = ChartEventsOptions().apply {
//                    load = { event ->
//                        val chart = event.target.unsafeCast<Chart>()
//                        val series = chart.series[0]
//                        window.setInterval({ ->
//                            series.addPoint(Point(4, 4), true, true)
//                        }, 1000)
////                        console.log("chart: ", chart)
////                        console.log("chart.series: ", chart.series)
////                        chart.zoomOut()
////                        chart.redraw()
//                    }
//                }
//            }
//        }
//        HighchartsReact {
//            attrs.highcharts = highcharts
//            attrs.options = options
//        }
    }
}

external interface EpProps : RProps {
    var id: String
    var channel: ReceiveChannel<EndpointData>
//    var timestamp: Number
//    var data: dynamic
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
