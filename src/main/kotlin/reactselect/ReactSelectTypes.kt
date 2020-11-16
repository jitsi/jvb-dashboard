package reactselect

import org.w3c.dom.events.Event

external interface Option {
    var value: String?
        get() = definedExternally
        set(value) = definedExternally
    var label: String?
        get() = definedExternally
        set(value) = definedExternally
}

fun Option(): Option = js("{}")

typealias OnChangeCallback = (event: Event) -> Unit
