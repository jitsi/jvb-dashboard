import ConferenceNode from './conferenceNode';
import DashboardNode from './dashboardNode';
import filter from '../util/filter';
import React from 'react';

class ConferencesNode extends React.Component {
    static _conferencesFilter = (key) => key == 'conferences';
    _renderConferences(conferences) {
        return
    }
    render() {
        const conferences = this.props.data.conferences || {};
        return(
            <div>
                num_conferences: {Object.keys(conferences).length}
                {Object.keys(conferences).map((confId, index) => {
                    return <ConferenceNode key={confId} data={conferences[confId]} />
                })
                }
            </div>
        );
    }
}

export default ConferencesNode;
