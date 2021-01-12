import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.css.paddingLeft
import kotlinx.css.paddingTop
import kotlinx.css.pct
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.button
import react.dom.div
import react.dom.h3
import react.setState
import styled.css
import styled.styledDiv
import kotlin.js.Date

class Endpoint : RComponent<EpProps, EpState>() {
    // The Endpoint broadcasts its received data onto this channel for all the graphs to receive
    private val broadcastChannel = BroadcastChannel<Any>(5)
    private var job: Job? = null
    private var nextGraphId: Int = 0
    private var graphSelectors: MutableMap<Int, GraphSelection> = mutableMapOf()

    init {
        state.chartInfos = listOf()
        state.numericalKeys = listOf()
        state.statsId = null
    }

    override fun componentDidMount() {
//        job = GlobalScope.launch { handleMessages() }
        if (state.numericalKeys.isEmpty() && props.data != undefined) {
            extractKeys(props.data!!.first())
        }
        GlobalScope.launch {
            repeat(20) {
                val now = Date.now()
                val data = jsObject {
                    key1 = 1 * it
                    key2 = 2 * it
                    key3 = it % 2 == 0
                    timestamp = now
                }
                addData(data)
                delay(2000)
            }
        }
    }

    private fun extractKeys(data: dynamic) {
        console.log("Extracting keys from ", data)
        setState {
            numericalKeys = getAllKeysWithValuesThat(data) { it is Number }
            nonNumericalKeys = getAllKeysWithValuesThat(data) { it !is Number }
        }
    }

    override fun componentWillUnmount() {
        job?.cancel("Unmounting")
        broadcastChannel.close()
    }

    private suspend fun CoroutineScope.handleMessages() {
        try {
            while (isActive) {
                val epData = props.channel.receive()
                if (state.numericalKeys.isEmpty()) {
                    // Build the list of available keys the first time we get data.
                    // NOTE: This means keys that didn't show up later won't be displayed,
                    // if we need to cover that then we can add any missing ones each time
                    val allKeys = getAllKeys(epData.data).filter { key ->
                        isNumber(getValue(epData.data, key))
                    }
                    console.log("Endpoint ${props.id}: Got all (numerical) keys: ", allKeys)
                    setState {
                        this.numericalKeys = allKeys
                    }
                }
                // Set the stats ID if it isn't already set
                if (state.statsId == null) {
                    setState {
                        statsId = epData.data.statsId.unsafeCast<String>()
                    }
                }
                // Pass the epData down to all graphs
                broadcastChannel.send(epData)
            }
        } catch (c: CancellationException) {
            console.log("endpoint data send loop cancelled: ${c.message}")
            throw c
        } catch (t: Throwable) {
            console.log("endpoint data send loop error: ", t)
        }
    }

    private fun addGraph() {
        val newGraph = GraphInfo(nextGraphId++, broadcastChannel.openSubscription())
        setState {
            chartInfos += newGraph
        }
    }

    private fun addTimeline() {
        val newTimeline = TimelineInfo(nextGraphId++, broadcastChannel.openSubscription())
        setState {
            chartInfos += newTimeline
        }
    }

    private fun removeChart(chartId: Int) {
        setState {
            chartInfos = chartInfos.filterNot { it.id == chartId }
        }
    }

    fun addData(data: dynamic) {
        if (usingLiveData()) {
            console.log("adding data ", data)
            if (state.numericalKeys.isEmpty()) {
                extractKeys(data)
            }
            console.log("sending data to selectors")
            graphSelectors.forEach { (_, selector) ->
                selector.addData(data)
            }
        }
    }

    override fun RBuilder.render() {
        console.log("Endpoint ${props.id}: rendering")
        div {
            h3 {
                if (!state.statsId.isNullOrEmpty()) {
                    +"Endpoint ${props.id} (${state.statsId})   "
                } else {
                    +"Endpoint ${props.id}   "
                }
            }
            if (usingLiveData()) {
                div {
                    child(FeatureToggle::class) {
                        attrs {
                            featureName = "PCAP dump"
                            url = "${props.baseRestApiUrl}/features/endpoint/${props.confId}/${props.id}/pcap-dump"
                        }
                    }
                }
            }
            div {
                button {
                    attrs.text("Add graph")
                    attrs.onClickFunction = { _ ->
                        addGraph()
                    }
                }
                button {
                    attrs.text("Add Timeline")
                    attrs.onClickFunction = { _ ->
                        addTimeline()
                    }
                }
                if (usingLiveData() && state.chartInfos.isNotEmpty()) {
                    button {
                        attrs {
                            text("1 min")
                            onClickFunction = {}
                        }
                    }
                    button {
                        attrs {
                            text("5 mins")
                            onClickFunction = {}
                        }
                    }
                    button {
                        attrs {
                            text("All")
                            onClickFunction = {}
                        }
                    }
                }
            }
            styledDiv {
                css {
                    paddingLeft = 2.pct
                    paddingTop = 2.pct
                }
                state.chartInfos.forEach { chart ->
                    console.log("rendering chart ${chart.id}")
                    div {
                        key = chart.id.toString()
                        button {
                            key = "remove-graph-${chart.id}"
                            attrs.value = chart.id.toString()
                            attrs.text("Remove chart")
                            attrs.onClickFunction = { _ ->
                                removeChart(chart.id)
                            }
                        }
                        when (chart) {
                            is GraphInfo -> {
                                child(GraphSelection::class) {
                                    key = "graph-filter-${chart.id}"
                                    attrs {
                                        title = "Graph ${chart.id}"
                                        allKeys = state.numericalKeys
                                        graphType = "spline"
//                                    channel = chart.channel
                                        data = props.data
                                    }
                                    ref {
                                        if (it != null) {
                                            graphSelectors[chart.id] = it as GraphSelection
                                        }
                                    }
                                }
                            }
                            is TimelineInfo -> {
                                child(GraphSelection::class) {
                                    key = "graph-filter-${chart.id}"
                                    attrs {
                                        title = "Graph ${chart.id}"
                                        allKeys = state.nonNumericalKeys
//                                    channel = chart.channel
                                        data = props.data
                                        graphType = "timeline"
                                    }
                                    ref {
                                        if (it != null) {
                                            graphSelectors[chart.id] = it as GraphSelection
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun usingLiveData(): Boolean = props.baseRestApiUrl != null
}

external interface EpProps : RProps {
    var confId: String
    var id: String
    var baseRestApiUrl: String?
    var channel: ReceiveChannel<EndpointData>
    // An optional property to pass pre-existing data (e.g. from a dump file)
    var data: List<dynamic>?
}

data class EndpointData(
    val timestamp: Number,
    val data: dynamic
)

// Endpoints don't retrieve their own data, the conference makes a single
// request and updates the props of the ep components
external interface EpState : RState {
    var numericalKeys: List<String>
    var nonNumericalKeys: List<String>
    var chartInfos: List<ChartInfo>
    var statsId: String?
}

sealed class ChartInfo(
    val id: Int,
    val channel: ReceiveChannel<Any>
)

class GraphInfo(
    id: Int,
    channel: ReceiveChannel<Any>
) : ChartInfo(id, channel)

class TimelineInfo(
    id: Int,
    channel: ReceiveChannel<Any>
) : ChartInfo(id, channel)
