import React from 'react';
import ReactDOM from 'react-dom';

import delayAsync from './util/delayAsync';

import App from './ui/app.js';


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
        await delayAsync(5000);
    }
}

fetchData();

ReactDOM.render(<App />, document.getElementById('app'));
