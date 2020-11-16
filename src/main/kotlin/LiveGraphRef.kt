import highcharts.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import react.*
import react.dom.div

class LiveGraphRef : RComponent<LiveGraphRefProps, LiveGraphRefState>() {
    private var numDataPoints = 0
    private var knownSeries = mutableSetOf<String>()

    override fun LiveGraphRefState.init() {
        GlobalScope.launch {
            while (true) {
                val point = props.channel.receive()
                val chart = state.myRef.asDynamic().chart.unsafeCast<Chart>()
                if (point.key !in knownSeries) {
                    chart.addSeries(
                        SeriesOptions(
                            type = "spline",
                            name = point.key,
                            data = arrayOf()
                        )
                    )
                    knownSeries.add(point.key)
                }
                val series = chart.series.find { it.name == point.key }
                series?.addPoint(Point().apply { x = point.timestamp; y = point.value }, true)
                numDataPoints++
            }
        }
    }

    override fun shouldComponentUpdate(nextProps: LiveGraphRefProps, nextState: LiveGraphRefState): Boolean {
        return false
    }

    override fun componentDidMount() {
        console.log("ref", state.myRef)
        console.log("chart: ", state.myRef.asDynamic().chart)
    }

    override fun RBuilder.render() {
        val seriesOptions = props.info.series.map { seriesInfo ->
            SeriesOptions(
                type = "spline",
                name = seriesInfo.name,
                data = arrayOf()
            )
        }.toTypedArray()
        val chartOpts = Options().apply {
            title = Title(props.info.title)
            series = seriesOptions
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
