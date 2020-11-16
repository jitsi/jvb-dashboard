import highcharts.*
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.createRef
import react.useRef

class LiveGraph : RComponent<LiveGraphProps, LiveGraphState>() {
    override fun RBuilder.render() {
        val chartOpts = Options().apply {
            title = Title(props.info.name)
            series = arrayOf(
                SeriesOptions(
                    type = "spline",
                    name = props.info.name,
                    data = arrayOf()
                )
            )
            xAxis = XAxis("datetime")
            chart = ChartOptions().apply {
                events = ChartEventsOptions().apply {
                    load = { event ->
                        println("chart loaded, assigning")
//                        this@LiveGraph.chart = event.target.unsafeCast<Chart>()
                        val chart = event.target.unsafeCast<Chart>()
                        val series = chart.series[0]
                        series.setData(series.data + Point().apply { x = 1; y = 1 })
                        series.setData(series.data + Point().apply { x = 2; y = 2 })
                        series.setData(series.data + Point().apply { x = 3; y = 3 })
//                        // TODO: coroutine?
////                        window.setInterval(
////                            { ->
//                                println("graph ${props.info.name} updating")
//                        series.addPoint(arrayOf(1, 1), redraw = true, shift = false)
//                        series.addPoint(arrayOf(2, 2), redraw = true, shift = false)
//                        series.addPoint(arrayOf(3, 3), redraw = true, shift = false)
//                        console.log("series: ", chart.series)
////                                with(props.info.dataSource()) {
////                                    println("got timestamp: ${this.time}, got value: ${this.value}")
////                                    series.addPoint(arrayOf(this.time, this.value), true, false)
////                                }
////                            },
////                            1000
////                        )
                    }
                }
            }
        }
        HighchartsReact {
            attrs.highcharts = highcharts
            attrs.options = chartOpts
        }
    }
}

external interface LiveGraphProps : RProps {
    var info: GraphInfo
}

data class TimeSeriesPoint(
    val timestamp: Number,
    val value: Number
)

external interface LiveGraphState : RState
