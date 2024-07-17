import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.await
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.css.Display
import kotlinx.css.Margin
import kotlinx.css.display
import kotlinx.css.margin
import kotlinx.css.px
import kotlinx.html.js.onClickFunction
import org.w3c.xhr.XMLHttpRequest
import react.RBuilder
import react.RComponent
import react.PropsWithChildren
import react.State
import react.dom.button
import react.dom.div
import react.setState
import styled.css
import styled.styledDiv

class FeatureToggle : RComponent<FeatureToggleProps, FeatureToggleState>() {
    private var job: Job? = null

    init {
        state.enabled = false
    }

    private suspend fun CoroutineScope.fetchState() {
        while (isActive) {
            val data = window.fetch(props.url)
                .await()
                .text()
                .await()
            setState {
                enabled = data == "true"
            }
            delay(1000)
        }
    }

    override fun componentDidMount() {
        job = GlobalScope.launch { fetchState() }
    }

    override fun componentWillUnmount() {
        job?.cancel("Unmounting")
    }

    override fun RBuilder.render() {
        styledDiv {
            css {
                display = Display.inlineBlock
                margin = Margin(10.px)
            }
            +props.featureName
            div {
                button {
                    val isEnabled: Boolean = state.enabled
                    attrs.text(if (isEnabled) "Disable" else "Enable")
                    attrs.onClickFunction = { _ ->
                        XMLHttpRequest().apply {
                            open("POST", "${props.url}/${!isEnabled}", async = true)
                            send()
                        }
                        setState {
                            enabled = !state.enabled
                        }
                    }
                }
            }
        }
    }
}

external interface FeatureToggleState : State {
    var enabled: Boolean
}

external interface FeatureToggleProps : PropsWithChildren {
    var featureName: String
    var url: String
}
