import kotlinx.css.FontWeight
import kotlinx.css.fontWeight
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.RComponent
import react.PropsWithChildren
import react.State
import react.setState
import styled.css
import styled.styledButton
import kotlin.time.Duration


class ChartZoomButtons : RComponent<ChartZoomProps, ChartZoomState>() {
    init {
        // Default to 1 minute
        state.currZoomSeconds = 60
    }

    override fun RBuilder.render() {
        +"Zoom level "
        props.buttons?.forEach { buttonDesc ->
            styledButton {
                if (state.currZoomSeconds == buttonDesc.zoomSeconds) {
                    css {
                        this.fontWeight = FontWeight.bold
                    }
                }
                attrs.text(buttonDesc.title)
                attrs.onClickFunction = {
                    setState {
                        currZoomSeconds = buttonDesc.zoomSeconds
                    }
                    props.onZoomChange?.invoke(buttonDesc.zoomSeconds)
                }
            }
        }
    }

    fun currZoomSeconds(): Int = state.currZoomSeconds
}

external interface ChartZoomState : State {
    var currZoomSeconds: Int
}

external interface ChartZoomProps : PropsWithChildren {
    var onZoomChange: ((Int) -> Unit)?
    var buttons: List<ZoomButtonDesc>?
}

data class ZoomButtonDesc(
    val title: String,
    val zoomSeconds: Int,
    val default: Boolean = false
)

fun ZoomButtonDesc(title: String, size: Duration): ZoomButtonDesc =
    ZoomButtonDesc(title, size.inWholeSeconds.toInt())
