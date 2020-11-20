import highcharts.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import react.*
import react.dom.div
import kotlin.js.Date
import kotlin.time.seconds

class LiveGraphRef : RComponent<LiveGraphRefProps, LiveGraphRefState>() {
    private var knownSeries = mutableSetOf<String>()
    private var job: Job? = null

    init {
        // Only track an hour's worth of data
        state.maxPoints = 30 //60 * 60
        state.currentTimeZoomSeconds = state.maxPoints
    }

    override fun componentWillUnmount() {
        console.log("live graph ${props.info.title} cancelling graph coro ${props.info.title}")
        job?.cancel()
        props.channel.cancel()
        console.log("live graph ${props.info.title} coro canceled")
    }

    override fun shouldComponentUpdate(nextProps: LiveGraphRefProps, nextState: LiveGraphRefState): Boolean {
        return false
    }

    private fun getOrCreateSeries(chart: Chart, seriesName: String): Series {
        return chart.series.find { it.name == seriesName } ?: run {
            if (seriesName in knownSeries) {
                console.warn("Series $seriesName wasn't in chart but was in known series")
            }
            chart.addSeries(
                SeriesOptions(
                    type = "spline",
                    name = seriesName,
                    data = arrayOf()
                )
            ).also {
                knownSeries.add(seriesName)
            }
        }
    }

    private fun addPoint(chart: Chart, point: TimeSeriesPoint) {
        val series = getOrCreateSeries(chart, point.key)
        series.addPoint(Point(point.timestamp, point.value))
        // Limit how many points we store
        while (series.data.size > state.maxPoints) {
            series.removePoint(0)
        }
        // Zoom to any requested live window
        val windowStart = Date(series.xAxis.min!!)
        val now = Date()
        if (now.getSeconds() - windowStart.getSeconds() > state.currentTimeZoomSeconds) {
            series.xAxis.setExtremes(now.getTime() - state.currentTimeZoomSeconds * 1000)
        }
    }

    private suspend fun CoroutineScope.receiveMessages() {
        try {
            val chart = state.myRef.asDynamic().chart.unsafeCast<Chart>()
            while (isActive) {
                when (val msg = props.channel.receive()) {
                    is NewDataMsg -> addPoint(chart, msg.timeSeriesPoint)
                    is LiveZoomAdjustment -> {
                        console.log("updating time zoom to ${msg.numSeconds} seconds")
                        setState {
                            currentTimeZoomSeconds = msg.numSeconds
                        }
                    }
                }
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

    override fun componentDidMount() {
        console.log("ref", state.myRef)
        console.log("chart: ", state.myRef.asDynamic().chart)
        job = GlobalScope.launch { receiveMessages() }
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
    var channel: ReceiveChannel<GraphMsg>
}

external interface LiveGraphRefState : RState {
    var myRef: ReactElement?
    // The maximum amount of datapoints we'll track in the graph
    var maxPoints: Int
    // How many seconds worth of live data we're currently displaying
    var currentTimeZoomSeconds: Int
}

sealed class GraphMsg

// Pass a new TimeSeriesPoint to be rendered on the graph
data class NewDataMsg(val timeSeriesPoint: TimeSeriesPoint) : GraphMsg()

// Adjust how many seconds worth of live data the graph should display
data class LiveZoomAdjustment(val numSeconds: Int) : GraphMsg()
