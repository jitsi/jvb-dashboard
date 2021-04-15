import kotlinx.css.span
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.div
import react.dom.span
import react.router.dom.hashRouter
import react.router.dom.route
import react.router.dom.routeLink
import react.router.dom.switch

class App : RComponent<RProps, RState>() {
    override fun RBuilder.render() {
        hashRouter {
            switch {
                route("/", exact = true) {
                    div {
                        routeLink("/dump") {
                            +"Dump viewer"
                        }
                        span {
                            +" | "
                        }
                        routeLink("/live") {
                            +"Live dashboard"
                        }
                    }
                }
                route("/dump", Dump::class, exact = true)
                route("/live", LiveDashboard::class, exact = true)
            }
        }
    }
}
