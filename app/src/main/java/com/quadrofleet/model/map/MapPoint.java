package com.quadrofleet.model.map;

import java.util.List;

public class MapPoint {

    private List<Double> coordinate;

    private String type;

    public List<Double> getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(List<Double> coordinate) {
        this.coordinate = coordinate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Point{" +
                "coordinate=" + coordinate +
                ", type='" + type + '\'' +
                '}';
    }

    public boolean isHome() {
        return type != null && type.equals("H");
    }

    public boolean isTarget() {
        return type != null && type.equals("T");
    }

    public Double getLongitude() {
        if (coordinate == null || coordinate.isEmpty()) {
            return 0d;
        }

        return coordinate.getFirst();
    }

    public Double getLatitude() {
        if (coordinate == null || coordinate.size() < 2) {
            return 0d;
        }

        return coordinate.get(1);
    }

}
