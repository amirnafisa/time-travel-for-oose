let mapDiv = document.createElement('mapDiv');
var service;

window.onload = function () {
    console.log("Window is loaded");
    service = new google.maps.places.PlacesService(mapDiv);
};

function getPlaceID (query, callback) {
    if (!service) {
        service = new google.maps.places.PlacesService(mapDiv);
    }
    service.findPlaceFromQuery({query:query,fields:['place_id']}, function(results, status) {
        if (status === google.maps.places.PlacesServiceStatus.OK) {
           callback(results[0].place_id);
        } else {
            callback(null);
        }
    });
}


function setDetails(placeID, callback) {
    if (!service) {
        service = new google.maps.places.PlacesService(mapDiv);
    }
    service.getDetails({placeId:placeID, fields:['name', 'address_components','geometry']}, function(place, status) {
        if (status === google.maps.places.PlacesServiceStatus.OK) {
            let placeDetails = {};
            placeDetails.lat = place.geometry.location.lat();
            placeDetails.lon = place.geometry.location.lng();
            placeDetails.city = '';
            for (var comp of place.address_components) {
                if (comp.types[0] === "locality") {
                    placeDetails.city = placeDetails.city.concat(" ",comp.long_name);
                } else if(comp.types[0] === "administrative_area_level_1") {
                    placeDetails.city = placeDetails.city.concat(" ",comp.long_name);
                } else if(comp.types[0] === "administrative_area_level_2") {
                    placeDetails.city = placeDetails.city.concat(" ",comp.long_name);
                } else if(comp.types[0] === "sublocality_level_1") {
                    placeDetails.city = placeDetails.city.concat(" ",comp.long_name);
                } else if(comp.types[0] === "country") {
                    placeDetails.country = comp.long_name;
                }
            }
            callback(placeDetails);
        }
    });
}

function setMultipleGeometryLocation(queries, callback) {
    let lat_lon = [];
    for (var query of queries) {

        getPlaceID(query, function (placeID) {
            if(placeID) {
                setDetails(placeID, function (result) {
                    lat_lon.push(result);
                    if (lat_lon.length === queries.length) {
                        callback(lat_lon);
                    }
                });
            } else {
                callback([]);
                return;
            }
        });
    }
}