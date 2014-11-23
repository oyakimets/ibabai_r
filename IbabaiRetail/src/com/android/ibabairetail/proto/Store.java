package com.android.ibabairetail.proto;

import org.json.JSONObject;

public class Store {
	JSONObject store = null;
	
	public Store(JSONObject store) {
		this.store=store;
	}
	public int getStoreId() {
		return(store.optInt("store_id"));
	}
	public double getLat() {
		return(store.optDouble("latitude"));
	}
	public double getLon() {
		return(store.optDouble("longitude"));
	}
	public int getStoreRadius() {
		return(store.optInt("radius"));
	}
	public int getClientId() {
		return(store.optInt("client_id"));
	}
	public String getClientName() {
		return(store.optString("client_name"));
	}
	public int getCatInd() {
		return(store.optInt("category"));
	}
	public String getAddress() {
		return(store.optString("address"));
	}
}
