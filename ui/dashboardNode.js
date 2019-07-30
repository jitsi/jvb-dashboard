import filter from '../util/filter';
import JsonData from './jsonData';
import React from 'react';

/**
 * Component for displaying the top-level dashboard
 */
class DashboardNode extends React.Component {
    static acceptAllFilter = () => true;
    /**
     * Recursively filters the data object for keys which pass the given predicate
     */
    static _filter(data, predicate) {
        return Object.keys(data).reduce((acc, currKey) => {
            if (predicate(currKey)) {
                if (typeof data[currKey] === 'object') {
                    const res = DashboardNode._filter(data[currKey], predicate);

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

    static _doRender(data, predicate) {
        const filteredData = filter(data, predicate || DashboardNode.acceptAllFilter);

        return (
            <div>
                <JsonData
                    data={filteredData}
                />
            </div>
        );
    }

    /**
     * @inheritdoc
     */
    render() {
        if (this.props.data) {
            const filter = this.props.filter || DashboardNode.acceptAllFilter;
            return DashboardNode._doRender(this.props.data, filter);
        }

        return (
            <div>Loading...</div>
        );
    }
}

export default DashboardNode;
