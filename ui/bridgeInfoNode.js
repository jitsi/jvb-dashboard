import ConferencesNode from './conferencesNode';
import DashboardNode from './dashboardNode';
import React from 'react';

class BridgeInfoNode extends DashboardNode {
    render() {
        return(
            <div>
                {DashboardNode._doRender(this.props.data, (key) => !key.includes("conferences"))}
                <ConferencesNode data={this.props.data}/>
            </div>
        );
    }
}

export default BridgeInfoNode;
