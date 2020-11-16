import react.RBuilder
import react.RComponent
import react.RProps
import react.RState

data class GraphInfo(
    val title: String,
    val series: List<SeriesInfo>
)

data class SeriesInfo(
    val name: String
)

class GraphList : RComponent<GraphListProps, RState>() {
    override fun RBuilder.render() {
    }
}

external interface GraphListProps : RProps {
    var graphInfos: Array<GraphInfo>
}
