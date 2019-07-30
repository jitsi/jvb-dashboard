import ConferencesNode from './conferencesNode';
import DashboardNode from './dashboardNode';
import React from 'react';

class BridgeInfoNode extends React.Component {
    render() {
        return(
            <div>
                <DashboardNode data={this.props.data} filter={key => !key.includes("conferences")} />
                <ConferencesNode data={this.props.data}/>
            </div>
        );
    }
}

export default BridgeInfoNode;
