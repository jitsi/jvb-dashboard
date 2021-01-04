import graphs.Timeseries
import highcharts.Point
import org.w3c.files.File
import org.w3c.files.FileReader
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.setState

class DumpViewer : RComponent<DumpViewerProps, DumpViewerState>() {
    init {
        state.data = null
        state.error = null
    }
    override fun RBuilder.render() {
        console.log("DumpViewer rendering")
        if (state.error != null) {
            +"Error parsing dump file: '${state.error}'"
        } else if (state.data == null) {
            val reader = FileReader().apply {
                onload = { event ->
                    val rawData = (event.target as FileReader).result as String
                    try {
                        val data = rawData.split("\r\n", "\n")
                            .filter { it.isNotEmpty() }
                            .map { JSON.parse<dynamic>(it) }
                            .toList()

                        setState {
                            error = null
                            this.data = data
                        }
                    } catch (t: Throwable) {
                        setState {
                            error = t.message
                        }
                    }
                }
            }
            reader.readAsText(props.file)
        } else {
            console.log("got data ", state.data)
        }
    }
}

external interface DumpViewerState : RState {
    // A list of JSON stat entries from the dump
    var data: List<dynamic>?
    var error: String?
}

external interface DumpViewerProps : RProps {
    var file: File
}
