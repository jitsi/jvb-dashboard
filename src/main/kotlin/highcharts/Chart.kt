//package highcharts
//
//import kotlinx.html.id
//import react.RBuilder
//import react.RComponent
//import react.RProps
//import react.RState
//import react.dom.div
//
//class Chart : RComponent<ChartProps, RState>() {
//    override fun RBuilder.render() {
//        div {
//            attrs {
//                id = "blah"
//            }
//        }
//        for (chartOpts in props.chartOptions) {
//            highcharts.chart("blah", chartOpts)
//        }
//    }
//}
//
//external interface ChartProps : RProps {
//    var chartOptions: List<ChartOptions>
//}
