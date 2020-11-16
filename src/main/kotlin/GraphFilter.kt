import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.css.pct
import kotlinx.css.width
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.div
import reactselect.Option
import reactselect.Select
import styled.css
import styled.styledDiv

class GraphFilter : RComponent<GraphFilterProps, GraphFilterState>() {
    private var graphChannel = Channel<TimeSeriesPoint>()
    override fun GraphFilterState.init() {
        this.graphedKeys = setOf()
        GlobalScope.launch {
            try {
                while (true) {
                    delay(1000)
                    val data = props.channel.receive()
                    state.graphedKeys.forEach { key ->
                        val value = getValue(data.data, key)
                        graphChannel.send(TimeSeriesPoint(data.timestamp, key, value))
                    }
                }
            } catch (t: Throwable) {
                console.log("graph filter loop error: ", t)
            }
        }
    }

    override fun RBuilder.render() {
        val options = props.allKeys.map {
            Option().apply {
                value = it
                label = it
            }
        }.toTypedArray()
        styledDiv {
            css {
                width = 50.pct
            }
            Select {
                attrs.options = options
                attrs.onChange = { event ->
                    // TODO: when a key is removed here, the old points still remain on
                    // the graph: would be better to remove them completely
                    val keys = event.unsafeCast<Array<dynamic>>().map { evt ->
                        evt.value.unsafeCast<String>()
                    }.toSet()
                    state.graphedKeys = keys
                }
                attrs.isMulti = true
            }
            div {
                child(LiveGraphRef::class) {
                    attrs.channel = graphChannel
                    attrs.info = GraphInfo("stuff", state.graphedKeys.map { SeriesInfo(it) })
                }
            }
        }
    }
}

external interface GraphFilterState : RState {
    var graphedKeys: Set<String>
}

external interface GraphFilterProps : RProps {
    var allKeys: List<String>
    var channel: ReceiveChannel<EndpointData>
}
