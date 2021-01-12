import graphs.Graph
import highcharts.Point
import highcharts.TimeseriesPoint
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState

// Defines a selector and the graph itself, and acts as the 'go between' between the two
class GraphSelection : RComponent<GraphSelectionProps, RState>() {
    private var currentlyGraphedKeys = listOf<String>()
    private var selector: Selector? = null
    private var graph: Graph? = null

    override fun componentDidMount() {}

    private fun selectedKeysChanged(newKeys: List<String>) {
        val removedKeys = currentlyGraphedKeys.filterNot(newKeys::contains)
        val addedKeys = newKeys.filterNot(currentlyGraphedKeys::contains)
        currentlyGraphedKeys = newKeys
        removedKeys.forEach { graph?.removeSeries(it) }
        // If we're using stored data, then we'll add the timeseries for the new keys
        // immediately.
        ifHaveStoredData { storedData ->
            if (addedKeys.isEmpty()) return@ifHaveStoredData
            val allTimeseries = mutableMapOf<String, MutableList<Point>>()
            storedData.forEach { dataEntry ->
                addedKeys.forEach { addedKey ->
                    val timeseries = allTimeseries.getOrPut(addedKey) { mutableListOf() }
                    // As of now, the 'x' value is always the timestamp, and we expect the
                    // entry to have a 'timestamp' field.
                    val timestamp = dataEntry.timestamp.unsafeCast<Number>()
                    val value = getValue(dataEntry, addedKey)
                    if (props.graphType.equals("timeline", ignoreCase = true)) {
                        timeseries.add(TimeseriesPoint(timestamp, value.toString()))
                    } else {
                        timeseries.add(Point(timestamp, value as Number))
                    }
                }
            }
            allTimeseries.forEach { (seriesName, timeseries) -> graph?.addTimeseries(seriesName, timeseries) }
        }
    }

    override fun RBuilder.render() {
        child(Selector::class) {
            attrs {
                onSelectedKeysChange = this@GraphSelection::selectedKeysChanged
                allKeys = props.allKeys
            }
            ref {
                selector = it.unsafeCast<Selector>()
            }
        }
        child(Graph::class) {
            attrs {
                title = props.title
                enableZoom = haveStoredData()
                graphType = props.graphType ?: "line"
            }
            ref {
                graph = it.unsafeCast<Graph>()
            }
        }
    }

    private fun ifHaveStoredData(block: (List<dynamic>) -> Unit) {
        if (haveStoredData()) {
            block(props.data!!)
        }
    }

    private fun haveStoredData(): Boolean = props.data != null

    // Leaving this here, but since it doesn't propagate these values to props, it's a bit cumbersome to use.
    // See https://github.com/JetBrains/kotlin-wrappers/issues/385
//    companion object : RStatics<GraphSelectionProps, RState, GraphSelection, Nothing>(GraphSelection::class) {
//        init {
//            defaultProps = GraphSelectionProps().apply {
//                allKeys = emptyList()
//                data = emptyList()
//            }
//        }
//    }
}

external interface GraphSelectionProps : RProps {
    var title: String?
    var allKeys: List<String>?
    // An optional property which can contain stored data to be graphed
    var data: List<dynamic>?
    var graphType: String?
}

// See comment above about defaultProps
// fun GraphSelectionProps(): GraphSelectionProps = js("{}").unsafeCast<GraphSelectionProps>()
