fun keys(obj: dynamic): Array<String> {
    return js("Object").keys(obj).unsafeCast<Array<String>>()
}

