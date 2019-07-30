import React from 'react';
import JsonData from './jsonData';

/**
 * Component for displaying the top-level dashboard
 */
class DashboardNode extends React.Component {
    /**
     * Recursively filters the data object for keys which pass the given predicate
     */
    static _filter(data, predicate) {
        return Object.keys(data).reduce((acc, currKey) => {
            if (typeof data[currKey] === 'object') {
                const res = DashboardNode._filter(data[currKey], predicate);

                if (Object.keys(res).length > 0) {
                    acc[currKey] = res;
                }
            } else if (predicate(currKey)) {
                acc[currKey] = data[currKey];
            }

            return acc;
        }, {});
    }

    /**
     * @inheritdoc
     */
    render() {
        if (this.props.data) {
            const acceptAll = () => true;
            const filter = this.props.filter || acceptAll;
            const filteredData = DashboardNode._filter(this.props.data, filter);

            return (
                <div>
                    <JsonData
                        data={filteredData}
                    />
                </div>
            );
        }

        return (
            <div>Loading...</div>
        );
    }
}

export default DashboardNode;
