import org.w3c.dom.HTMLInputElement
import org.w3c.files.File
import react.RBuilder
import react.RComponent
import react.PropsWithChildren
import react.State
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.setState
import web.html.InputType

class Dump : RComponent<PropsWithChildren, DumpState>() {
    init {
        state.file = null
    }
    override fun RBuilder.render() {
        console.log("blah")
        if (state.file == null) {
            div {
                input {
                    attrs {
                        type = InputType.file
                        onChange = { event ->
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

external interface DumpState : State {
    var file: File?
}
