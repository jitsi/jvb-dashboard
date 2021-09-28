import graphs.ChartCollection
import kotlinx.browser.window
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.await
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.css.paddingLeft
import kotlinx.css.paddingTop
import kotlinx.css.pct
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.div
import react.dom.h2
import react.dom.p
import react.setState
import styled.css
import styled.styledDiv

class Conference : RComponent<ConferenceProps, ConferenceState>() {
    private val eps: MutableMap<String, Endpoint> = mutableMapOf()
    private var chartCollection: ChartCollection? = null
    private var job: Job? = null

    init {
        state.epIds = arrayOf()
        state.expanded = false
        state.dataByEp = null
        state.numericalKeys = emptyList()
        state.nonNumericalKeys = emptyList()
    }

    override fun componentDidMount() {
        // Start the fetch data job if there's a URL to query
        if (usingLiveData()) {
            job = GlobalScope.launch { fetchDataLoop(props.baseRestApiUrl!!) }
        }
        // TODO: do we know that when this method runs state.numericalKeys will always be empty?
        if (state.numericalKeys.isEmpty() && props.confData != undefined) {
            extractKeys(props.confData!!.first())
        }

        if ((state.epIds == undefined || state.epIds.isEmpty()) && !usingLiveData()) {
            val confData = props.confData!!
            // We need to extract all epIds present in all the data
            val allEpIds = confData.flatMap {
                val epIds = getEpIds(it).toList()
                epIds
            }.toSet()
            val dataByEp = mutableMapOf<String, MutableList<dynamic>>()
            confData.forEach { confDataEntry ->
                if (confDataEntry.timestamp == undefined) {
                    return@forEach
                }
                val timestamp = confDataEntry.timestamp as Number
                val epIds = getEpIds(confDataEntry)
                epIds.forEach { epId ->
                    val epData = confDataEntry.endpoints[epId]
                    // Set the timestamp in the object we'll pass down, since it's at a higher
                    // level
                    epData.timestamp = timestamp
                    val existingEpData = dataByEp.getOrPut(epId) { mutableListOf() }
                    existingEpData.add(epData)
                }
            }
            val name = confData.asSequence().map { it.name }.first { it != undefined } ?: "No conf name found"
            setState {
                epIds = allEpIds.toTypedArray()
                this.dataByEp = dataByEp
                this.name = name
            }
        }
    }

    private fun extractKeys(data: dynamic) {
        console.log("Extracting keys from ", data)
        setState {
            numericalKeys = getAllKeysWithValuesThat(data) { it is Number }
            nonNumericalKeys = getAllKeysWithValuesThat(data) { it !is Number }
        }
    }

    override fun componentWillUnmount() {
        job?.cancel("Unmounting")
    }

    // TODO: we need this function because react doesn't do a deep comparison on the epIds array to know whether
    //  or not it changed.  We could introduce an 'EndpointList' component whose only state was the IDs, and then
    //  get rid of this override here and move it there (which would be a bit cleaner, since here we may add more state
    //  and would have to remember to take it into account in this method)
    override fun shouldComponentUpdate(nextProps: ConferenceProps, nextState: ConferenceState): Boolean {
        return (state.expanded != nextState.expanded) ||
            (state.name != nextState.name) ||
            (!state.epIds.contentEquals(nextState.epIds) ||
            (state.numericalKeys != nextState.numericalKeys) ||
            (state.nonNumericalKeys != nextState.nonNumericalKeys))
    }

    override fun RBuilder.render() {
        console.log("conference ${props.id} rendering")
        div {
            key = props.id
            h2 {
                if (state.expanded) {
                    +"▾"
                } else {
                    +"▸"
                }
                if (state.name != undefined) {
                    +"Conference \"${state.name}\" (${props.id})"
                } else {
                    +"Conference (${props.id})"
                }
                attrs {
                    onClickFunction = { _ ->
                        setState {
                            expanded = !expanded
                        }
                    }
                }
            }
            if (state.expanded) {
                if (state.epIds == undefined || state.epIds.isEmpty()) {
                    p {
                        +"No data received yet"
                        return
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
                            data = props.confData
                        }
                        ref {
                            if (it != null) {
                                chartCollection = it as ChartCollection
                            }
                        }
                    }
                }
                state.epIds.forEach { epId ->
                    styledDiv {
                        css {
                            paddingLeft = 2.pct
                        }
                        key = epId
                        child(Endpoint::class) {
                            attrs {
                                confId = props.id
                                id = epId
                                baseRestApiUrl = props.baseRestApiUrl
                                state.dataByEp?.get(epId)?.let { existingEpData ->
                                    data = existingEpData
                                }
                            }
                            ref {
                                if (it != null) {
                                    eps[epId] = it as Endpoint
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // The baseRestApiUrl in props may be null, so we call this (with a non-null version) only when it isn't null
    private suspend fun CoroutineScope.fetchDataLoop(baseRestApiUrl: String) {
        while (isActive) {
            try {
                val jvbData = window.fetch("$baseRestApiUrl/${props.id}")
                    .await()
                    .json()
                    .await()
                    .asDynamic()
                val now = jvbData.time.unsafeCast<Number>()
                val confData = jvbData.conferences[props.id]
                if (state.numericalKeys.isEmpty()) {
                    extractKeys(confData)
                }
                confData.timestamp = now
                chartCollection?.addData(confData)
                val epIds = getEpIds(confData)
                setState {
                    this.name = confData.name.unsafeCast<String>().substringBefore('@')
                    this.epIds = epIds
                }
                eps.forEach { (epId, ep) ->
                    val epData = confData.endpoints[epId]
                    epData.timestamp = now
                    ep.addData(epData)
                }
                delay(1000)
            } catch (c: CancellationException) {
                console.log("Conference ${props.id} fetch loop cancelled")
                throw c
            } catch (t: Throwable) {
                console.log("Conference ${props.id} fetch loop error: ", t)
            }
        }
    }

    private fun usingLiveData(): Boolean = props.baseRestApiUrl != null
}

private fun getEpIds(confData: dynamic): Array<String> {
    // we only want to consider lines like this:
    // {"endpoints":{"bca514fc":{"iceTransport":{"iceConnected":true,"num_packets_received":328,"num_packets_sent":76}...
    // and not lines like this:
    // {"confName":"jitsisitdown","meetingUniqueId":"804786d39ed9aba3","applicationName":"JVB","endpoints":["Lawson-rqz", ...
    // in other words, we want to extract the endpoint stats tree root keys
    return if (confData.endpoints != undefined && confData.endpoints !is Array<String>)
        keys(confData.endpoints) else emptyArray()
}

external interface ConferenceState : RState {
    var epIds: Array<String>
    var name: String
    var expanded: Boolean
    var numericalKeys: List<String>
    var nonNumericalKeys: List<String>
    var dataByEp: MutableMap<String, MutableList<dynamic>>?
}

external interface ConferenceProps : RProps {
    var baseRestApiUrl: String?
    var id: String
    var confData: List<dynamic>?
}
