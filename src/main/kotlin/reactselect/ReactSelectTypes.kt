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

fun Option(value: String? = null, label: String? = null): Option =
    js("{}").unsafeCast<Option>().also { opt ->
        value?.let { opt.value = it }
        label?.let { opt.label = it } ?: value?.let { opt.label = it }
    }

typealias ComponentFactory = (dynamic) -> dynamic

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
    var MenuList: ComponentFactory?
        get() = definedExternally
        set(value) = definedExternally
}

fun Components(): Components = js("{}")

typealias OnChangeCallback = (event: Event) -> Unit

typealias FilterFunc = (dynamic, String) -> dynamic
typealias OnInputChangeFunc = () -> Unit

