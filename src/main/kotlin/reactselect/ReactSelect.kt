@file:JsModule("react-select")
@file:JsNonModule

package reactselect

import react.RClass
import react.RProps

@JsName("default")
external val Select: RClass<ReactSelectProps>

external interface ReactSelectProps : RProps {
    var options: Array<Option>
    var onChange: OnChangeCallback
    var isMulti: Boolean
    var components: Components
    var filterOption: () -> FilterFunc
    var onInputChange: () -> OnInputChangeFunc
}

