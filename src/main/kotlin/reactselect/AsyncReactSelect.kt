@file:JsModule("react-select/async")
@file:JsNonModule

package reactselect

import react.RClass
import react.RProps

@JsName("default")
external val AsyncSelect: RClass<AsyncReactSelectProps>

external interface AsyncReactSelectProps : RProps {
    var loadOptions: (String /* inputValue */, (Array<Option>) -> Unit /* callback */) -> Unit
    var isMulti: Boolean
    var onChange: OnChangeCallback
    var defaultOptions: dynamic /* Boolean | array of initial options */
}

