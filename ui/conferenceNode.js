import DashboardNode from './dashboardNode';
import delayAsync from '../util/delayAsync';
import filter from '../util/filter';
import React from 'react';

class ConferenceNode extends React.Component {
    _running;

    async componentDidMount() {
        this._running = true;
        this._refreshData();
    }
    render() {
        if (this.state && this.state.data) {
            const d = this.state.data;
            d['creationTime'] = new Date(d['creationTime']).toString();
            d['lastActivity'] = new Date(d['lastActivity']).toString();
            d['numEndpoints'] = Object.keys(d.endpoints).length;
            let filter;
            if (this.state.showDetails || true) {
                filter = key => true;
            } else {
                filter = key => {
                    return [
                        'creationTime',
                        'lastActivity',
                        'speechActivity',
                            'dominantSpeakerIdentification',
                            'dominantEndpoint',
                        'numEndpoints'
                    ].indexOf(key) !== -1;
                };
            }
            //TODO: onClick here conflicts with the json view, but don't think we'll end up using
            // both in the same place in the end?
            // <div onClick={this.handleClick.bind(this)}>
            return(
                <div >
                    <DashboardNode name={`${d.name}-${d.id}`} data={d} filter={filter} />
                </div>
            );
        }
        return(
            <div>Loading...</div>
        );
    }

    handleClick() {
        this.setState({ showDetails: true });
    }

    async _refreshData() {
        while (this._running) {
            const result = await fetch(`http://54.68.179.217:8080/colibri/debug/${this.props.data.id}`);
            const json = await result.json();
            this.setState({ data: json.conferences[this.props.data.id] });
            delayAsync(5000);
        }
    }

    componentWillUnmount() {
        this._running = false;
    }
}

export default ConferenceNode;
