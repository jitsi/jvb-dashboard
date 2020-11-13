package highcharts

data class Title(
    val text: String
)

data class XAxis(
    val type: String,
)

data class Point(
    val x: Number,
    val y: Number
)

data class Series(
    val type: String,
    val name: String,
    val data: Array<Point>
)

data class ChartOptions(
    val title: Title,
    val series: Array<Series>,
    val xAxis: XAxis,
)

external class Highcharts {
    fun chart(renderTo: String, opts: ChartOptions)
}

@JsModule("HighCharts")
@JsNonModule
external val highcharts: Highcharts
