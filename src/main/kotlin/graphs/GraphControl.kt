package graphs

sealed class GraphControl

/**
 * Adjust how many seconds worth of live data the graph should display
 */
data class LiveZoomAdjustment(val numSeconds: Int) : GraphControl()
