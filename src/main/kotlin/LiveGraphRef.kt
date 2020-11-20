import highcharts.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import react.*
import react.dom.div
import kotlin.js.Date

class LiveGraphRef : RComponent<LiveGraphRefProps, LiveGraphRefState>() {
    private var numDataPoints = 0
    private var knownSeries = mutableSetOf<String>()
    private var job: Job? = null
    private var originalMin: Number = 0

    override fun componentWillUnmount() {
        console.log("live graph ${props.info.title} cancelling graph coro ${props.info.title}")
        job?.cancel()
        props.channel.cancel()
        console.log("live graph ${props.info.title} coro canceled")
    }

    override fun shouldComponentUpdate(nextProps: LiveGraphRefProps, nextState: LiveGraphRefState): Boolean {
        return false
    }

    override fun componentDidMount() {
        console.log("ref", state.myRef)
        console.log("chart: ", state.myRef.asDynamic().chart)
        job = GlobalScope.launch {
            try {
                while (isActive) {
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
                    // We just added it, so should be safe to assume
                    val series = chart.series.find { it.name == point.key }!!
                    series.addPoint(Point().apply { x = point.timestamp; y = point.value }, true)
                    console.log("series data size is now ", series.data.size)
                    if (series.data.size > 10 && series.data.size < 20) {
                        val xAxis = series.chart.xAxis[0]
                        val max = Date(xAxis.max!!)
                        val min = Date(xAxis.min!!)
                        if (originalMin == 0) {
                            originalMin = min.getTime()
                        }
                        console.log("current max and min: ", max, min)
                        console.log("setting min to ", max.getSeconds() - 10)
                        xAxis.setExtremes(max.getTime() - 10000, redraw = true)
                    } else if (series.data.size == 20) {
                        val xAxis = series.chart.xAxis[0]
                        xAxis.setExtremes(originalMin)
                    }
                    numDataPoints++
                }
            } catch (c: CancellationException) {
                console.log("live graph ${props.info.title} data receive loop cancelled")
                throw c
            } catch (c: ClosedReceiveChannelException) {
                console.log("live graph ${props.info.title} receive channel closed")
            } catch (t: Throwable) {
                console.log("live graph ${props.info.title} loop error: ", t)
            }
        }
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
