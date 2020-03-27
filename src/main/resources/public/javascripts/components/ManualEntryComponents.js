function refreshPage() {
    window.location.reload();
}

function submitBooking(formDataJson) {
    function getFormData(object) {
        const formData = new FormData();
        Object.keys(object).forEach(key => formData.append(key, object[key]));
        return formData;
    }

    document.body.style.cursor='wait';
    fetch("/bookings/add/", { method: "PUT", body: getFormData(formDataJson) })
        .then(response => {
            if(!response.ok) {
                throw new Error("Adding new Booking Failed with HTTP Status " + response.status);
            }
            document.body.style.cursor='default';
        });

}

class NewAirForm extends React.Component {
    _isMounted = false;
    constructor(props) {
        super(props);
        this.state = {type: 'Air', source: '', confirmation_no: '', first_name: '', last_name: '',
            airline: '', flight_code: '', origin_name: '', destination_name: '', departure_date:'',
            departure_time: '', arrival_date: '', arrival_time: '',
            ticket_number: '', travelers: [''], numTravelers: 1};
        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.addMoreTravelers = this.addMoreTravelers.bind(this);
        this.handleDynamicTravelersChange = this.handleDynamicTravelersChange.bind(this);
        this.callback = this.callback.bind(this);
    }

    componentDidMount() {
        this._isMounted = true;
    }

    componentWillUnmount() {
        this._isMounted = false;
    }

    handleDynamicTravelersChange(newState, index) {
        let travelers = this.state.travelers;
        travelers[index] = newState;
        this._isMounted && this.setState({travelers:travelers});
    }

    handleChange(newState) {
        this._isMounted && this.setState(newState);
    }

    callback (lat_lon) {
        if(lat_lon.length === 0) {
            alert("Address not found. Please update Airport code.");
        } else if(confirm("You sure want to submit the booking?")) {
            let jsonState = this.state;

            jsonState.departure_datetime = this.state.departure_date + 'T' + this.state.departure_time;
            jsonState.arrival_datetime = this.state.arrival_date + 'T' + this.state.arrival_time;
            [jsonState.iata_code, jsonState.flight_number] = jsonState.flight_code.split(/^([A-Za-z]{3}|[A-Za-z]{2}|[A-Za-z][0-9]|[0-9][A-Za-z])([0-9]+)$/).slice(1, 3);
            jsonState.travelers = this.state.travelers.join(',');
            //Dont take latitude and longitude and timezoneid input from user. Next iteration try to get it from google Maps
            jsonState.time_zone_id = '';
            jsonState.origin_lat = lat_lon[0].lat;
            jsonState.origin_lon = lat_lon[0].lon;
            jsonState.origin_city_name = lat_lon[0].city;
            jsonState.origin_country = lat_lon[0].country;
            jsonState.destination_lat = lat_lon[1].lat;
            jsonState.destination_lon = lat_lon[1].lon;
            jsonState.destination_city_name = lat_lon[1].city;
            jsonState.destination_country = lat_lon[1].country;
            submitBooking(jsonState);
            alert("Booking Submitted! Sync Again to view new itineraries.");
            refreshPage();
        }
    }

    handleSubmit (event) {
        event.preventDefault();
        setMultipleGeometryLocation([[this.state.origin_name, "Airport"].join(" "), [this.state.destination_name, "Airport"].join(" ")], this.callback);
        return false;
    }

    addMoreTravelers() {
        let travelers = this.state.travelers;
        travelers.push('');
        this._isMounted && this.setState({numTravelers:this.state.numTravelers + 1, travelers:travelers});
    }

