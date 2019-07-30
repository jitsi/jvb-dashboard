import DashboardNode from './dashboardNode';

class ConferenceNode extends DashboardNode {
    render() {
        return DashboardNode._doRender(this.props.data, (key) => {
            return [
                'name',
                'id'
            ].indexOf(key) !== -1;
        });
    }
}

export default ConferenceNode;
