import highcharts.*
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.p

class Endpoint : RComponent<EpProps, EpState>() {
    override fun RBuilder.render() {
        +"Endpoint ${props.id}"
        if (!props.data) {
            +"No data"
            return
        }
        val num_packets_received = props.data.iceTransport.num_packets_received
        p {
            +"num packets received: $num_packets_received"
        }
        val chartOpts = ChartOptions(
            title = Title("foo"),
            series = arrayOf(
                Series(
                    type = "line",
                    name = "foo",
                    data = arrayOf(
                        Point(1, 1),
                        Point(2, 2),
                        Point(3, 3),
                    )
                )
            ),
            xAxis = XAxis("datetime")
        )
        val x = HighchartsReact {
            attrs.highcharts = highcharts
            attrs.options = chartOpts
        }
    }
}

external interface EpProps : RProps {
    var id: String
    var data: dynamic
}

// Endpoints don't retrieve their own data, the conference makes a single
// request and updates the props of the ep components
external interface EpState : RState {
//    var state: dynamic
}
