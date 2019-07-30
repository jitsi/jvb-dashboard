import DashboardNode from './dashboardNode';
import filter from '../util/filter';
import React from 'react';

class ConferenceNode extends DashboardNode {
    async componentDidMount() {
        const result = await fetch(`http://54.68.179.217:8080/colibri/debug/${this.props.data.id}`);
        const json = await result.json();
        this.setState({ data: json.conferences[this.props.data.id] });
    }
    render() {
        if (this.state && this.state.data) {
            const d = this.state.data;
            d['creationTime'] = new Date(d['creationTime']).toString();
            d['lastActivity'] = new Date(d['lastActivity']).toString();
            return DashboardNode._doRender(d);
        }
        return(
            <div>Loading...</div>
        );
    }
}

export default ConferenceNode;
