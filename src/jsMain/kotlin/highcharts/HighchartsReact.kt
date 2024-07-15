@file:JsModule("highcharts-react-official")
@file:JsNonModule

package highcharts

import react.ComponentClass
import react.PropsWithChildren

@JsName("default")
external val HighchartsReact: ComponentClass<HighchartsReactProps>

external interface HighchartsReactProps : PropsWithChildren {
    var highcharts: Highcharts
//    var options: dynamic
    var options: Options
    var allowChartUpdate: Boolean?
}
