import React from 'react';
import ReactJson from 'react-json-view'

/**
 * Component for displaying the top-level dashboard
 */
class JsonData extends React.Component {
    /**
     * @inheritdoc
     */
    render() {
        if (this.props.data) {
            return (
                <div>
                    <ReactJson
                        src={this.props.data}
                        displayDataTypes={false}
                        collapsed={2}
                    />
                </div>
            );
        }

        return (
            <div>Loading...</div>
        );
    }
}

export default JsonData;
