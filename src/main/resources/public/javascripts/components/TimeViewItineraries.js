var colourCodes = ["#0000FF", "#CD5C5C", "#229954", "#F4D03F", "#9B59B6", "#515A5A", "#117A65", "#6E2C00", "#154360", "#A93226"];


function getNearbyRestaurants(show_button, markers, map, place) {
    console.log("State: " + show_button);

    var request = {
        location: place,
        rankBy: google.maps.places.RankBy.DISTANCE,
        type: ['restaurant']
    };

    function setMapOnAll(map) {
        for (var i = 0; i < markers.length; i++) {
            markers[i].setMap(map);
        }
    }

    function clearMarkers() {
        setMapOnAll(null);
        markers = [];
    }

    function createMarkers(place) {
        var marker = new google.maps.Marker({
            position: place.geometry.location,
            map: map,
            title: place.name
        });
        markers.push(marker);
    }

    service = new google.maps.places.PlacesService(map);
    service.nearbySearch(request, nearbyCallback);

    function nearbyCallback(results, status) {
        if (status == google.maps.places.PlacesServiceStatus.OK) {
            if (!show_button){
                clearMarkers();
            }
            else {
                var bounds = new google.maps.LatLngBounds();
                for (var i = 0; i < 10; i++) {
                    createMarkers(results[i]);
                    bounds.extend(results[i].geometry.location);
                }
                setMapOnAll(map);
                map.fitBounds(bounds);
            }
        }
    }



}

function getNearbyPlaces(show_button, markers, map, place) {
    var request = {
        location: place,
        //radius: '500',
        rankBy: google.maps.places.RankBy.DISTANCE,
        type: ['tourist_attraction']
    };

    service = new google.maps.places.PlacesService(map);
    service.nearbySearch(request, nearbyCallback);

    function clearMarkers() {
        for (var i = 0; i < markers.length; i++) {
            markers[i].setMap(null);
        }
        markers = [];
    }

    function nearbyCallback(results, status) {
        if (status == google.maps.places.PlacesServiceStatus.OK) {
            //
            if (!show_button){
                clearMarkers();
            }
            else{
            createMarkers(results);
            map.setCenter(results[0].geometry.location);
            }
        }
    }

    function createMarkers(places) {
        var image = {
            url: 'http://maps.google.com/mapfiles/kml/paddle/blu-stars.png',
            scaledSize: new google.maps.Size(30, 30),
            // The origin for this image is (0, 0).
            origin: new google.maps.Point(0, 0),
        };

        var bounds = new google.maps.LatLngBounds();
        places.map(place => {
            var marker = new google.maps.Marker({
                position: place.geometry.location,
                map: map,
                icon: image,
                title: place.name,
            });
            bounds.extend(place.geometry.location);
            markers.push(marker);
        });
        map.fitBounds(bounds);
    }
    //

}


function createAirMarker(map, latlngs) {
    var image = {
        url: 'http://maps.google.com/mapfiles/kml/pal2/icon48.png',
        scaledSize: new google.maps.Size(20, 30),
        // The origin for this image is (0, 0).
        origin: new google.maps.Point(0, 0),
    };
    latlngs.map(latlng => {
        new google.maps.Marker({
            position: latlng,
            map: map,
            icon: image,
        });
    }
    );
    //var bounds = new google.maps.LatLngBounds(latlngs);
    //map.fitBounds(bounds);
}

function createHotelMarker(map, latlngs, nameHotel) {
    var image = {
        url: 'http://maps.google.com/mapfiles/kml/shapes/lodging.png',
        scaledSize: new google.maps.Size(30, 30),
        origin: new google.maps.Point(0, 0),
    };

    var marker = new google.maps.Marker({
        position: latlngs,
        map: map,
        icon: image,
        title: nameHotel });

    return marker;
}

