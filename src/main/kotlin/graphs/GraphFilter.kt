package graphs

import EndpointData
import getValue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.css.pct
import kotlinx.css.width
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.div
import reactselect.AsyncSelect
import reactselect.Option
import styled.css
import styled.styledDiv

/**
 * Wraps a [LiveGraph] and presents all possible keys in a dropdown, notifying the
 * [LiveGraph] which series to render.
 */
class GraphFilter : RComponent<GraphFilterProps, RState>() {
    private var graphChannel = Channel<GraphMsg>()
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
                            val value = getValue(msg.data, key).unsafeCast<Number>()
                            graphChannel.send(NewDataMsg(TimeSeriesPoint(msg.timestamp, key, value)))
                        }
                    }
                    is LiveGraphControlMsg -> {
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
            GlobalScope.launch {
                graphChannel.send(RemoveSeries(removedKeys))
            }
        }
        graphedKeys = newKeys
    }

    override fun RBuilder.render() {
        console.log("graph filter ${props.name} rendering")
        val allOptions = props.allKeys.map { Option(it) }.toTypedArray()
        styledDiv {
            css {
                width = 50.pct
            }
            AsyncSelect {
                attrs {
                    loadOptions = { inputValue, callback ->
                        // Since we don't show all keys by default, show them all if the user
                        // searches for "*"
                        if (inputValue == "*") {
                            callback(allOptions)
                        } else {
                            val filteredOptions = allOptions
                                .filter { it.value!!.contains(inputValue, ignoreCase = true) }
                                .take(50)
                                .toTypedArray()
                            callback(filteredOptions)
                        }
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
                child(LiveGraph::class) {
                    attrs {
                        graphTitle = this@GraphFilter.props.name
                        channel = graphChannel
                    }
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
