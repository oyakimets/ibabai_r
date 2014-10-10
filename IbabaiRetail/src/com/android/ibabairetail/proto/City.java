package com.android.ibabairetail.proto;

import org.json.JSONObject;

public class City {
	JSONObject city = null;
	
	public City(JSONObject city) {
		this.city=city;
	}
	public int getCityId() {
		return(city.optInt("city_id"));
	}
	public int getCityRadius() {
		return(city.optInt("radius"));
	}
	public double getCityLat() {
		return(city.optDouble("latitude"));
	}
	public double getCityLon() {
		return(city.optDouble("longitude"));
	}
}