function createLine(map, latlngs, mapColor) {
    var ArrowSymbol = {
        path: google.maps.SymbolPath.FORWARD_CLOSED_ARROW,
        strokeOpacity: 1,
        scale: 4,
    };

    var lineSymbol = {
        path: 'M 0,-1 0,1',
        strokeOpacity: 1,
        scale: 4
    };

    var Path = new google.maps.Polyline({
        path: latlngs,
        geodesic: true,
        strokeColor: mapColor,
        map: map,
        icons: [
            {
                icon: ArrowSymbol,
                offset: '100%'
            }
        ],

    });
}

class SyncEmailButton extends React.Component {
    handleClick() {
        syncBookings();
    }

    render() {
        return <button className="btn float-left text-white" onClick={() => { this.handleClick(); }}>Sync</button>;
    }
}

class RecommendationButton extends React.Component {

    constructor(props) {
        super(props);
        this.state = { markers: [], showRestaurantsRecommendations: false};
    }

    componentDidMount() {
        this._isMounted = true;

    }
    componentWillUnmount() {
        this._isMounted = false;
    }

    handleClick() {
        //console.log("At handleclick"+ this.state.showRecommendations);
        this.setState(
            {showRestaurantsRecommendations: !this.state.showRestaurantsRecommendations},
            () => {
                getNearbyRestaurants(this.state.showRestaurantsRecommendations, this.state.markers, this.props.map, this.props.place);
            })
    }

    render() {
        return <button type="button" className="btn btn-info" onClick={() => { this.handleClick(); }}>
            {this.state.showRestaurantsRecommendations? "Hide": "Show"} Restaurants Recommendation</button>;
    }
}

class TouristRecommendationButton extends React.Component {

    constructor(props) {
        super(props);
        this.state = { markers: [], showRecommendations: false};
    }

    componentDidMount() {
        this._isMounted = true;

    }
    componentWillUnmount() {
        this._isMounted = false;
    }

    handleClick() {
        //console.log("At handleclick"+ this.state.showRecommendations);
        this.setState(
            {showRecommendations: !this.state.showRecommendations},
            () => {
                getNearbyPlaces(this.state.showRecommendations, this.state.markers, this.props.map, this.props.place);
            })
    }

    render() {
        return <button type="button" className="btn btn-outline-danger" onClick={ this.handleClick.bind(this)}>
            {this.state.showRecommendations? "Hide": "Show"} Tourist Spots Around</button>;
    }
}

class ItineraryList extends React.Component {
    _isMounted = false;
    constructor(props) {
        super(props);
        this.state = { itineraries: [], airDiscrepancies: [], hotelDiscrepancies: [] };
    }

    async getDataFromServer() {
        this._isMounted && this.setState({
            itineraries: await (await fetch("/temp", {method: "GET"})).json(),
            airDiscrepancies: await (await fetch("/airdiscrepancies", {method: "GET"})).json(),
            hotelDiscrepancies: await (await fetch("/hoteldiscrepancies", {method: "GET"})).json()
        });
        this._isMounted && window.setTimeout(() => { this._isMounted && this.getDataFromServer(); }, 10000);
    }

    componentWillUnmount() {
        this._isMounted = false;
    }

    componentDidMount() {
        this._isMounted = true;
        this._isMounted && this.getDataFromServer();
    }

    render() {

        return (
            <div className="itinerary-list">
                {this.state.itineraries.map((itinerary, index) => <Itinerary key={index} index={index} itinerary={itinerary}
                     airDiscrepancy={this.state.airDiscrepancies.filter((airDiscrepancy, index) =>
                         airDiscrepancy.itineraryIdentifiers.includes(itinerary.identifier))}
                     hotelDiscrepancy={this.state.hotelDiscrepancies.filter((hotelDiscrepancy, index) =>
                         hotelDiscrepancy.itineraryIdentifiers.includes(itinerary.identifier))}/>)}
            </div>
        );
    }
}



