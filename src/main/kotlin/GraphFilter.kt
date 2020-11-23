import graphs.GraphControl
import graphs.RemoveSeries
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
import react.setState
import reactselect.AsyncSelect
import reactselect.Components
import reactselect.Option
import reactselect.Select
import styled.css
import styled.styledDiv

class GraphFilter : RComponent<GraphFilterProps, RState>() {
    private var graphChannel = Channel<Any>()
    private var job: Job? = null
    private var graphedKeys: Set<String> = setOf()

    override fun componentWillUnmount() {
        console.log("graph filter ${props.name} unmounted")
        job?.cancel()
        props.channel.cancel()
        graphChannel.close()
    }

    private suspend fun CoroutineScope.handleMessages() {
        try {
            while (isActive) {
                when (val msg = props.channel.receive()) {
                    is EndpointData -> {
                        graphedKeys.forEach { key ->
                            val value = getValue(msg.data, key)
                            graphChannel.send(NewDataMsg(TimeSeriesPoint(msg.timestamp, key, value)))
                        }
                    }
                    is GraphControl -> {
                        graphChannel.send(msg)
                    }
                }
            }
        } catch (c: CancellationException) {
            console.log("graph filter ${props.name} data send loop cancelled")
            throw c
        } catch (t: Throwable) {
            console.log("graph filter ${props.name} loop error: ", t)
        }
    }

    override fun componentDidMount() {
        console.log("graph filter ${props.name} mounted")
        job = GlobalScope.launch {
            console.log("graph filter ${props.name} coro started")
            handleMessages()
        }
    }

    private fun onSelectedKeysChange(newKeys: Set<String>) {
        // New graphs will be added automatically when data with a new key is seen
        // by the graph, but we need to remove graphs explicitly
        val removedKeys = graphedKeys.filterNot(newKeys::contains)
        if (removedKeys.isNotEmpty()) {
            console.log("these graphs were removed: ", removedKeys)
            GlobalScope.launch {
                graphChannel.send(RemoveSeries(removedKeys))
            }
        }
        graphedKeys = newKeys
    }

    override fun RBuilder.render() {
        console.log("graph filter ${props.name} rendering")
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
            AsyncSelect {
                attrs {
                    loadOptions = { inputValue, callback ->
                        val filtered = options
                            .filter { it.value!!.contains(inputValue, ignoreCase = true) }
                            .take(50).toTypedArray()
                        callback(filtered)
                    }
                    isMulti = true
                    onChange = { event ->
                        val keys = event.unsafeCast<Array<dynamic>>().map { evt ->
                            evt.value.unsafeCast<String>()
                        }.toSet()
                        onSelectedKeysChange(keys)
                    }
                }
            }
            div {
                child(LiveGraphRef::class) {
                    attrs.channel = graphChannel
                    attrs.info = GraphInfo(props.name, listOf())
                }
            }
        }
    }
}

external interface GraphFilterProps : RProps {
    var name: String
    var allKeys: List<String>
    var channel: ReceiveChannel<Any>
}
