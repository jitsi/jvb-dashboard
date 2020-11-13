@file:JsModule("highcharts-react-official")
@file:JsNonModule

package highcharts

import react.RClass
import react.RProps

@JsName("default")
external val HighchartsReact: RClass<HighchartsReactProps>

external interface HighchartsReactProps : RProps {
    var highcharts: Highcharts
    var options: ChartOptions
}
