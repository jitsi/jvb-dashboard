import React from 'react';

/**
 * Component for displaying the top-level dashboard
 */
class JsonData extends React.Component {
    /**
     * @inheritdoc
     */
    async componentDidMount() {
        // Retrieve the data from the psnr test which will be used by
        // the psnr and frame charts.  We do it here so we only make
        // the request once.
        try {
            //const jsonData = await PsnrResultRequester.fetchPsnrResults();

            //this.setState({
            //    psnrData: PsnrResultRequester.getPsnrChartData(jsonData),
            //    frameData: PsnrResultRequester.getFrameChartData(jsonData)
            //});
        } catch (error) {
            this.setState({ error });
        }
    }

    _renderObject(obj, indent) {
        const numSpaces = indent || 0;
        return Object.entries(obj).map(([key, value], i) => {
            if (typeof value == 'object') {
                return (
                    <div key={key}>
                        {key}: {'{'}
                            {this._renderObject(value, numSpaces + 2)}
                        {'}'}
                    </div>
                );
            } else {
                    //{' '.repeat(numSpaces)} {key}: {value}
                return (
                    <div key={key}>
                        {key}: {value}
                    </div>
                )
            }
        })
    }

    /**
     * @inheritdoc
     */
    render() {
        if (this.props.data) {
                    //{this._renderObject(this.props.data)}
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
