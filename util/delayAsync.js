
/**
 * Asynchronously wait for 'ms' milliseconds
 * @param {Number} ms the amount of milliseconds to wait
 */
async function delayAsync(ms) {
    // return await for better async stack trace support in case of errors.
    return await new Promise(resolve => setTimeout(resolve, ms));
}

export default delayAsync;