    render() {
        const children = [];

        for (var i = 0; i < this.state.numTravelers; i++) {
            children.push(<FormDynamicLabel key={i} name="travelers" index={i} title="Traveler" required={false} handleChange={this.handleDynamicTravelersChange}/>);
        }
        return (
            <form onSubmit={this.handleSubmit}>
                <div className="form-group">
                    <div className="row">
                        <div className="col">
                            <FormLabel value={this.state.source} name="source" title="Booking Source" required={false} handleChange={this.handleChange}/>
                        </div>
                        <div className="col">
                            <FormLabel value={this.state.confirmation_no} name="confirmation_no" title="Confirmation No" required={true} handleChange={this.handleChange}/>
                        </div>
                    </div>
                    <div className="row">
                        <div className="col">
                            <FormLabel value={this.state.first_name} name="first_name" title="First Name" required={false} handleChange={this.handleChange}/>
                        </div>
                        <div className="col">
                            <FormLabel value={this.state.last_name} name="last_name" title="Last Name" required={false} handleChange={this.handleChange}/>
                        </div>
                    </div>
                    <div className="row">
                        <div className="col">
                            <FormLabel value={this.state.airline} name="airline" title="Airline" required={true} handleChange={this.handleChange}/>
                        </div>
                        <div className="col">
                            <FormLabel value={this.state.flight_code} name="flight_code" title="Flight Number" required={true} handleChange={this.handleChange}/>
                        </div>
                    </div>
                    <div className="row">
                        <div className="col">
                            <FormLabel value={this.state.origin_name} name="origin_name" title="Departure Airport" required={true} handleChange={this.handleChange}/>
                        </div>
                        <div className="col">
                            <FormLabel value={this.state.destination_name} name="destination_name" title="Arrival Airport" required={true} handleChange={this.handleChange}/>
                        </div>
                    </div>
                    <div className="row">
                        <div className="col">
                            <FormLabel value={this.state.departure_date} name="departure_date" title="Departure Date" required={true} handleChange={this.handleChange}/>
                        </div>
                        <div className="col">
                            <FormLabel value={this.state.departure_time} name="departure_time" title="Departure Time" required={true} handleChange={this.handleChange}/>
                        </div>
                    </div>
                    <div className="row">
                        <div className="col">
                            <FormLabel value={this.state.arrival_date} name="arrival_date" title="Arrival Date" required={true} handleChange={this.handleChange}/>
                        </div>
                        <div className="col">
                            <FormLabel value={this.state.arrival_time} name="arrival_time" title="Arrival Time" required={true} handleChange={this.handleChange}/>
                        </div>
                    </div>
                    <div className="row">
                        <div className="col">
                            <FormLabel value={this.state.ticket_number} name="ticket_number" title="Ticket No" required={false} handleChange={this.handleChange}/>
                        </div>
                        <div className="col">
                            {children}
                            <button className="btn btn-block" onClick={this.addMoreTravelers}>➕ Add More Travelers</button>
                        </div>
                    </div>
                    <input className="btn btn-success" type="submit" value="Submit" />
                </div>
            </form>
        );
    }
}

class NewHotelForm extends React.Component {
    _isMounted = false;
    constructor(props) {
        super(props);
        this.state = {type:'Hotel', source: '', confirmation_no: '', first_name: '', last_name: '', hotel_name: '',
        address1: '', address2: '', city_name: '', country_name: '', postal_code: '', lat: '', lon: '',
        checkin_date: '', checkout_date: '', time_zone_id: '', number_of_rooms: '', created_date: '', created_time: '',
        travelers: [], numTravelers: 1};
        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.addMoreTravelers = this.addMoreTravelers.bind(this);
        this.handleDynamicTravelersChange = this.handleDynamicTravelersChange.bind(this);
        this.callback = this.callback.bind(this);
    }

    componentDidMount() {
        this._isMounted = true;
    }

    componentWillUnmount() {
        this._isMounted = false;
    }

    handleChange(newState) {
        this._isMounted && this.setState(newState);
    }

    handleDynamicTravelersChange(newState, index) {
        let travelers = this.state.travelers;
        travelers[index] = newState;
        this._isMounted && this.setState({travelers:travelers});
    }

