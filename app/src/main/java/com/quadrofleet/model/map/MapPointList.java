package com.quadrofleet.model.map;

import java.util.List;

public class MapPointList {

    private List<MapPoint> points;

    public List<MapPoint> getPoints() {
        return points;
    }

    public void setPoints(List<MapPoint> points) {
        this.points = points;
    }

    @Override
    public String toString() {
        return "MapPointList{" +
                "points=" + points +
                '}';
    }

}
