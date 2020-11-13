import highcharts
import highcharts.*
import highcharts.ChartSettings
import kotlinx.browser.window
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState

class LiveGraph : RComponent<LiveGraphProps, RState>() {
    override fun RBuilder.render() {
        val chartOpts = ChartOptions(
            title = Title(props.info.name),
            series = arrayOf(
                Series(
                    type = "spline",
                    name = props.info.name,
                    data = arrayOf()
                )
            ),
            xAxis = XAxis("datetime"),
            chart = ChartSettings(
                events = Events(
                    load = {
                        //TODO: coroutine?
                        window.setInterval({ ->
                            val series = this.series[0]
                            series.

                        }, 1000)
                    }
                )
            )
        )
    }
}

external interface LiveGraphProps : RProps {
    var info: GraphInfo
}
