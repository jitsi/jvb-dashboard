import React from 'react';

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
                    <pre>
                        {JSON.stringify(this.props.data, null, 2)}
                    </pre>
                </div>
            );
        }

        return (
            <div>Loading...</div>
        );
    }
}

export default JsonData;
