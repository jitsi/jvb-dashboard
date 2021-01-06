package graphs

/**
 * Message handled by a Graph
 */
sealed class GraphMsg

/**
 * A message which controls something about a live graph
 */
sealed class LiveGraphControlMsg : GraphMsg()

/**
 * Adjust how many seconds worth of live data the graph should display
 */
data class LiveZoomAdjustment(val numSeconds: Int) : LiveGraphControlMsg()

/**
 * Remove series whose names are in the given list from the graph
 *
 * (New series can be added automatically by passing a graphs.TimeSeriesPoint with
 * a new key, but series need to be removed explicitly)
 */
data class RemoveSeries(val series: List<String>) : LiveGraphControlMsg()

/**
 * Pass a new [TimeSeriesPoint] to be rendered on the graph
 */
// TODO: rename to new TimeSeriesPointMsg or something
data class NewDataMsg(val timeSeriesPoint: TimeSeriesPoint) : GraphMsg()

data class SetDataMsg(val timeseries: Timeseries) : GraphMsg()
