import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.button

class ChartZoomButtons : RComponent<ChartZoomProps, RState>() {
    override fun RBuilder.render() {
        button {
            attrs {
                text("1 min")
                onClickFunction = {
                    props.onZoomChange?.invoke(60)
                }
            }
        }
        button {
            attrs {
                text("5 mins")
                onClickFunction = {
                    props.onZoomChange?.invoke(300)
                }
            }
        }
        button {
            attrs {
                text("All")
                onClickFunction = {
                    props.onZoomChange?.invoke(Int.MAX_VALUE)
                }
            }
        }
    }
}

external interface ChartZoomProps : RProps {
    var onZoomChange: ((Int) -> Unit)?
}
