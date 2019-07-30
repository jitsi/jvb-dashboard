import React from 'react';
import DashboardNode from './jsonFilter';

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
                    <DashboardNode
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
