import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import reactselect.AsyncSelect
import reactselect.Option

class Selector : RComponent<SelectorProps, RState>() {
    override fun RBuilder.render() {
        val allOptions = props.allKeys?.map { Option(it) }?.toTypedArray() ?: emptyArray()
        AsyncSelect {
            attrs {
                loadOptions = { inputValue, callback ->
                    // Since we don't show all keys by default, show them all if the user
                    // searches for "*"
                    if (inputValue == "*") {
                        callback(allOptions)
                    } else {
                        val filteredOptions = allOptions
                            .filter { it.value!!.contains(inputValue, ignoreCase = true) }
                            .take(50)
                            .toTypedArray()
                        callback(filteredOptions)
                    }
                }
                defaultOptions = allOptions.take(50).toTypedArray()
                isMulti = true
                onChange = { event ->
                    val keys = event.unsafeCast<Array<dynamic>>().map { evt ->
                        evt.value.unsafeCast<String>()
                    }.toList()
                    props.onSelectedKeysChange?.invoke(keys)
                }
            }
        }
    }
}

external interface SelectorProps : RProps {
    var allKeys: List<String>?
    var onSelectedKeysChange: ((List<String>) -> Unit)?
}
