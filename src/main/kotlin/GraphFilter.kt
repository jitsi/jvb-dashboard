import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ReceiveChannel
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
    private var job: Job? = null
    override fun GraphFilterState.init() {
        this.graphedKeys = setOf()
    }

    override fun componentWillUnmount() {
        console.log("graph filter ${props.name} unmounted")
        job?.cancel()
        props.channel.cancel()
        graphChannel.close()
    }

    override fun componentDidMount() {
        console.log("graph filter ${props.name} mounted")
        job = GlobalScope.launch {
            console.log("graph filter ${props.name} coro started")
            try {
                while (isActive) {
                    val data = props.channel.receive()
                    console.log("graph filter ${props.name} got data")
                    state.graphedKeys.forEach { key ->
                        val value = getValue(data.data, key)
                        graphChannel.send(TimeSeriesPoint(data.timestamp, key, value))
                    }
                    delay(1000)
                }
            } catch (c: CancellationException) {
                console.log("graph filter ${props.name} data send loop cancelled")
                throw c
            } catch (t: Throwable) {
                console.log("graph filter ${props.name} loop error: ", t)
            }
        }
    }

    override fun RBuilder.render() {
        console.log("graph filter ${props.name} rendering")
        val options = props.allKeys.map {
            Option().apply {
                value = it
                label = it
            }
        }.take(50).toTypedArray()
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
                    attrs.info = GraphInfo(props.name, state.graphedKeys.map { SeriesInfo(it) })
                }
            }
        }
    }
}

external interface GraphFilterState : RState {
    var graphedKeys: Set<String>
}

external interface GraphFilterProps : RProps {
    var name: String
    var allKeys: List<String>
    var channel: ReceiveChannel<EndpointData>
}
