import graphs.ChartCollection
import kotlinx.css.paddingLeft
import kotlinx.css.paddingTop
import kotlinx.css.pct
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.div
import react.dom.h3
import react.setState
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
        if (props.data != null) {
            extractKeys(props.data!!)
            val statsId = props.data!!.findFirstValueFor("statsId")
            if (statsId != undefined) {
                setState {
                    this.statsId = statsId
                }
            }
        }
        if (state.numericalKeys.isEmpty() && props.data != undefined) {
            extractKeys(props.data!!)
        }
    }

    private fun extractKeys(dataList: List<dynamic>) {
        console.log("Extracting keys from ", dataList)
        setState {
            numericalKeys = dataList.flatMap { data ->
                getAllKeysWithValuesThat(data) { it is Number }
            }.toSet().toList()
            nonNumericalKeys = dataList.flatMap { data ->
                getAllKeysWithValuesThat(data) { it !is Number }
            }.toSet().toList()
        }
    }

    private fun extractKeys(data: dynamic) = extractKeys(listOf(data))

    override fun componentWillUnmount() {}

    fun addData(data: dynamic) {
        if (usingLiveData()) {
            if (state.numericalKeys.isEmpty()) {
                extractKeys(data)
            }
            if (state.statsId == null && data.statsId != undefined) {
                setState {
                    statsId = data.statsId
                }
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
