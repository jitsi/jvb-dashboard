import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ReceiveChannel
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

class Endpoint : RComponent<EpProps, EpState>() {
    // The Endpoint broadcasts its received data onto this channel for all the graphs to receive
    private var job: Job? = null
    private var nextGraphId: Int = 0
    private var graphSelectors: MutableMap<Int, GraphSelection> = mutableMapOf()

    init {
        state.chartInfos = listOf()
        state.numericalKeys = listOf()
        state.statsId = null
    }

    override fun componentDidMount() {
        if (state.numericalKeys.isEmpty() && props.data != undefined) {
            extractKeys(props.data!!.first())
        }
    }

    private fun extractKeys(data: dynamic) {
        console.log("Extracting keys from ", data)
        setState {
            numericalKeys = getAllKeysWithValuesThat(data) { it is Number }
            nonNumericalKeys = getAllKeysWithValuesThat(data) { it !is Number }
        }
    }

    override fun componentWillUnmount() {}

    private fun addGraph() {
        val newGraph = GraphInfo(nextGraphId++)
        setState {
            chartInfos += newGraph
        }
    }

    private fun addTimeline() {
        val newTimeline = TimelineInfo(nextGraphId++)
        setState {
            chartInfos += newTimeline
        }
    }

    private fun removeChart(chartId: Int) {
        graphSelectors.remove(chartId)
        setState {
            chartInfos = chartInfos.filterNot { it.id == chartId }
        }
    }

    fun addData(data: dynamic) {
        if (usingLiveData()) {
            if (state.numericalKeys.isEmpty()) {
                extractKeys(data)
            }
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
                            onClickFunction = {
                                graphSelectors.values.forEach { it.setZoom(60) }
                            }
                        }
                    }
                    button {
                        attrs {
                            text("5 mins")
                            onClickFunction = {
                                graphSelectors.values.forEach { it.setZoom(300) }
                            }
                        }
                    }
                    button {
                        attrs {
                            text("All")
                            onClickFunction = {
                                graphSelectors.values.forEach { it.setZoom(Int.MAX_VALUE) }
                            }
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
    // An optional property to pass pre-existing data (e.g. from a dump file)
    var data: List<dynamic>?
}

// Endpoints don't retrieve their own data, the conference makes a single
// request and updates the props of the ep components
external interface EpState : RState {
    var numericalKeys: List<String>
    var nonNumericalKeys: List<String>
    var chartInfos: List<ChartInfo>
    var statsId: String?
}

sealed class ChartInfo(val id: Int)

class GraphInfo(id: Int) : ChartInfo(id)

class TimelineInfo(id: Int) : ChartInfo(id)
