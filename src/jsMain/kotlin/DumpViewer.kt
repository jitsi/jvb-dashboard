import org.w3c.files.File
import org.w3c.files.FileReader
import react.RBuilder
import react.RComponent
import react.PropsWithChildren
import react.State
import react.setState

class DumpViewer : RComponent<DumpViewerProps, DumpViewerState>() {
    init {
        state.data = null
        state.error = null
    }

    override fun RBuilder.render() {
        console.log("DumpViewer rendering")
        when {
            state.error != null -> {
                +"Error parsing dump file: '${state.error}'"
            }
            state.data == null -> {
                +"Loading..."
                val reader = FileReader().apply {
                    onload = { event ->
                        val rawData = (event.target as FileReader).result as String
                        try {
                            val data = rawData.split("\r\n", "\n")
                                .filter { it.isNotEmpty() }
                                .map { JSON.parse<dynamic>(it) }
                                .map {
                                    try {
                                        if (it.data is String) {
                                            val lineData = JSON.parse<dynamic>(it.data)
                                            js("Object").assign(it, lineData)
                                        }
                                    } catch (t: Throwable) {
                                        // Ignore any failures
                                    }
                                    it
                                }
                                .toList()

                            setState {
                                error = null
                                this.data = data
                            }
                        } catch (t: Throwable) {
                            setState {
                                error = t.message
                            }
                        }
                    }
                }
                reader.readAsText(props.file)
            }
            else -> {
                val data = state.data as List<dynamic>
                val jicofo = try {
                    var connectionInfo = data[0][2]
                    if (connectionInfo is String) {
                        connectionInfo = JSON.parse<dynamic>(connectionInfo)
                    }
                    val clientProtocol = connectionInfo.clientProtocol as String
                    clientProtocol.contains("jicofo", ignoreCase = true)
                } catch (e: Exception) {
                    false
                }
                console.log("Rendering a ${if (jicofo) "jicofo" else "JVB"} conference.")
                if (jicofo) {
                    child(JicofoConference::class) {
                        attrs {
                            id = getJicofoConfId(data)
                            confData = data
                        }
                    }
                } else {
                    child(Conference::class) {
                        attrs {
                            id = getConfId(data)
                            confData = data
                        }
                    }
                }
            }
        }
    }
}

private fun getConfId(data: List<dynamic>?): String {
    return data?.asSequence()
        ?.map { it.id }
        ?.firstOrNull { it != undefined } as? String ?: "No conf ID found"
}
private fun getJicofoConfId(data: List<dynamic>?): String {
    return data?.asSequence()
        ?.map { it.confName }
        ?.firstOrNull { it != undefined } as? String ?: "No conf ID found"
}

private fun getConfName(data: List<dynamic>?): String {
    return data?.asSequence()
        ?.map { it.name }
        ?.firstOrNull { it != undefined } as? String ?: "No conf name found"
}

external interface DumpViewerState : State {
    // A list of JSON stat entries from the dump
    var data: List<dynamic>?
    var error: String?
}

external interface DumpViewerProps : PropsWithChildren {
    var file: File
}
