package graphs

sealed class GraphControl

/**
 * Adjust how many seconds worth of live data the graph should display
 */
data class LiveZoomAdjustment(val numSeconds: Int) : GraphControl()

/**
 * Remove series whose names are in the given list from the graph
 *
 * (New series can be added automatically by passing a TimeSeriesPoint with
 * a new key, but series need to be removed explicitly)
 */
data class RemoveSeries(val series: List<String>) : GraphControl()
