import highcharts.*
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
        numPacketsReceived.add(Point(props.timestamp, props.data.iceTransport.num_packets_received))
        val chartOpts = ChartOptions(
            title = Title("numPacketsReceived"),
            series = arrayOf(
                Series(
                    type = "spline",
                    name = "foo",
                    data = numPacketsReceived.toTypedArray()
                )
            ),
            xAxis = XAxis("datetime"),
        )
        HighchartsReact {
            attrs.highcharts = highcharts
            attrs.options = chartOpts
        }
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
