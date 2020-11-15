package highcharts

import org.w3c.dom.events.Event

data class Title(
    val text: String
)

data class XAxis(
    val type: String,
)

//data class Point(
//    val x: Number,
//    val y: Number
//)

external interface Point {
    var x: Number
    var y: Number
}

fun Point(): Point = js("{}")

data class SeriesOptions(
    val type: String,
    val name: String,
    val data: Array<Point>
)

external class Series {
    var chart: Chart
    var type: String
    var name: String
    var data: Array<Point>

    fun addPoint(point: Point, redraw: Boolean, shift: Boolean)
    fun setData(data: Array<dynamic /* Number? | String? | PointOptionsObject? | Array<dynamic /* Number? | String? */>? */>, redraw: Boolean = definedExternally, animation: Boolean = definedExternally, updatePoints: Boolean = definedExternally)
}

// Docs suggest that this callback is called with a context of 'Chart', but in practice
// i can't seem to define this function in a way that works there (can't do an extension
// func for an external declaration)
typealias ChartLoadCallbackFunction = (event: Event) -> Unit // , event: Event) -> Unit
//typealias ChartLoadCallbackFunction = Chart.(event: Event) -> Unit

external interface ChartEventsOptions {
//    var addSeries: dynamic
//        get() = definedExternally
//        set(value) = definedExternally
//    var afterPrint: dynamic
//        get() = definedExternally
//        set(value) = definedExternally
//    var beforePrint: dynamic
//        get() = definedExternally
//        set(value) = definedExternally
//    var click: dynamic
//        get() = definedExternally
//        set(value) = definedExternally
//    var drilldown: dynamic
//        get() = definedExternally
//        set(value) = definedExternally
//    var drillup: dynamic
//        get() = definedExternally
//        set(value) = definedExternally
//    var drillupall: dynamic
//        get() = definedExternally
//        set(value) = definedExternally
//    var exportData: dynamic
//        get() = definedExternally
//        set(value) = definedExternally
    var load: ChartLoadCallbackFunction?
        get() = definedExternally
        set(value) = definedExternally
//    var redraw: dynamic
//        get() = definedExternally
//        set(value) = definedExternally
//    var render: dynamic
//        get() = definedExternally
//        set(value) = definedExternally
//    var selection: dynamic
//        get() = definedExternally
//        set(value) = definedExternally
}

fun ChartEventsOptions(): ChartEventsOptions = js("{}")

external interface Options {
    var title: Title?
        get() = definedExternally
        set(value) = definedExternally
    var xAxis: XAxis?
        get() = definedExternally
        set(value) = definedExternally
    var series: Array<SeriesOptions>?
        get() = definedExternally
        set(value) = definedExternally
    var chart: ChartOptions?
        get() = definedExternally
        set(value) = definedExternally
}

fun Options(): Options = js("{}")

external interface ChartOptions {
    var events: ChartEventsOptions?
        get() = definedExternally
        set(value) = definedExternally
}

fun ChartOptions(): ChartOptions = js("{}")

open external class Chart {
    var series: Array<Series>
        get() = definedExternally
        set(value) = definedExternally
    open fun redraw(animation: Boolean = definedExternally)
    open fun zoomOut()
}

external class Highcharts {
    fun chart(renderTo: String, opts: Options): Chart
}

// fun foo() {
//    val options = Options(
//        title = Title("title"),
//        series = arrayOf(
//            SeriesOptions(
//                type = "spline",
//                name = "foo",
//                data = arrayOf(
//                    Point(1, 1),
//                    Point(2, 2),
//                    Point(3, 3),
//                )
//            )
//        ),
//        xAxis = XAxis("datetime"),
//        chart = ChartOptions(
//            events = ChartEventsOptions(
//                load = { self ->
//                    val series = self.series[0]
//                    series.addPoint(Point(4, 4), true)
//                }
//            )
//        )
//    )
// }

@JsModule("HighCharts")
@JsNonModule
external val highcharts: Highcharts

