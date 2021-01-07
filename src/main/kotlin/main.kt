import highcharts.TimelineSeries
import highcharts.highcharts
import kotlinx.browser.document
import react.dom.render

fun main() {
    TimelineSeries(highcharts)
    render(document.getElementById("root")) {
        child(App::class) {}
    }
}
