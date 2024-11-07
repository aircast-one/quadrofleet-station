// WebSocket
let lastLatitude = 48.132582;
let lastLongitude = 11.581609;

function toggleTable(tableId) {
    const table = document.querySelector(`.telemetry-table[data-table="${tableId}"]`);
    const rows = table.querySelectorAll('.data-row');
    rows.forEach(row => {
        row.classList.toggle('hidden');
    });
}

function fetchTelemetryData() {
    fetch('/telemetry')
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(data => {
            parseFlightStatus(data);
        })
        .catch(error => {
            console.error('There was a problem with the fetch operation:', error);
        });
}

function updateFuelRotation(value) {
    const gElement = document.getElementById('fuel-value');

    if (gElement === null) {
        return;
    }

    let transform = gElement.getAttribute('transform');

    const newAngle = value * 2;
    transform = transform.replace(/rotate\([-+]?[0-9]*\.?[0-9]+\)/, 'rotate(' + newAngle + ')');

    gElement.setAttribute('transform', transform);
}

function updateRollRotation(value) {
    const gElement = document.getElementById('roll-value');

    if (gElement === null) {
        return;
    }

    let transform = gElement.getAttribute('transform');

    const newAngle = value * 2;
    transform = transform.replace(/rotate\([-+]?[0-9]*\.?[0-9]+\)/, 'rotate(' + newAngle + ')');

    gElement.setAttribute('transform', transform);
}

function updatePitchValue(value) {
    const gElement = document.getElementById('pitch-value');

    if (gElement === null) {
        return;
    }

    let transform = gElement.getAttribute('transform');

    const newValue = Math.round(value * 0.6 * -1);
    if (transform.includes('translate(')) {
        transform = transform.replace(/translate\([-\d.]+\s[-\d.]+\)/, `translate(0 ${newValue})`);
    } else {
        transform += ` translate(0 ${newValue})`;
    }

    gElement.setAttribute('transform', transform);
}

function updateMap(latitude, longitude) {
    if (latitude !== lastLatitude && longitude !== lastLongitude) {
        lastLatitude = latitude;
        lastLongitude = longitude;

        map.getView().setCenter(ol.proj.fromLonLat([longitude, latitude]));
    }
}

function parseFlightStatus(telemetryData) {
    document.getElementById('armed') && (document.getElementById('armed').textContent = telemetryData.armed);
    document.getElementById('angleMode') && (document.getElementById('angleMode').textContent = telemetryData.angleMode);
    document.getElementById('altHoldMode') && (document.getElementById('altHoldMode').textContent = telemetryData.altHoldMode);
    document.getElementById('flightMode') && (document.getElementById('flightMode').textContent = telemetryData.flightMode);
    document.getElementById('pitch') && (document.getElementById('pitch').textContent = telemetryData.pitch);
    document.getElementById('roll') && (document.getElementById('roll').textContent = telemetryData.roll);
    document.getElementById('yaw') && (document.getElementById('yaw').textContent = telemetryData.yaw);
    document.getElementById('throttle') && (document.getElementById('throttle').textContent = telemetryData.throttle);

    document.getElementById('voltage') && (document.getElementById('voltage').textContent = telemetryData.voltage);
    document.getElementById('voltage-value') && (document.getElementById('voltage-value').textContent = telemetryData.voltage + " V");
    document.getElementById('current') && (document.getElementById('current').textContent = telemetryData.current);
    document.getElementById('fuel') && (document.getElementById('fuel').textContent = telemetryData.fuel);
    document.getElementById('remaining') && (document.getElementById('remaining').textContent = telemetryData.remaining);

    document.getElementById('latitude') && (document.getElementById('latitude').textContent = telemetryData.latitude);
    document.getElementById('longitude') && (document.getElementById('longitude').textContent = telemetryData.longitude);
    document.getElementById('groundSpeed') && (document.getElementById('groundSpeed').textContent = telemetryData.groundSpeed);
    document.getElementById('heading') && (document.getElementById('heading').textContent = telemetryData.heading);
    document.getElementById('altitude') && (document.getElementById('altitude').textContent = telemetryData.altitude);
    document.getElementById('satellites') && (document.getElementById('satellites').textContent = telemetryData.satellites);

    // Update map
    updateMap(telemetryData.latitude, telemetryData.longitude);

    updateFuelRotation(telemetryData.remaining);
    updateRollRotation(telemetryData.roll);
    updatePitchValue(telemetryData.pitch);
}

setInterval(fetchTelemetryData, 20);

const map = new ol.Map({
    target: 'map',
    layers: [
        new ol.layer.Tile({
            source: new ol.source.OSM()
        })
    ],
    view: new ol.View({
        center: ol.proj.fromLonLat([lastLatitude, lastLongitude]),
        zoom: 15
    })
});
