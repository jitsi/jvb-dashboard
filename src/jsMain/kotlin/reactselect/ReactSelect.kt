@file:JsModule("react-select")
@file:JsNonModule

package reactselect

import react.ComponentClass
import react.PropsWithChildren

@JsName("default")
external val Select: ComponentClass<ReactSelectProps>

external interface ReactSelectProps : PropsWithChildren {
    var options: Array<Option>
    var onChange: OnChangeCallback
    var isMulti: Boolean
    var components: Components
    var filterOption: () -> FilterFunc
    var onInputChange: () -> OnInputChangeFunc
}

