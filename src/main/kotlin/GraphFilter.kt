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
                    console.log("graph filter receiving")
                    val data = props.channel.receive()
                    console.log("graph filter received")
                    val key = state.graphedKeys.firstOrNull() ?: continue
                    val value = getValue(data.data, key)
                    console.log("graphfilter sending point: ", key, value)
                    graphChannel.send(TimeSeriesPoint(data.timestamp, value))
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
        div {
            child(LiveGraphRef::class) {
                attrs.channel = graphChannel
                attrs.info = GraphInfo("stuff", js("{}"))
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