class Itinerary extends React.Component {
    _isMounted = false;
    constructor(props) {
        super(props);
        this.state = {map:null};
    }
    componentDidMount() {
        this._isMounted = true;
        let mapID = "map"+ this.props.index;
        this._isMounted &&
            this.setState({map: new google.maps.Map(document.getElementById(mapID), {
                center: {lat: 39.2904, lng: -76.6122},
                zoom: 4
            })});
    }
    componentWillUnmount() {
        this._isMounted = false;
    }
    render() {
        const mapStyle = {
            height: '400px',
            borderStyle: 'solid',
            borderWidth: '2px',
            borderColor: 'black'
        };

        let mapColor = colourCodes[this.props.index%10];
        let index_last_bookings = this.props.itinerary.bookings.length-1;
        let destination_names = [];
        this.props.itinerary.bookings.map((booking, index) => {
            destination_names.push(booking._type==="Air"? booking._destination_city : booking.city_name);
        });
        let indexOfLastBooking = this.props.itinerary.bookings.length-1;
        var startDate = this.props.itinerary.bookings[0]._type==="Air"?new Date(this.props.itinerary.bookings[0]._departure_datetime):
            new Date(this.props.itinerary.bookings[0].checkin_date);
        var endDate = this.props.itinerary.bookings[indexOfLastBooking]._type==="Air"?new Date(this.props.itinerary.bookings[indexOfLastBooking]._arrival_datetime):
            new Date(this.props.itinerary.bookings[indexOfLastBooking].checkout_date);
        return (
            <div className="container" style={{height:'400px'}}>
                <div className="row">
                    <div className="col bg-light">
                        <div id="itinerary" className="card overflow-auto bg-light" style={{height:'400px'}}>
                            <div id="header" className="card-title">
                                <WarningDiscrepancy msg={"Trip for "+destination_names.join(' ')+ " from "+ startDate.toDateString()+" to "+endDate.toDateString()} airDiscrepancy={this.props.airDiscrepancy} hotelDiscrepancy={this.props.hotelDiscrepancy}/>
                            </div>
                            <div className="card-body">
                                {this.props.itinerary.bookings.map((booking, index) => <Booking  key={index} index= {index} map={this.state.map} mapColor={mapColor} last={index===index_last_bookings} booking={booking} itinerary_id={this.props.itinerary.identifier}/>)}
                            </div>
                        </div>
                    </div>
                    <div id={"map"+this.props.index} style={mapStyle} className="col">
                    </div>
                </div>

            </div>
        );
    }
}

class Booking extends React.Component {
    _isMounted = false;
    constructor(props) {
        super(props);
        this.state = {
            MouseOver: false,
        }
    }

    componentDidMount() {
        this._isMounted = true;
    }

    componentWillUnmount() {
        this._isMounted = false;
    }

    mouseOverEvent() {
        this._isMounted && this.setState({MouseOver:true})
    }

    mouseLeaveEvent() {
        this._isMounted && this.setState({MouseOver:false})
    }

    handleClick() {
        if (confirm("You sure want to delete the booking?")) {
            fetch("/itineraries/delete/" + this.props.itinerary_id + "/" + this.props.booking._type + "-" + this.props.booking._booking_identifier + "/", {method: "PUT"});
        }
    }

    render() {
        if (this.props.booking._type==="Air") {
            var myLatLng = [{lat: Number(this.props.booking._origin_lat), lng: Number(this.props.booking._origin_lon)}];
            myLatLng.push({lat: Number(this.props.booking._destination_lat), lng: Number(this.props.booking._destination_lon)});

            createAirMarker(this.props.map, myLatLng);
            createLine(this.props.map, myLatLng, this.props.mapColor);

        } else if (this.props.booking._type==="Hotel") {
            var hotelLatLng = {lat: Number(this.props.booking.lat), lng: Number(this.props.booking.lon)};
            createHotelMarker(this.props.map, hotelLatLng, this.props.booking.hotel_name);

        }
        return (
            <div id="booking" className="container" onMouseOver={this.mouseOverEvent.bind(this)} onMouseLeave={this.mouseLeaveEvent.bind(this)}>
                <div className="row">
                    <div className="col-11">
                        {this.props.booking._type==="Air"?
                            "Flight from "+this.props.booking._origin_airport+" to "+this.props.booking._destination_airport:
                            "Stay at "+ this.props.booking.hotel_name+ " from "+this.props.booking.checkin_date+" to "+this.props.booking.checkout_date}
                        <DetailBox map={this.props.map} place= {hotelLatLng} type={this.props.booking._type} booking={this.props.booking}/>
                    </div>
                    <div className="col">
                        <a id="delete-icon" className="close" onClick={this.handleClick.bind(this)}>×</a>
                    </div>

                </div>
            </div>
        );
    }
}

