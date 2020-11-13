import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.h1

class App : RComponent<RProps, AppState>() {
    override fun AppState.init() {
    }

    override fun RBuilder.render() {
        h1 {
            +"JVB dashboard"
        }
        child(Jvb::class) {
            attrs {
                url = "http://127.0.0.1:4443/debug"
            }
        }
    }
}

external interface AppState : RState
