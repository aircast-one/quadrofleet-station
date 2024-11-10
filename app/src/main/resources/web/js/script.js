let lastLatitude = 0;
let lastLongitude = 0;

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

function updateHeadingRotation(value) {
    const gElement = document.getElementById('heading-value');

    if (gElement === null) {
        return;
    }

    let transform = gElement.getAttribute('transform');

    transform = transform.replace(/rotate\([-+]?[0-9]*\.?[0-9]+\)/, 'rotate(' + value * -1 + ')');

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
    if (latitude !== 0 && longitude !== 0) {
        map.getView().setCenter(ol.proj.fromLonLat([longitude, latitude]));
    }

    if (latitude !== lastLatitude && longitude !== lastLongitude) {
        lastLatitude = latitude;
        lastLongitude = longitude;

        iconFeature.getGeometry().setCoordinates(ol.proj.fromLonLat([longitude, latitude]));
    }
}

function addIconAtLocation(coordinate, type = "T") {
    const iconFeature = new ol.Feature({
        geometry: new ol.geom.Point(ol.proj.fromLonLat(coordinate)),
        type: type,
    });

    iconFeature.setStyle(type === 'T' ? iconStyleTarget : iconStyleHome);

    iconLayer.getSource().addFeature(iconFeature);
}

function getAllFeaturesInfo() {
    const featuresInfo = [];

    iconLayer.getSource().getFeatures().forEach((feature) => {
        const coordinate = ol.proj.toLonLat(feature.getGeometry().getCoordinates());
        const type = feature.get('type');

        featuresInfo.push({
            coordinate: coordinate,
            type: type,
        });
    });

    return featuresInfo;
}

function sendPointList(list) {
    const payload = {points: list};

    fetch('/map-points', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(data => {
            console.log('Success:', data);
        })
        .catch(error => {
            console.error('There was a problem with the fetch operation:', error);
        });
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
    updateHeadingRotation(telemetryData.heading);
    updatePitchValue(telemetryData.pitch);
}

setInterval(fetchTelemetryData, 20);

const iconFeature = new ol.Feature({
    geometry: new ol.geom.Point([0, 0]),
});

const iconStyleDrone = new ol.style.Style({
    image: new ol.style.Icon({
        anchor: [0.5, 46],
        anchorXUnits: 'fraction',
        anchorYUnits: 'pixels',
        src: 'img/drone-icon.png',
    }),
});

const iconStyleTarget = new ol.style.Style({
    image: new ol.style.Icon({
        anchor: [0.5, 46],
        anchorXUnits: 'fraction',
        anchorYUnits: 'pixels',
        src: 'img/target-icon.png',
    }),
});

const iconStyleHome = new ol.style.Style({
    image: new ol.style.Icon({
        anchor: [0.5, 46],
        anchorXUnits: 'fraction',
        anchorYUnits: 'pixels',
        src: 'img/home-icon.png',
    }),
});

iconFeature.setStyle(iconStyleDrone);

const vectorMarkerSource = new ol.source.Vector({
    features: [iconFeature],
});

const vectorMarkerGroup = new ol.layer.Vector({
    source: vectorMarkerSource
});

const iconLayer = new ol.layer.Vector({
    source: new ol.source.Vector(),
});

const map = new ol.Map({
    target: 'map',
    controls: [],
    layers: [
        new ol.layer.Tile({
            source: new ol.source.OSM()
        }), vectorMarkerGroup, iconLayer
    ],
    view: new ol.View({
        center: ol.proj.fromLonLat([lastLatitude, lastLongitude]),
        zoom: 15
    })
});

map.on('singleclick', function (event) {
    let featureFound = false;

    map.forEachFeatureAtPixel(event.pixel, function (feature, layer) {
        if (layer === iconLayer) {
            featureFound = true;

            if (feature.get('type') === 'T') {
                feature.setStyle(iconStyleHome);
                feature.set('type', 'H');

                sendPointList(getAllFeaturesInfo());
            } else {
                iconLayer.getSource().removeFeature(feature);

                sendPointList(getAllFeaturesInfo());
            }

            return true;
        }
    });

    if (!featureFound) {
        const lonLat = ol.proj.toLonLat(event.coordinate);
        addIconAtLocation(lonLat);

        sendPointList(getAllFeaturesInfo());
    }
})