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

data class Events(
    val load: ChartOptions.() -> Unit
)

data class ChartSettings(
    val events: Events
)

val EmptyChartSettings = ChartSettings(
    events = Events(
        load = {}
    )
)

data class ChartOptions(
    val title: Title,
    val series: Array<Series>,
    val xAxis: XAxis,
    val chart: ChartSettings = EmptyChartSettings
)

external class Highcharts {
    fun chart(renderTo: String, opts: ChartOptions)
}

@JsModule("HighCharts")
@JsNonModule
external val highcharts: Highcharts
