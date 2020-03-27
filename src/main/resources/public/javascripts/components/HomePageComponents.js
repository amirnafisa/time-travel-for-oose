
class HomePageTabs extends React.Component{
    _isMounted = false;
    constructor(props){
        super(props);
        this.state = {activeTab:0};
        this.handleUpcomingTripsClick = this.handleUpcomingTripsClick.bind(this);
        this.handleAddAirBookingClick = this.handleAddAirBookingClick.bind(this);
        this.handleAddHotelBookingClick = this.handleAddHotelBookingClick.bind(this);
    }

    componentDidMount() {
        this._isMounted = true;
    }

    componentWillUnmount() {
        this._isMounted = false;
    }

    handleUpcomingTripsClick(){
        this._isMounted && this.setState({activeTab:0});
    }
    handleAddAirBookingClick(){
        this._isMounted && this.setState({activeTab:1});
    }
    handleAddHotelBookingClick(){
        this._isMounted && this.setState({activeTab:2});
    }
    render(){

        return(
            <div className="container-fluid">
                <div className="row">
                    <div className="flex-column bg-light">
                        <div className="container-fluid nav-tabs">
                            <div className="row nav-item active">
                                <TabButton active={this.state.activeTab===0?"active":""} handleClick={this.handleUpcomingTripsClick} title="Trips"/>
                            </div>

                            <div className="row nav-item">
                                <TabButton active={this.state.activeTab===1?"active":""}  handleClick={this.handleAddAirBookingClick} title="Add Air Booking"/>
                            </div>
                            <div className="row nav-item">
                                <TabButton active={this.state.activeTab===2?"active":""}  handleClick={this.handleAddHotelBookingClick} title="Add Hotel Booking"/>
                            </div>
                        </div>
                    </div>
                    <div className="col-10 tab-content">
                        {this.state.activeTab === 0 ? <ItineraryList/> :
                                    this.state.activeTab == 1? <NewAirForm/> :
                                        <NewHotelForm/>
                        }
                    </div>
                </div>
            </div>
        );
    }

}


