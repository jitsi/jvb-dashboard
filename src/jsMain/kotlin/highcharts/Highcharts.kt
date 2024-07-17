package highcharts

import jsObject
import org.w3c.dom.events.Event

data class Title(
    @JsName("text")
    val text: String
)

data class XAxis(
    @JsName("type")
    val type: String, /* "category" | "datetime" | "linear" | "logarithmic" | "treegrid" */
    @JsName("visible")
    val visible: Boolean = true,
    @JsName("gridLineWidth")
    val gridLineWidth: Int = 1
)

external interface Point {
    var name: dynamic /* Number? | String? */
        get() = definedExternally
        set(value) = definedExternally
    var description: String?
        get() = definedExternally
        set(value) = definedExternally
    @JsName("x")
    var x: Number
    @JsName("y")
    var y: Number
}

external interface AnimationOptionsObjectPartial {
    var complete: Function<*>?
        get() = definedExternally
        set(value) = definedExternally
    var defer: Number?
        get() = definedExternally
        set(value) = definedExternally
    var duration: Number?
        get() = definedExternally
        set(value) = definedExternally
    var easing: dynamic /* String? | Function<*>? */
        get() = definedExternally
        set(value) = definedExternally
    var step: Function<*>?
        get() = definedExternally
        set(value) = definedExternally
}

fun Point(x: Number, y: Number): Point = jsObject { this.x = x; this.y = y }.unsafeCast<Point>()

fun Event(x: Number, name: String): Point = jsObject { this.x = x; this.name = name }.unsafeCast<Point>()

external interface SeriesOptionsType {
    var type: String?
        get() = definedExternally
        set(value) = definedExternally
    var name: String?
        get() = definedExternally
        set(value) = definedExternally
    var data: Array<Point>?
        get() = definedExternally
        set(value) = definedExternally
}

fun SeriesOptions(): SeriesOptionsType = js("{}")

external class Series {
    var chart: Chart
    var type: String
    var name: String
    var data: Array<Point>
    var xAxis: Axis

    fun addPoint(point: Point, redraw: Boolean = definedExternally, shift: Boolean = definedExternally)
    fun addPoint(point: Point, redraw: Boolean = definedExternally, shift: Boolean = definedExternally, animation: AnimationOptionsObjectPartial = definedExternally, withEvent: Boolean = definedExternally)
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

external interface PointMarkerOptionsObject {
    var enabled: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var enabledThreshold: Number?
        get() = definedExternally
        set(value) = definedExternally
}

fun PointMarkerOptionsObject(): PointMarkerOptionsObject = js("{}")

external interface PlotLineOptions {
    var marker: PointMarkerOptionsObject?
        get() = definedExternally
        set(value) = definedExternally
}

fun PlotLineOptions(): PlotLineOptions = js("{}")

external interface PlotSeriesOptions {
    var marker: PointMarkerOptionsObject?
        get() = definedExternally
        set(value) = definedExternally
    var step: String?
        get() = definedExternally
        set(value) = definedExternally
    var boostThreshold: Number?
        get() = definedExternally
        set(value) = definedExternally
    var turboThreshold: Number?
        get() = definedExternally
        set(value) = definedExternally
}

fun PlotSeriesOptions(): PlotSeriesOptions = js("{}")

external interface PlotOptions {
    var line: PlotLineOptions?
        get() = definedExternally
        set(value) = definedExternally
    var series: PlotSeriesOptions?
        get() = definedExternally
        set(value) = definedExternally
}

fun PlotOptions(): PlotOptions = js("{}")

external interface Options {
    var title: Title?
        get() = definedExternally
        set(value) = definedExternally
    var xAxis: XAxis?
        get() = definedExternally
        set(value) = definedExternally
    var series: Array<SeriesOptionsType>?
        get() = definedExternally
        set(value) = definedExternally
    var chart: ChartOptions?
        get() = definedExternally
        set(value) = definedExternally
    var plotOptions: PlotOptions?
        get() = definedExternally
        set(value) = definedExternally
}

fun Options(): Options = js("{}")

external interface ChartOptions {
    var events: ChartEventsOptions?
        get() = definedExternally
        set(value) = definedExternally
    var type: String?
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
    open fun addSeries(options: SeriesOptionsType, redraw: Boolean = definedExternally, animation: Boolean = definedExternally): Series
    open fun update(options: Options, redraw: Boolean = definedExternally, oneToOne: Boolean = definedExternally, animation: Boolean = definedExternally)
}

external class Highcharts {
    fun chart(renderTo: String, opts: Options): Chart
}

@JsModule("highcharts")
@JsNonModule
external val highcharts: Highcharts
