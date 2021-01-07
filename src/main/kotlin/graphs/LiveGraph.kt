package graphs

import highcharts.Chart
import highcharts.ChartOptions
import highcharts.HighchartsReact
import highcharts.Options
import highcharts.PlotOptions
import highcharts.PlotSeriesOptions
import highcharts.Point
import highcharts.PointMarkerOptionsObject
import highcharts.Series
import highcharts.SeriesOptions
import highcharts.SeriesOptionsType
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
                SeriesOptions().apply {
                    type = "spline"
                    name = seriesName
                    data = arrayOf()
                }
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

        // The minimum displayed x value of the graph is either now - the zoom window, or the oldest point,
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

    private fun setTimeseries(timeseries: Timeseries) {
        val series = getOrCreateSeries(chart, timeseries.name)
        series.setData(timeseries.points.toTypedArray())
    }

    private suspend fun CoroutineScope.handleMessages() {
        try {
            while (isActive) {
                when (val msg = props.channel.receive()) {
                    is NewDataMsg -> addPoint(msg.timeSeriesPoint)
                    is SetDataMsg -> setTimeseries(msg.timeseries)
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
                        // The markers are nice, but they cause a big hit to performance with
                        // live graphs
                        enabled = false
                    }
                }
            }
            if (props.enableZoom) {
                chart = ChartOptions().apply {
                    zoomType = "x"
                }
            }
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
    var channel: ReceiveChannel<GraphMsg>
    // Zooming (via the highcharts click and drag) doesn't currently work well
    // with live data, since we do our own manual zoom there (I think this can
    // be fixed, but it isn't done yet).  If viewing a dump, though, then we want
    // to enable the highcharts zoom
    var enableZoom: Boolean
}

// TODO: can we define this better w.r.t the definition of TimeSeries used by Graph?
data class TimeSeriesPoint(
    val timestamp: Number,
    val key: String,
    val value: Number
)

data class Timeseries(
    val name: String,
    val points: List<Point>
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
