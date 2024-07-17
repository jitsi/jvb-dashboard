import highcharts.TimelineSeries
import highcharts.highcharts
import react.create
import react.dom.client.createRoot
import web.dom.document

fun main() {
    TimelineSeries(highcharts)
    val root = createRoot(document.getElementById("root")!!)
    root.render(App.create())
}
