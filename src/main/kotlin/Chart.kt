@file:JsModule("highcharts-react-official")
@file:JsNonModule

import highcharts.ChartOptions
import highcharts.Highcharts
import react.RClass
import react.RProps

@JsName("default")
external val HighchartsReact: RClass<HighchartsReactProps>

external interface HighchartsReactProps : RProps {
    var highcharts: Highcharts
    var options: ChartOptions
}
