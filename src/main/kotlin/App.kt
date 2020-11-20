import kotlinx.browser.document
import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLInputElement
import react.*
import react.dom.button
import react.dom.h1
import react.dom.input
import react.dom.p

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
                    placeholder = "127.0.0.1:4443"
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