    callback (lat_lon) {
        if(lat_lon.length === 0) {
            alert("Address not found. Please update city name, country name or postal code.");
        } else if(confirm("You sure want to submit the booking?")) {
            let jsonState = this.state;
            jsonState.created = this.state.created_date + 'T' + this.state.created_time;
            jsonState.travelers = this.state.travelers.join(',');
            //Dont take latitude and longitude and timezoneid input from user. Next iteration try to get it from google Maps
            jsonState.time_zone_id = '';
            jsonState.lat = lat_lon[0].lat;
            jsonState.lon = lat_lon[0].lon;
            submitBooking(jsonState);
            alert("Booking Submitted! Sync Again to view new itineraries.");
            refreshPage();
        }
    }

    handleSubmit (event) {
        event.preventDefault();
        setMultipleGeometryLocation([[this.state.city_name, this.state.country_name, this.state.postal_code].join(" ")], this.callback);
        return false;
    }

    addMoreTravelers() {
        let travelers = this.state.travelers;
        travelers.push('');
        this._isMounted && this.setState({numTravelers:this.state.numTravelers + 1, travelers:travelers});
    }
    render() {
        const children = [];

        for (var i = 0; i < this.state.numTravelers; i++) {
            children.push(<FormDynamicLabel key={i} name="travelers" index={i} title="Traveler" required={false} handleChange={this.handleDynamicTravelersChange}/>);
        }
        return (
            <form onSubmit={this.handleSubmit}>
                <div className="form-group">
                    <div className="row">
                        <div className="col">
                            <FormLabel name="source" title="Booking Source" required={false} handleChange={this.handleChange}/>
                        </div>
                        <div className="col">
                            <FormLabel name="confirmation_no" title="Confirmation No" required={true} handleChange={this.handleChange}/>
                        </div>
                    </div>
                    <div className="row">
                        <div className="col">
                            <FormLabel name="first_name" title="First Name" required={false} handleChange={this.handleChange}/>
                        </div>
                        <div className="col">
                            <FormLabel name="last_name" title="Last Name" required={false} handleChange={this.handleChange}/>
                        </div>
                    </div>
                    <div className="row">
                        <div className="col">
                            <FormLabel name="hotel_name" title="Hotel Name" required={true} handleChange={this.handleChange}/>
                        </div>
                        <div className="col">
                            <FormLabel name="address1" title="Address Line 1" required={true} handleChange={this.handleChange}/>
                        </div>
                    </div>
                    <div className="row">
                        <div className="col">
                            <FormLabel name="address2" title="Address Line 2" required={false} handleChange={this.handleChange}/>
                        </div>
                        <div className="col">
                            <FormLabel name="city_name" title="City" required={true} handleChange={this.handleChange}/>
                        </div>
                    </div>
                    <div className="row">
                        <div className="col">
                            <FormLabel name="country" title="Country" required={true} handleChange={this.handleChange}/>
                        </div>
                        <div className="col">
                            <FormLabel name="postal_code" title="Postal Code" required={false} handleChange={this.handleChange}/>
                        </div>
                    </div>
                    <div className="row">
                        <div className="col">
                            <FormLabel name="checkin_date" title="Checkin Date" required={true} handleChange={this.handleChange}/>
                        </div>
                        <div className="col">
                            <FormLabel name="checkout_date" title="Checkout Date" required={true} handleChange={this.handleChange}/>
                        </div>
                    </div>
                    <div className="row">
                        <div className="col">
                            <FormLabel name="number_of_rooms" title="Number of Rooms" required={false} handleChange={this.handleChange}/>
                        </div>
                        <div className="col">
                            <FormLabel name="created_date" title="Booking Date" required={false} handleChange={this.handleChange}/>
                        </div>
                    </div>
                    <div className="row">
                        <div className="col">
                            <FormLabel name="created_time" title="Booking Time" required={false} handleChange={this.handleChange}/>
                        </div>
                        <div className="col">
                            {children}
                            <button className="btn btn-block" onClick={this.addMoreTravelers}>➕ Add More Travelers</button>
                        </div>
                    </div>
                    <input className="btn btn-success" type="submit" value="Submit" />
                </div>
            </form>
        );
    }
}