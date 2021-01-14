package graphs

import ChartInfo
import ChartSelection
import ChartZoomButtons
import GraphInfo
import TimelineInfo
import ZoomButtonDesc
import kotlinx.css.paddingLeft
import kotlinx.css.pct
import kotlinx.html.js.onClickFunction
import react.*
import react.dom.button
import react.dom.div
import styled.css
import styled.styledDiv
import kotlin.time.minutes
import kotlin.time.seconds

class ChartCollection : RComponent<ChartCollectionProps, ChartCollectionState>() {
    private var chartSelectors: MutableMap<Int, ChartSelection> = mutableMapOf()
    private var zoomButtons: ChartZoomButtons? = null
    private var nextGraphId: Int = 0

    init {
        state.chartInfos = emptyList()
    }

    fun addGraph() {
        val newGraph = GraphInfo(nextGraphId++)
        setState {
            chartInfos += newGraph
        }
    }

    fun addTimeline() {
        val newTimeline = TimelineInfo(nextGraphId++)
        setState {
            chartInfos += newTimeline
        }
    }

    fun removeChart(chartId: Int) {
        chartSelectors.remove(chartId)
        setState {
            chartInfos = chartInfos.filterNot { it.id == chartId }
        }
    }

    fun addData(data: dynamic) {
        chartSelectors.forEach { (_, selector) ->
            selector.addData(data)
        }
    }

    override fun RBuilder.render() {
        div {
            div {
                button {
                    attrs.text("Add graph")
                    attrs.onClickFunction = { addGraph() }
                }
                button {
                    attrs.text("Add Timeline")
                    attrs.onClickFunction = { addTimeline() }
                }
            }
            if (usingLiveData() && state.chartInfos.isNotEmpty()) {
                div {
                    child(ChartZoomButtons::class) {
                        attrs {
                            onZoomChange = { zoomSeconds ->
                                chartSelectors.values.forEach { it.setZoom(zoomSeconds) }
                            }
                            buttons = listOf(
                                ZoomButtonDesc("1 min", 1.minutes),
                                ZoomButtonDesc("5 mins", 5.minutes),
                                ZoomButtonDesc("All", Int.MAX_VALUE.seconds),
                            )
                        }
                        ref {
                            zoomButtons = it as? ChartZoomButtons
                        }
                    }
                }
            }
            styledDiv {
                css {
                    paddingLeft = 2.pct
                }
                state.chartInfos.forEach { chart ->
                    div {
                        key = chart.id.toString()
                        button {
                            key = "remove-chart-${chart.id}"
                            attrs.value = chart.id.toString()
                            attrs.text("Remove chart")
                            attrs.onClickFunction = { removeChart(chart.id) }
                        }
                        when (chart) {
                            is GraphInfo -> {
                                child(ChartSelection::class) {
                                    key = "chart-selection-${chart.id}"
                                    attrs {
                                        title = "Graph ${chart.id}"
                                        allKeys = props.numericalKeys
                                        graphType = "spline"
                                        data = props.data
                                        // TODO: ideally we'd always pull the start zoom from the value in the
                                        //  buttons, but we may not have a reference to the buttons component
                                        //  yet
                                        startZoomSeconds = zoomButtons?.currZoomSeconds() ?: 60
                                    }
                                    ref {
                                        if (it != null) {
                                            chartSelectors[chart.id] = it as ChartSelection
                                        }
                                    }
                                }
                            }
                            is TimelineInfo -> {
                                child(ChartSelection::class) {
                                    key = "chart-selection-${chart.id}"
                                    attrs {
                                        title = "Timeline ${chart.id}"
                                        allKeys = props.nonNumericalKeys
                                        data = props.data
                                        graphType = "timeline"
                                        // TODO: ideally we'd always pull the start zoom from the value in the
                                        //  buttons, but we may not have a reference to the buttons component
                                        //  yet
                                        startZoomSeconds = zoomButtons?.currZoomSeconds() ?: 60
                                    }
                                    ref {
                                        if (it != null) {
                                            chartSelectors[chart.id] = it as ChartSelection
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

    private fun usingLiveData(): Boolean = props.data == null
}

external interface ChartCollectionState : RState {
    var chartInfos: List<ChartInfo>
}

external interface ChartCollectionProps : RProps {
    var numericalKeys: List<String>
    var nonNumericalKeys: List<String>
    // An optional property to pass pre-existing data (e.g. from a dump file)
    var data: List<dynamic>?
}
