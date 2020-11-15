fun keys(obj: dynamic): Array<String> {
    return js("Object").keys(obj).unsafeCast<Array<String>>()
}

/**
 * Recursively get all keys.  Doesn't duplicate paths, that is if
 * the object is:
 * {
 *     a: {
 *       b: ...
 *       c: ...
 *     }
 *     d: ...
 * }
 * It will return:
 * a.b, a.c, d
 */
fun getAllKeys(obj: dynamic): List<String> {
    if (obj == undefined) {
        return listOf()
    }
    return keys(obj).flatMap { key ->
        if (jsTypeOf(obj[key]) == "object") {
            (getAllKeys(obj[key]).map { "$key.$it" })
        } else {
            listOf(key)
        }
    }
}