class DetailBox extends React.Component {
    render () {
        return (
            <div id="detail-box">
                {this.props.type==="Air"?<AirDetail booking={this.props.booking}/>:
                    <HotelDetail map={this.props.map} place={this.props.place}  booking={this.props.booking}/>
                }
            </div>
        )
    }
}

class AirDetail extends React.Component {

    render () {
        const departure_datetime = new Date(this.props.booking._departure_datetime);
        const arrival_datetime = new Date(this.props.booking._arrival_datetime);
        return(
            <div className="container bg-light px-4">
                <div className="row bg-light text-left align-content-stretch">
                    <div className="col text-info text-left align-content-stretch">
                        <p className="m-0">Guests Name</p>
                    </div>
                    <div className="col text-left align-content-stretch">
                        <p className="m-0">{this.props.booking._travelers}</p>
                    </div>
                </div>
                <div className="row bg-white text-left align-content-stretch">
                    <div className="col text-info text-left align-content-stretch">
                        <p className="m-0">Confirmation Number</p>
                    </div>
                    <div className="col text-left align-content-stretch">
                        <p className="m-0">{this.props.booking._confirmation_no}</p>
                    </div>
                </div>
                <div className="row bg-light text-left align-content-stretch">
                    <div className="col text-info text-left align-content-stretch">
                        <p className="m-0">Ticket Number</p>
                    </div>
                    <div className="col text-left align-content-stretch">
                        <p className="m-0">{this.props.booking._ticket_number}</p>
                    </div>
                </div>
                <div className="row bg-white text-left align-content-stretch">
                    <div className="col text-info text-left align-content-stretch">
                        <p className="m-0">Flight</p>
                    </div>
                    <div className="col text-left align-content-stretch">
                        <p className="m-0">{this.props.booking._airline}</p>
                        <p className="m-0">{this.props.booking._iata_code}{this.props.booking._flight_number}</p>
                    </div>
                </div>
                <div className="row bg-light text-left align-content-stretch">
                    <div className="col text-info text-left align-content-stretch">
                        <p className="m-0">From</p>
                    </div>
                    <div className="col text-left align-content-stretch">
                        <p className="m-0">{this.props.booking._origin_airport}</p>
                        <p className="m-0">{this.props.booking._origin_city}, {this.props.booking._origin_country}</p>
                        <p className="m-0">{departure_datetime.toUTCString()}</p>
                    </div>
                </div>
                <div className="row bg-white text-left align-content-stretch">
                    <div className="col text-info text-left align-content-stretch">
                        <p className="m-0">To</p>
                    </div>
                    <div className="col text-left align-content-stretch">
                        <p className="m-0">{this.props.booking._destination_airport}</p>
                        <p className="m-0">{this.props.booking._destination_city}, {this.props.booking._destination_country}</p>
                        <p className="m-0">{arrival_datetime.toUTCString()}</p>
                    </div>
                </div>
            </div>
        );
    }
}

