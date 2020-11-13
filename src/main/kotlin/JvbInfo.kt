import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.p

class JvbInfo : RComponent<JvbProps, RState>() {
    override fun RBuilder.render() {
        p {
            if (props.info.time) {
                +"time: ${props.info?.time}"
            }
        }
    }
}

external interface JvbProps : RProps {
    var info: dynamic
}
