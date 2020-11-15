import highcharts.*
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.w3c.dom.Element
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.ReactElement
import react.createRef
import react.dom.div
import react.ref
import react.useRef

class LiveGraphRef : RComponent<LiveGraphRefProps, LiveGraphRefState>() {
    private var numDataPoints = 0

    override fun LiveGraphRefState.init() {
        MainScope().launch {
            while (true) {
                val point = props.channel.receive()
                val chart = state.myRef.asDynamic().chart.unsafeCast<Chart>()
                val shift = numDataPoints >= 10
                chart.series[0].addPoint(Point().apply { x = point.timestamp; y = point.value }, true, false)
                numDataPoints++
            }
        }
    }

    override fun shouldComponentUpdate(nextProps: LiveGraphRefProps, nextState: LiveGraphRefState): Boolean {
        return false
    }

    override fun componentWillMount() {
        console.log("will mount")
    }

    override fun componentWillUnmount() {
        console.log("will unmount")
    }

    override fun componentWillUpdate(nextProps: LiveGraphRefProps, nextState: LiveGraphRefState) {
        console.log("component updating: nextProps:", nextProps, " nextState: ", nextState)
    }

    override fun componentDidMount() {
        console.log("ref", state.myRef)
        console.log("chart: ", state.myRef.asDynamic().chart)
    }

    override fun RBuilder.render() {
        console.log("rendering")
        val chartOpts = Options().apply {
            title = Title(props.info.name)
            series = arrayOf(
                SeriesOptions(
                    type = "spline",
                    name = props.info.name,
                    data = arrayOf()
                )
            )
            xAxis = XAxis("datetime")
            chart = ChartOptions().apply {
                zoomType = "x"
            }
        }
        div {
            HighchartsReact {
                attrs.highcharts = highcharts
                attrs.options = chartOpts
                attrs.allowChartUpdate = true
                ref {
                    state.myRef = it
                }
            }
        }
    }
}

external interface LiveGraphRefProps : RProps {
    var info: GraphInfo
    var channel: ReceiveChannel<TimeSeriesPoint>
}

external interface LiveGraphRefState : RState {
    var myRef: ReactElement?
}