class HotelDetail extends React.Component {
    render () {
        const monthNames = ["January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        ];
        var checkin_date = this.props.booking.checkin_date.split("-");
        checkin_date = monthNames[Number(checkin_date[1])-1] + " " + checkin_date[2] + ", " + checkin_date[0];
        var checkout_date = this.props.booking.checkout_date.split("-");
        checkout_date = monthNames[Number(checkout_date[1])-1] + " " + checkout_date[2] + ", " + checkout_date[0];
        return(

            <div className="container bg-light px-4">
                <div className="row bg-light text-left align-content-stretch">
                    <div className="col text-info text-left align-content-stretch">
                        <p className="m-0">Guests Name</p>
                    </div>
                    <div className="col text-left align-content-stretch">
                        <p className="m-0">{this.props.booking._travelers}</p>
                    </div>
                </div>
                <div className="row bg-white text-left align-content-stretch">
                    <div className="col text-info text-left align-content-stretch">
                        <p className="m-0">Number of Rooms</p>
                    </div>
                    <div className="col text-left align-content-stretch">
                        <p className="m-0">{this.props.booking.number_of_rooms}</p>
                    </div>
                </div>
                <div className="row bg-light text-left align-content-stretch">
                    <div className="col text-info text-left align-content-stretch">
                        <p className="m-0">Confirmation Number</p>
                    </div>
                    <div className="col text-left align-content-stretch">
                        <p className="m-0">{this.props.booking._confirmation_no}</p>
                    </div>
                </div>
                <div className="row bg-white text-left align-content-stretch">
                    <div className="col text-info text-left align-content-stretch">
                        <p className="m-0">Hotel</p>
                    </div>
                    <div className="col text-left align-content-stretch">
                        <p className="m-0">{this.props.booking.hotel_name}</p>
                        <p className="m-0">{this.props.booking.address1}</p>
                        <p className="m-0">{this.props.booking.address2}</p>
                        <p className="m-0">{this.props.booking.city_name}</p>
                        <p className="m-0">{this.props.booking.country}</p>
                        <p className="m-0">{this.props.booking.postal_code}</p>
                    </div>
                </div>
                <div className="row bg-light text-left align-content-stretch">
                    <div className="col text-info text-left align-content-stretch">
                        <p className="m-0">From</p>
                    </div>
                    <div className="col text-left align-content-stretch">
                        <p className="m-0">{checkin_date}</p>
                    </div>
                </div>
                <div className="row bg-white text-left align-content-stretch">
                    <div className="col text-info text-left align-content-stretch">
                        <p className="m-0">To</p>
                    </div>
                    <div className="col text-left align-content-stretch">
                        <p className="m-0">{checkout_date}</p>
                    </div>
                </div>
            <div>
                <RecommendationButton map={this.props.map} place={this.props.place}  />
                <TouristRecommendationButton map={this.props.map} place={this.props.place}  />
            </div>
            </div>

        );
    }
}

class WarningDiscrepancy extends React.Component {

    handleClick() {
        var msg = "";
        this.props.airDiscrepancy.map((airDiscrepancy,index) => {
            msg = msg.concat("You may have missed booking flight");
            if(airDiscrepancy.fromCity !== "N/A") {
                msg = msg.concat(" from "+airDiscrepancy.fromCity);
            }
            if(airDiscrepancy.toCity !== "N/A") {
                msg = msg.concat(" to "+airDiscrepancy.toCity);
            }
            msg = msg.concat("\n");
        });

        this.props.hotelDiscrepancy.map((hotelDiscrepancy,index) => {
            msg = msg.concat("You may have missed booking stay");
            if(hotelDiscrepancy.city !== "N/A") {
                msg = msg.concat(" at "+hotelDiscrepancy.city);
            }
            msg = msg.concat("\n");
        });

        if (msg !== "") {
            alert(msg);
        }
    }

    render () {
        return (
            <div>
                {this.props.msg+" "}
                {(this.props.airDiscrepancy.length+this.props.hotelDiscrepancy.length)>0?
                    <button onClick={this.handleClick.bind(this)}>❗</button>:""}
                <p/>
            </div>
        );
    }
}