import React from 'react';
import DashboardNode from './dashboardNode';
import BridgeInfoNode from './bridgeInfoNode';

/**
 * Component for displaying the top-level dashboard
 */
class Dashboard extends React.Component {
    static defaultProps = {
        data: {}
    };

    /**
     * @inheritdoc
     */
    render() {
        if (this.props.data) {
            return (
                <div>
                    <BridgeInfoNode
                        data={this.props.data}
                    />
                </div>
            );
        }

        return (
            <div>Loading...</div>
        );
    }
}

export default Dashboard;
