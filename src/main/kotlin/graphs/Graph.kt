package graphs

import highcharts.ChartOptions
import highcharts.HighchartsReact
import highcharts.Options
import highcharts.Point
import highcharts.SeriesOptions
import highcharts.Title
import highcharts.XAxis
import highcharts.highcharts
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.div

open class Graph : RComponent<GraphProps, RState>() {
    override fun RBuilder.render() {
        val chartOpts = Options().apply {
            title = Title(props.graphTitle)
            xAxis = XAxis("datetime")
            chart = ChartOptions().apply {
                zoomType = "x"
            }
            series = props.data.map { timeseries ->
                SeriesOptions(
                    type = "spline",
                    name = timeseries.name,
                    data = timeseries.points.toTypedArray()
                )
            }.toTypedArray()
        }
        div {
            HighchartsReact {
                attrs.highcharts = highcharts
                attrs.options = chartOpts
                attrs.allowChartUpdate = true
            }
        }
    }
}

external interface GraphProps : RProps {
    var graphTitle: String
    var data: List<Timeseries>
}

data class Timeseries(
    val name: String,
    val points: List<Point>
)
