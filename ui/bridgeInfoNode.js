import DashboardNode from './dashboardNode';

class BridgeInfoNode extends DashboardNode {
    render() {
        return DashboardNode._doRender(this.props.data, (key) => !key.includes("conferences"));
    }
}

export default BridgeInfoNode;
