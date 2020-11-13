import react.RBuilder
import react.RComponent
import react.RProps
import react.RState

data class GraphInfo(
    val name: String,
    val dataSource: () -> dynamic
)

class GraphList : RComponent<GraphListProps, RState>() {
    override fun RBuilder.render() {
    }
}

external interface GraphListProps : RProps {
    var graphInfos: Array<GraphInfo>
}
