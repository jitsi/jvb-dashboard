import kotlinx.browser.document
import react.dom.h1
import react.dom.render

fun main() {
    render(document.getElementById("root")) {
        child(App::class) {}
    }
}
