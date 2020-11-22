package highcharts

import jsObject
import org.w3c.dom.events.Event

data class Title(
    val text: String
)

data class XAxis(
    val type: String, /* "category" | "datetime" | "linear" | "logarithmic" | "treegrid" */
)

external interface Point {
    var x: Number
    var y: Number
}

fun Point(x: Number, y: Number): Point = jsObject { this.x = x; this.y = y }.unsafeCast<Point>()

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
    var xAxis: Axis

    fun addPoint(point: Point, redraw: Boolean = definedExternally, shift: Boolean = definedExternally)
    fun removePoint(i: Number, redraw: Boolean = definedExternally, animation: Boolean = definedExternally)
    fun setData(data: Array<dynamic /* Number? | String? | PointOptionsObject? | Array<dynamic /* Number? | String? */>? */>, redraw: Boolean = definedExternally, animation: Boolean = definedExternally, updatePoints: Boolean = definedExternally)
    fun remove(redraw: Boolean = definedExternally, animation: Boolean = definedExternally, withEvent: Boolean = definedExternally)
}

external class Axis {
    var max: Number?
    var min: Number?
    fun setExtremes(newMin: Number = definedExternally, newMax: Number = definedExternally, redraw: Boolean = definedExternally, animation: Boolean = definedExternally, eventArguments: Any = definedExternally)
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
    var zoomType: String? /* "x" | "xy" | "y" */
        get() = definedExternally
        set(value) = definedExternally
}

fun ChartOptions(): ChartOptions = js("{}")

open external class Chart {
    var series: Array<Series>
        get() = definedExternally
        set(value) = definedExternally
    var xAxis: Array<Axis>
        get() = definedExternally
        set(value) = definedExternally
    open fun get(id: String): dynamic /* Axis? | Point? | Series? */
    open fun redraw(animation: Boolean = definedExternally)
    open fun addSeries(options: SeriesOptions, redraw: Boolean = definedExternally, animation: Boolean = definedExternally): Series
}

external class Highcharts {
    fun chart(renderTo: String, opts: Options): Chart
}

@JsModule("HighCharts")
@JsNonModule
external val highcharts: Highcharts
