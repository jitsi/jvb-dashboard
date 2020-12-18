import kotlinx.browser.document
import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLInputElement
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.button
import react.dom.defaultValue
import react.dom.h1
import react.dom.input
import react.dom.p
import react.setState

class App : RComponent<RProps, AppState>() {
    init {
        state.jvbUrl = null
    }
    override fun RBuilder.render() {
        h1 {
            +"JVB dashboard"
        }
        p {
            key = "jvbUrl"
            input(type = InputType.text) {
                attrs {
                    id = "jvb-url"
                    defaultValue = "127.0.0.1:4443"
                }
            }
            button {
                attrs {
                    text("Set JVB")
                    onClickFunction = { _ ->
                        val inputUrl = (document.getElementById("jvb-url") as HTMLInputElement).value
                        console.log("setting jvb url to http://$inputUrl/debug")
                        setState {
                            jvbUrl = "http://$inputUrl/debug"
                        }
                    }
                }
            }
        }
        if (!state.jvbUrl.isNullOrBlank()) {
            child(Jvb::class) {
                attrs {
                    url = state.jvbUrl!!
                }
            }
        }
    }
}

external interface AppState : RState {
    var jvbUrl: String?
}
