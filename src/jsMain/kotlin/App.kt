import js.objects.jso
import react.FC
import react.Props
import react.RBuilder
import react.RComponent
import react.PropsWithChildren
import react.State
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import react.react
import react.router.RouterProvider
import react.router.dom.Link
import react.router.dom.createHashRouter

val root = FC<Props> {
    div {
        Link {
            to = "/dump"
            +"Dump viewer"
        }
        span {
            +" | "
        }
        Link {
            to = "/live"
            +"Live dashboard"
        }
    }
}


private val hashRouter = createHashRouter(
    routes = arrayOf(
        jso {
            path = "/"
            Component = root
        },
        jso {
            path = "/dump"
            Component = Dump::class.react
        },
        jso {
            path = "/live"
            Component = LiveDashboard::class.react
        }
    )
)

val App = FC<Props> {
     RouterProvider {
         router = hashRouter
     }
}
