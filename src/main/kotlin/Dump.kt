import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import org.w3c.dom.HTMLInputElement
import org.w3c.files.File
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.div
import react.dom.input
import react.setState

class Dump : RComponent<RProps, DumpState>() {
    init {
        state.file = null
    }
    override fun RBuilder.render() {
        console.log("blah")
        if (state.file == null) {
            div {
                input(type = InputType.file) {
                    attrs {
                        onChangeFunction = { event ->
                            val file = (event.target as HTMLInputElement).files?.item(0)
                            console.log("got file ", file)
                            setState {
                                this.file = file
                            }
                        }
                    }
                }
            }
        } else {
            child(DumpViewer::class) {
                attrs {
                    file = state.file!!
                }
            }
        }
    }
}

external interface DumpState : RState {
    var file: File?
}
