import highcharts.*
import kotlinx.browser.window
import kotlinx.coroutines.delay
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState

class Endpoint : RComponent<EpProps, EpState>() {
    private var numPacketsReceived = mutableListOf<Point>()
    override fun RBuilder.render() {
        +"Endpoint ${props.id}"
        if (!props.data) {
            +"No data"
            return
        }
//        numPacketsReceived.add(Point(props.timestamp, props.data.iceTransport.num_packets_received))
        child(LiveGraphRef::class) {
            attrs.info = GraphInfo(
                "numPacketsReceived",
                { ->
                    jsObject {
                        time = props.timestamp
                        value = props.data.iceTransport.num_packets_received
                    }
                }
            )
        }
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
    var timestamp: Number
    var data: dynamic
}

// Endpoints don't retrieve their own data, the conference makes a single
// request and updates the props of the ep components
external interface EpState : RState {
//    var state: dynamic
}

inline fun jsObject(init: dynamic.() -> Unit): dynamic {
    val o = js("{}")
    init(o)
    return o
}
