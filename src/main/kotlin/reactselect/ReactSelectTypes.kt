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

typealias ComponentFactory = () -> dynamic

external interface Components {
    var DropdownIndicator: ComponentFactory?
        get() = definedExternally
        set(value) = definedExternally
    var IndicatorSeparator: ComponentFactory?
        get() = definedExternally
        set(value) = definedExternally
    var Menu: ComponentFactory?
        get() = definedExternally
        set(value) = definedExternally
}

fun Components(): Components = js("{}")

typealias OnChangeCallback = (event: Event) -> Unit
