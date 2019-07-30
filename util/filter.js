/**
 * Recursively filters the data object for keys which pass the given predicate
 */
function filter(data, predicate) {
    if (data === null || typeof data === 'undefined') {
        return {};
    }
    return Object.keys(data).reduce((acc, currKey) => {
        if (predicate(currKey)) {
            if (typeof data[currKey] === 'object') {
                const res = filter(data[currKey], predicate);

                if (Object.keys(res).length > 0) {
                    acc[currKey] = res;
                }
            } else {
                acc[currKey] = data[currKey];
            }
        }

        return acc;
    }, {});
}

export default filter;
