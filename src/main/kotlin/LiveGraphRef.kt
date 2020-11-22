import graphs.GraphControl
import graphs.LiveZoomAdjustment
import highcharts.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import react.*
import react.dom.div
import kotlin.js.Date

class LiveGraphRef : RComponent<LiveGraphRefProps, RState>() {
    private var knownSeries = mutableSetOf<String>()
    private var job: Job? = null
    private var myRef: ReactElement? = null
    // The maximum amount of datapoints we'll track in the graph (one hour's worth)
    private var maxPoints: Int = 60 * 60

    // How many seconds worth of live data we're currently displaying
    private var currentTimeZoomSeconds: Int = maxPoints

    private val chart: Chart
        get() = myRef.asDynamic().chart.unsafeCast<Chart>()

    override fun componentWillUnmount() {
        console.log("live graph ${props.info.title} cancelling graph coro ${props.info.title}")
        job?.cancel()
        props.channel.cancel()
        console.log("live graph ${props.info.title} coro canceled")
    }

    override fun shouldComponentUpdate(nextProps: LiveGraphRefProps, nextState: RState): Boolean {
        return false
    }

    private fun log(msg: String) {
        console.log("graph ${props.info.title}: $msg")
    }

    /**
     * Get the [Series] with the name [seriesName] from [chart], or create and return it if it didn't
     * already exist.
     */
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

    private fun addPoint(point: TimeSeriesPoint) {
        val series = getOrCreateSeries(chart, point.key)
        series.addPoint(Point(point.timestamp, point.value))
        // Limit how many points we store
        while (series.data.size > maxPoints) {
            series.removePoint(0)
        }
        // Zoom to any requested live window
        val windowStart = Date(series.xAxis.min!!)
        val now = Date()
        // The minimum displayed x value of the graph is either the now - the zoom window, or the oldest point, whichever
        // is newer
        val newMin = maxOf(now.getTime() - currentTimeZoomSeconds * 1000, series.data[0].x.toDouble())
        series.xAxis.setExtremes(newMin)
    }

    private suspend fun CoroutineScope.handleMessages() {
        try {
            while (isActive) {
                when (val msg = props.channel.receive()) {
                    is NewDataMsg -> addPoint(msg.timeSeriesPoint)
                    is GraphControl -> handleGraphControlMessage(msg)
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

    private fun handleGraphControlMessage(msg: GraphControl) {
        when (msg) {
            is LiveZoomAdjustment -> {
                log("updating time zoom to ${msg.numSeconds} seconds")
                setState {
                    currentTimeZoomSeconds = minOf(msg.numSeconds, maxPoints)
                }
            }
        }
    }

    override fun componentDidMount() {
        console.log("ref", myRef)
        console.log("chart: ", chart)
        job = GlobalScope.launch { handleMessages() }
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
                    myRef = it
                }
            }
        }
    }
}

external interface LiveGraphRefProps : RProps {
    var info: GraphInfo
    var channel: ReceiveChannel<Any>
}

// Pass a new TimeSeriesPoint to be rendered on the graph
data class NewDataMsg(val timeSeriesPoint: TimeSeriesPoint)
