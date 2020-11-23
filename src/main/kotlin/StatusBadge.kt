import kotlinx.css.Color
import kotlinx.css.color
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.div
import styled.css
import styled.styledDiv

/**
 * A status badge renders some text and changes the color of its background
 * based on the value of that text
 */
abstract class StatusBadge<T>(
    private val fieldName: String,
    private val evaluationFunc: (T) -> Status
) : RComponent<StatusBadgeProps<T>, RState>() {
    override fun RBuilder.render() {
        val status = evaluationFunc(props.value)
        styledDiv {
            css {
                color = status.color
            }
            +"$fieldName: ${props.value}"
        }
    }
}

enum class Status(val color: Color) {
    GREEN(Color.green),
    ORANGE(Color.orange),
    RED(Color.red)
}

external interface StatusBadgeProps<T> : RProps {
    var value: T
}

class JvbStressStatusBadge : StatusBadge<Double>(
    "Stress",
    Companion::stressLevelFunc
) {
    companion object {
        fun stressLevelFunc(stress: Double): Status {
            return when {
                stress < .5 -> Status.GREEN
                stress < .75 -> Status.ORANGE
                else -> Status.RED
            }
        }
    }
}
