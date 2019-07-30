import React from 'react';
import ReactDOM from 'react-dom';

import App from './ui/app.js';

/**
 * Asynchronously wait for 'ms' milliseconds
 * @param {Number} ms the amount of milliseconds to wait
 */
async function delay(ms) {
    // return await for better async stack trace support in case of errors.
    return await new Promise(resolve => setTimeout(resolve, ms));
}

/**
 * Fetch data from the JVB
 */
async function fetchData() {
    // eslint-disable-next-line no-constant-condition
    while (true) {
        const result = await fetch('http://54.68.179.217:8080/colibri/debug/');

        if (result.status !== 200) {
            console.error(`Error retreiving data ${result.status}`);
        }
        const json = await result.json();

        ReactDOM.render(
            <App
                data={json}
            />,
            document.getElementById('app')
        );
        await delay(5000);
    }
}

fetchData();

ReactDOM.render(<App />, document.getElementById('app'));
