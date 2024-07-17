import graphs.Chart
import highcharts.Event
import highcharts.Point
import react.RBuilder
import react.RComponent
import react.PropsWithChildren
import react.State
import react.RefCallback

// Defines a selector and the graph itself, and acts as the 'go between' between the two
class ChartSelection : RComponent<GraphSelectionProps, State>() {
    private var currentlyGraphedKeys = listOf<String>()
    private var selector: Selector? = null
    private var chart: Chart? = null

    override fun componentDidMount() {}

    private fun selectedKeysChanged(newKeys: List<String>) {
        val removedKeys = currentlyGraphedKeys.filterNot(newKeys::contains)
        val addedKeys = newKeys.filterNot(currentlyGraphedKeys::contains)
        currentlyGraphedKeys = newKeys
        removedKeys.forEach { chart?.removeSeries(it) }
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
                    if (value != undefined) {
                        // Not all chunks will have an entry for every key, since duplicates are filtered out)
                        if (props.graphType.equals("timeline", ignoreCase = true)) {
                            timeseries.add(Event(timestamp, value.toString()))
                        } else {
                            timeseries.add(Point(timestamp, value as Number))
                        }
                    }
                }
            }
            allTimeseries.forEach { (seriesName, timeseries) -> chart?.addTimeseries(seriesName, timeseries) }
        }
    }

    override fun RBuilder.render() {
        console.log("graph selection rendering")
        child(Selector::class) {
            attrs {
                onSelectedKeysChange = this@ChartSelection::selectedKeysChanged
                allKeys = props.allKeys
            }
            ref = RefCallback<Selector> {
                selector = it
            }
        }
        child(Chart::class) {
            attrs {
                title = props.title
                enableZoom = haveStoredData()
                graphType = props.graphType ?: "line"
                startZoomSeconds = props.startZoomSeconds
            }
            ref = RefCallback<Chart> {
                chart = it
            }
        }
    }

    fun setZoom(zoomSeconds: Int) {
        chart?.setZoom(zoomSeconds)
    }

    fun addData(data: dynamic) {
        val timestamp = data.timestamp as Number
        currentlyGraphedKeys.forEach { key ->
            val value = getValue(data, key)
            if (value != undefined) {
                if (value is Number) {
                    chart?.addPoint(key, Point(timestamp, value))
                } else {
                    chart?.addPoint(key, Event(timestamp, value.toString()))
                }
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
//    companion object : RStatics<GraphSelectionProps, State, GraphSelection, Nothing>(GraphSelection::class) {
//        init {
//            defaultProps = GraphSelectionProps().apply {
//                allKeys = emptyList()
//                data = emptyList()
//            }
//        }
//    }
}

external interface GraphSelectionProps : PropsWithChildren {
    var title: String?
    var allKeys: List<String>?
    // An optional property which can contain stored data to be graphed
    var data: List<dynamic>?
    var graphType: String?
    var startZoomSeconds: Int?
}

// See comment above about defaultProps
// fun GraphSelectionProps(): GraphSelectionProps = js("{}").unsafeCast<GraphSelectionProps>()
