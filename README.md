# jvb-dashboard
JVB dashboard provides a UI to connect to a JVB's REST API and display live graphs of the data it publishes.

# Building
* Checkout the code
* Run `./gradlew build`

# Deploying
The built files will be in `/build/distributions`.  These files can be copied anywhere and hosted.  The easiest way to host
them locally is to run `python3 -m http.server` in the `/build/distributions` directory.

# Known issues
* The JVB's REST API is usually not publicly accessible, so the easiest way to have the UI connect to it is to create an
SSH tunnel, and then have the UI connect to the local port of the tunnel.  For example, run:
`ssh -L4443:localhost:8080 <jvb-ip>` and then tell the jvb-dashboard UI to connect to `http://127.0.0.1:4443`.
* ~There is currently a CORS issue with accessing the JVB REST API in this way.  You can use [this chrome extension](https://chrome.google.com/webstore/detail/allow-cors-access-control/lhobafahddgcelffkeicbaginigeejlf) to work around this problem.~ No longer an issue for JVB versions >= `2.1-411-g9754898e`
