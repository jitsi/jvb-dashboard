@file:JsModule("react-select/async")
@file:JsNonModule

package reactselect

import react.ComponentClass
import react.PropsWithChildren

@JsName("default")
external val AsyncSelect: ComponentClass<AsyncReactSelectProps>

external interface AsyncReactSelectProps : PropsWithChildren {
    var loadOptions: (String /* inputValue */, (Array<Option>) -> Unit /* callback */) -> Unit
    var isMulti: Boolean
    var onChange: OnChangeCallback
    var defaultOptions: dynamic /* Boolean | array of initial options */
}

