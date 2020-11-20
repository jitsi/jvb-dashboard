import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import react.dom.h1
import react.dom.render
import kotlin.js.Date

fun main() {
    val channel = Channel<TimeSeriesPoint>()
    MainScope().launch {
        var i = 0
        while (isActive) {
            val now = Date()
            channel.send(TimeSeriesPoint(now.getTime(), "key", i++))
            delay(1000)
        }
    }

    render(document.getElementById("root")) {
//        child(App::class) {}
        child(LiveGraphRef::class) {
            attrs {
                this.channel = channel
                info = GraphInfo("name", listOf(SeriesInfo("key")))
            }
        }
    }
}
