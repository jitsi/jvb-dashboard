package graphs

import highcharts.Chart
import highcharts.ChartOptions
import highcharts.HighchartsReact
import highcharts.Options
import highcharts.Point
import highcharts.SeriesOptions
import highcharts.SeriesOptionsType
import highcharts.Title
import highcharts.XAxis
import highcharts.highcharts
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.ReactElement
import react.dom.div

class Timeline : RComponent<RProps, RState>() {
    private var myRef: ReactElement? = null

    private val chart: Chart
        get() = myRef.asDynamic().chart.unsafeCast<Chart>()

    override fun RBuilder.render() {
        val chartOpts = Options().apply {
            title = Title("somethingz")
            xAxis = XAxis("datetime", visible = false)
            chart = ChartOptions().apply {
                type = "timeline"
            }
            series = arrayOf(
                SeriesOptions().apply {
                    name = "series 1"
                    data = arrayOf(
                        Point(1, 1).apply {
                            name = "name1"
                            description = "Here's some much longer description that would give more detail"
                        },
                        Point(2, 2).apply { name = "name2" },
                        Point(3, 3).apply { name = "name3" },
                    )
                }
            )
        }
        div {
            HighchartsReact {
                attrs.highcharts = highcharts
                attrs.options = chartOpts
                attrs.allowChartUpdate = true
                ref {
                    myRef = it.unsafeCast<ReactElement>()
                }
            }
        }
    }
}
