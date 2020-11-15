import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onInputFunction
import kotlinx.html.onInput
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.*

class GraphFilter : RComponent<GraphFilterProps, GraphFilterState>() {
    override fun GraphFilterState.init() {
        this.currSearch = ""
        this.currKeys = listOf()
    }

    override fun componentDidMount() {
        state.currKeys = props.allKeys
    }

    override fun RBuilder.render() {
//        val options = props.allKeys.filter { state.currSearch.isEmpty() || it.contains(state.currSearch) }
////            .mapIndexed { index, s -> li { attrs. = index,  } }
//        ul {
//            options.take(10).forEachIndexed { index, option ->
//                li {
//                    key = "$index"
//                    attrs.value = option
//                }
//            }
//        }
        input(type = InputType.text) {
            attrs.onInputFunction = { event ->
                state.currSearch = event.target.asDynamic().value
                state.currKeys = props.allKeys.filter { state.currSearch.isEmpty() || it.contains(state.currSearch) }
//                println("got input ${event.target.asDynamic().value}")
            }
        }
        select {
            state.currKeys.forEach {
                option {
                    attrs.value = it
                    +it
                }
            }
        }
    }
}


external interface GraphFilterState : RState {
    var currSearch: String
    var currKeys: List<String>
}

external interface GraphFilterProps : RProps {
    var allKeys: List<String>
}
