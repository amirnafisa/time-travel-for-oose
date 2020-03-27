function syncBookings() {
    document.body.style.cursor='wait';
    fetch("/emails", { method: "GET" })
        .then(response => {
            console.log(response.status);
            if (response.status === 201) {
                console.log("Emails Synced!");
                fetch("/bookings", { method: "GET" })
                    .then(response => {
                        if(!response.ok) {
                            document.body.style.cursor='default';
                            throw new Error ("Failed Syncing Bookings with Http Status " + response.status);
                        } else {
                            console.log("Bookings Synced");
                            fetch("/itineraries", {method: "GET"})
                                .then(response => {
                                    if(!response.ok) {
                                        document.body.style.cursor='default';
                                        throw new Error("Failed Syncing Itineraries with Http Status " + response.status);
                                    } else {
                                        console.log("Itineraries Synced");
                                        document.body.style.cursor='default';
                                    }
                                });
                        }
                    });
            } else if (response.status === 200) {
                alert("Now you will be redirected. Make sure popups are allowed. ");
                Promise.resolve(response.json()).then(value => {
                    window.open(value);
                });
                alert("Click sync again to view the itineraries after the authorization is complete and you see Close window on the other tab.");
                document.body.style.cursor='default';
            } else {
                document.body.style.cursor='default';
                throw new Error ("Response status not matched. Received Http response " + response.status);
            }

        });

}

