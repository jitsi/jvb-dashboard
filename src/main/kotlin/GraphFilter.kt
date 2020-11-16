import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onInputFunction
import kotlinx.html.js.onSelectFunction
import kotlinx.html.onInput
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.*

class GraphFilter : RComponent<GraphFilterProps, GraphFilterState>() {
    private var graphChannel = Channel<TimeSeriesPoint>()
    override fun GraphFilterState.init() {
        this.currKeys = listOf()
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
        input(type = InputType.text) {
            attrs.onInputFunction = { event ->
                val input = event.target.asDynamic().value.unsafeCast<String>()
                state.currKeys = props.allKeys.filter { input.isEmpty() || it.contains(input, ignoreCase = true) }
            }
        }
        select {
            state.currKeys.forEach {
                option {
                    attrs.value = it
                    +it
                }
            }
            attrs.onChangeFunction = { event ->
                println("graph ${event.target.asDynamic().value} changed!")
                state.graphedKeys += event.target.asDynamic().value.unsafeCast<String>()
            }
        }
        val infos = state.graphedKeys.map { SeriesInfo(it) }.toTypedArray()
        div {
            child(LiveGraphRef::class) {
                attrs.channel = graphChannel
                attrs.info = GraphInfo("stuff", state.graphedKeys.map { SeriesInfo(it) })
            }
        }
    }
}

external interface GraphFilterState : RState {
    var currKeys: List<String>
    var series: List<String>
    var graphedKeys: Set<String>
}

external interface GraphFilterProps : RProps {
    var allKeys: List<String>
    var channel: ReceiveChannel<EndpointData>
}
