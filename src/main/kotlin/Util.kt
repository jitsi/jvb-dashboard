fun keys(obj: dynamic): List<String> {
    return js("Object").keys(obj).unsafeCast<List<String>>()
}

