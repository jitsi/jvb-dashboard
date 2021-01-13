import kotlinx.css.FontWeight
import kotlinx.css.fontWeight
import kotlinx.html.js.onClickFunction
import react.*
import styled.css
import styled.styledButton

class ChartZoomButtons : RComponent<ChartZoomProps, ChartZoomState>() {
    init {
        // Default to 1 minute
        state.currZoomSeconds = 60
    }

    override fun RBuilder.render() {
        +"Zoom level "
        styledButton {
            if (state.currZoomSeconds == 60) {
                css {
                    this.fontWeight = FontWeight.bold
                }
            }
            attrs {
                text("1 min")
                onClickFunction = {
                    setState {
                        currZoomSeconds = 60
                    }
                    props.onZoomChange?.invoke(60)
                }
            }
        }
        styledButton {
            if (state.currZoomSeconds == 300) {
                css {
                    this.fontWeight = FontWeight.bold
                }
            }
            attrs {
                text("5 min")
                onClickFunction = {
                    setState {
                        currZoomSeconds = 300
                    }
                    props.onZoomChange?.invoke(300)
                }
            }
        }
        styledButton {
            if (state.currZoomSeconds == Int.MAX_VALUE) {
                css {
                    this.fontWeight = FontWeight.bold
                }
            }
            attrs {
                text("All")
                onClickFunction = {
                    setState {
                        currZoomSeconds = Int.MAX_VALUE
                    }
                    props.onZoomChange?.invoke(Int.MAX_VALUE)
                }
            }
        }
    }

    fun currZoomSeconds(): Int = state.currZoomSeconds
}

external interface ChartZoomState : RState {
    var currZoomSeconds: Int
}

external interface ChartZoomProps : RProps {
    var onZoomChange: ((Int) -> Unit)?
    var startingZoomSeconds: Int?
}
