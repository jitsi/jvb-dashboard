import graphs.GraphMsg
import graphs.LiveGraphControlMsg
import graphs.LiveZoomAdjustment
import graphs.NewDataMsg
import graphs.RemoveSeries
import highcharts.Chart
import highcharts.HighchartsReact
import highcharts.Options
import highcharts.PlotOptions
import highcharts.PlotSeriesOptions
import highcharts.Point
import highcharts.PointMarkerOptionsObject
import highcharts.Series
import highcharts.SeriesOptions
import highcharts.Title
import highcharts.XAxis
import highcharts.highcharts
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.ReactElement
import react.dom.div
import kotlin.js.Date

class LiveGraph : RComponent<LiveGraphProps, RState>() {
    private var knownSeries = mutableSetOf<String>()
    private var job: Job? = null
    private var myRef: ReactElement? = null
    // The maximum amount of datapoints we'll track in the graph (one hour's worth)
    private val maxPoints: Int = 60 * 60

    // How many seconds worth of live data we're currently displaying
    private var currentTimeZoomSeconds: Int = maxPoints

    private val chart: Chart
        get() = myRef.asDynamic().chart.unsafeCast<Chart>()

    override fun componentWillUnmount() {
        log("cancelling graph coro")
        job?.cancel()
        props.channel.cancel()
        log("coro canceled")
    }

    override fun shouldComponentUpdate(nextProps: LiveGraphProps, nextState: RState): Boolean {
        return false
    }

    private fun log(msg: String) {
        console.log("graph ${props.graphTitle}: $msg")
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

    /**
     * Update the zoom level on the graph based on the available data and [currentTimeZoomSeconds].
     */
    private fun updateZoom() {
        val now = Date()
        // Finding nothing here means we don't have any data yet, so there's nothing to do
        val currMin = chart.series.map { it.data.getOrNull(0)?.x?.toDouble() }.min() ?: return

        // The minimum displayed x value of the graph is either the now - the zoom window, or the oldest point,
        // whichever is newer
        val newMin = maxOf(now.getTime() - currentTimeZoomSeconds * 1000, currMin)

        chart.xAxis.forEach { it.setExtremes(newMin) }
    }

    private fun addPoint(point: TimeSeriesPoint) {
        val series = getOrCreateSeries(chart, point.key)
        series.addPoint(Point(point.timestamp, point.value))
        // Limit how many points we store
        while (series.data.size > maxPoints) {
            series.removePoint(0)
        }
        updateZoom()
    }

    private suspend fun CoroutineScope.handleMessages() {
        try {
            while (isActive) {
                when (val msg = props.channel.receive()) {
                    is NewDataMsg -> addPoint(msg.timeSeriesPoint)
                    is LiveGraphControlMsg -> handleGraphControlMessage(msg)
                }
            }
        } catch (c: CancellationException) {
            log("data receive loop cancelled")
            throw c
        } catch (c: ClosedReceiveChannelException) {
            log("receive channel closed")
        } catch (t: Throwable) {
            log("loop error: $t")
        }
    }

    private fun handleGraphControlMessage(msg: LiveGraphControlMsg) {
        when (msg) {
            is LiveZoomAdjustment -> {
                log("Updating time zoom to ${msg.numSeconds} seconds")
                currentTimeZoomSeconds = minOf(msg.numSeconds, maxPoints)
                updateZoom()
            }
            is RemoveSeries -> {
                log("Remove series ${msg.series}")
                msg.series.forEach { seriesName ->
                    val series = chart.series.find { it.name == seriesName } ?: return@forEach
                    series.remove(redraw = true)
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
        val chartOpts = Options().apply {
            title = Title(props.graphTitle)
            xAxis = XAxis("datetime")
            plotOptions = PlotOptions().apply {
                series = PlotSeriesOptions().apply {
                    marker = PointMarkerOptionsObject().apply {
                        // The markers are nice, but they cause a big hit to performance
                        enabled = false
                    }
                }
            }

            // TODO: zoom doesn't work right now because of our manual 'live' zoom, so disable this for now.
            // We'll need to be able to detect this method of zoom and, if it's active, disable our updating
            // of setExtremes.
//            chart = ChartOptions().apply {
//                zoomType = "x"
//            }
        }
        div {
            HighchartsReact {
                attrs.highcharts = highcharts
                attrs.options = chartOpts
                attrs.allowChartUpdate = true
                ref {
                    myRef = it.unsafeCast<ReactElement>()
                }
            }
        }
    }
}

external interface LiveGraphProps : RProps {
    var graphTitle: String
    // TODO: add support for a set of 'initial series' to support presets
    var channel: ReceiveChannel<GraphMsg>
}

data class TimeSeriesPoint(
    val timestamp: Number,
    val key: String,
    val value: Number
)

// TODO: i think I could do this with a reduce instead?  Would that be better?
private fun List<Double?>.min(): Double? {
    if (isEmpty()) {
        return null
    }
    var min: Double? = null
    for (item in this) {
        when {
            item == null -> continue
            min == null -> min = item
            item < min -> min = item
        }
    }
    return min
}
