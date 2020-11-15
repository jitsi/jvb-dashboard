import highcharts.*
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.w3c.dom.Element
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.ReactElement
import react.createRef
import react.dom.div
import react.ref
import react.useRef

class LiveGraphRef : RComponent<LiveGraphRefProps, LiveGraphRefState>() {
    private var chart: Chart? = null
    private var mounted = false
    override fun LiveGraphRefState.init() {
        var num = 1
        MainScope().launch {
            while (true) {
                val point = props.channel.receive()
                println("got point $point")
                val chart = state.myRef.asDynamic().chart.unsafeCast<Chart>()
                chart.series[0].addPoint(Point().apply { x = point.timestamp; y = point.value }, true, false)
//                if (mounted) {
//                    val chart = state.myRef.asDynamic().chart.unsafeCast<Chart>()
//                    chart.series[0].addPoint(Point().apply { x = num; y = num }, true, false)
//                    num++
//                }
//                delay(1000)
            }
        }

    }

    override fun shouldComponentUpdate(nextProps: LiveGraphRefProps, nextState: LiveGraphRefState): Boolean {
        return false
    }

    override fun componentWillMount() {
        console.log("will mount")
    }

    override fun componentWillUnmount() {
        console.log("will unmount")
    }

    override fun componentWillUpdate(nextProps: LiveGraphRefProps, nextState: LiveGraphRefState) {
        console.log("component updating: nextProps:", nextProps, " nextState: ", nextState)
    }

    override fun componentDidMount() {
        console.log("ref", state.myRef)
        console.log("chart: ", state.myRef.asDynamic().chart)
        mounted = true

    }

    override fun RBuilder.render() {
        console.log("rendering")
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
//                events = ChartEventsOptions().apply {
//                    load = { event ->
//                        println("chart loaded, assigning")
////                        this@LiveGraph.chart = event.target.unsafeCast<Chart>()
//                        val chart = event.target.unsafeCast<Chart>()
//                        val series = chart.series[0]
//                        series.setData(series.data + Point().apply { x = 1; y = 1})
//                        series.setData(series.data + Point().apply { x = 2; y = 2})
//                        series.setData(series.data + Point().apply { x = 3; y = 3})
////                        // TODO: coroutine?
//////                        window.setInterval(
//////                            { ->
////                                println("graph ${props.info.name} updating")
////                        series.addPoint(arrayOf(1, 1), redraw = true, shift = false)
////                        series.addPoint(arrayOf(2, 2), redraw = true, shift = false)
////                        series.addPoint(arrayOf(3, 3), redraw = true, shift = false)
////                        console.log("series: ", chart.series)
//////                                with(props.info.dataSource()) {
//////                                    println("got timestamp: ${this.time}, got value: ${this.value}")
//////                                    series.addPoint(arrayOf(this.time, this.value), true, false)
//////                                }
//////                            },
//////                            1000
//////                        )
//                    }
//                }
            }
        }
        div {
            HighchartsReact {
                attrs.highcharts = highcharts
                attrs.options = chartOpts
                attrs.allowChartUpdate = true
//                attrs.ref = it
                ref {
                    state.myRef = it
                }
            }
        }
    }
}

external interface LiveGraphRefProps : RProps {
    var info: GraphInfo
    var channel: ReceiveChannel<TimeSeriesPoint>
}

external interface LiveGraphRefState : RState {
    var myRef: ReactElement?
}
