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
import highcharts.Title
import highcharts.XAxis
import highcharts.highcharts
import react.RBuilder
import react.RComponent
import react.PropsWithChildren
import react.State
import react.ReactElement
import react.dom.div
import react.useRefCallback
import kotlin.js.Date

class Chart : RComponent<GraphProps, State>() {
    private var chartRef: ReactElement<GraphProps>? = null
    private val maxPoints: Int = 60 * 60
    // How many seconds worth of live data we're currently displaying
    private var currentTimeZoomSeconds: Long = maxPoints.toLong()

    private val chart: Chart
        get() = chartRef.asDynamic().chart.unsafeCast<Chart>()

    override fun componentDidMount() {
        props.startZoomSeconds?.let {
            currentTimeZoomSeconds = it.toLong()
        }
    }

    override fun RBuilder.render() {
        val options = Options().apply {
            title = Title(props.title ?: "untitled")
            xAxis = XAxis("datetime")
            plotOptions = PlotOptions().apply {
                series = PlotSeriesOptions().apply {
                    marker = PointMarkerOptionsObject().apply {
                        // The markers are nice, but they cause a big hit to performance with
                        // live graphs
                        enabled = false
                    }
                    step = "left"
                    turboThreshold = 0
                }
            }
            chart = ChartOptions().apply {
                if (props.enableZoom) {
                    zoomType = "x"
                }
                type = props.graphType ?: "line"
            }
        }
        div {
            HighchartsReact {
                attrs.highcharts = highcharts
                attrs.options = options
                attrs.allowChartUpdate = true
                ref = useRefCallback<ReactElement<GraphProps>> {
                    chartRef = it
                }
            }
        }
    }

    /**
     * Get the [Series] with the name [seriesName] from [chart], or create and return it if it didn't
     * already exist.
     */
    private fun getOrCreateSeries(chart: Chart, seriesName: String): Series {
        return chart.series.find { it.name == seriesName } ?: run {
            chart.addSeries(
                SeriesOptions().apply {
                    name = seriesName
                    data = arrayOf()
                }
            )
        }
    }

    /**
     * Add a new [Point] to series [seriesName].  If a series with name
     * [seriesName] does not currently exist, it will be added to the graph.
     */
    fun addPoint(seriesName: String, point: Point) {
        val series = getOrCreateSeries(chart, seriesName)
        series.addPoint(point)
        // Limit how many points we store
        while (series.data.size > maxPoints) {
            series.removePoint(0)
        }
        updateZoom()
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

    fun setZoom(zoomSeconds: Int) {
        currentTimeZoomSeconds = zoomSeconds.toLong()
        updateZoom()
    }

    /**
     * Add a complete timeseries to the graph
     */
    fun addTimeseries(seriesName: String, points: List<Point>) {
        val series = getOrCreateSeries(chart, seriesName)
        series.setData(points.toTypedArray())
    }

    /**
     * Remove a series from the graph
     */
    fun removeSeries(seriesName: String) {
        val series = chart.series.find { it.name == seriesName }
        series?.remove(redraw = true)
    }
}

external interface GraphProps : PropsWithChildren {
    var title: String?
    var enableZoom: Boolean
    var graphType: String?
    var startZoomSeconds: Int?
}

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
