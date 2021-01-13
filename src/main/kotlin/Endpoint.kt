import graphs.ChartCollection
import kotlinx.css.paddingLeft
import kotlinx.css.paddingTop
import kotlinx.css.pct
import react.*
import react.dom.div
import react.dom.h3
import styled.css
import styled.styledDiv

class Endpoint : RComponent<EpProps, EpState>() {
    private var chartCollection: ChartCollection? = null

    init {
        state.numericalKeys = emptyList()
        state.nonNumericalKeys = emptyList()
        state.statsId = null
    }

    override fun componentDidMount() {
        // TODO: do we know that when this method runs state.numericalKeys will always be empty?
        if (state.numericalKeys.isEmpty() && props.data != undefined) {
            extractKeys(props.data!!.first())
        }
    }

    private fun extractKeys(data: dynamic) {
        console.log("Extracting keys from ", data)
        setState {
            numericalKeys = getAllKeysWithValuesThat(data) { it is Number }
            nonNumericalKeys = getAllKeysWithValuesThat(data) { it !is Number }
        }
    }

    override fun componentWillUnmount() {}

    fun addData(data: dynamic) {
        if (usingLiveData()) {
            if (state.numericalKeys.isEmpty()) {
                extractKeys(data)
            }
            chartCollection?.addData(data)
        }
    }

    override fun RBuilder.render() {
        console.log("Endpoint ${props.id}: rendering")
        div {
            h3 {
                if (!state.statsId.isNullOrEmpty()) {
                    +"Endpoint ${props.id} (${state.statsId})   "
                } else {
                    +"Endpoint ${props.id}   "
                }
            }
            if (usingLiveData()) {
                div {
                    child(FeatureToggle::class) {
                        attrs {
                            featureName = "PCAP dump"
                            url = "${props.baseRestApiUrl}/features/endpoint/${props.confId}/${props.id}/pcap-dump"
                        }
                    }
                }
            }
            styledDiv {
                css {
                    paddingLeft = 2.pct
                    paddingTop = 2.pct
                }
                child(ChartCollection::class) {
                    attrs {
                        numericalKeys = state.numericalKeys
                        nonNumericalKeys = state.nonNumericalKeys
                        data = props.data
                    }
                    ref {
                        if (it != null) {
                            chartCollection = it as ChartCollection
                        }
                    }
                }
            }
        }
    }

    private fun usingLiveData(): Boolean = props.baseRestApiUrl != null
}

external interface EpProps : RProps {
    var confId: String
    var id: String
    var baseRestApiUrl: String?
    // An optional property to pass pre-existing data (e.g. from a dump file)
    var data: List<dynamic>?
}

// Endpoints don't retrieve their own data, the conference makes a single
// request and updates the props of the ep components
external interface EpState : RState {
    var numericalKeys: List<String>
    var nonNumericalKeys: List<String>
    var statsId: String?
}

sealed class ChartInfo(val id: Int)

class GraphInfo(id: Int) : ChartInfo(id)

class TimelineInfo(id: Int) : ChartInfo(id)
