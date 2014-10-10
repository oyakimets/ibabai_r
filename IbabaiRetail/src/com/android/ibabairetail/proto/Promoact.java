package com.android.ibabairetail.proto;

import org.json.JSONObject;

public class Promoact {
	JSONObject pa=null;
	
	public Promoact(JSONObject pa) {
		this.pa=pa;
	}
	public int getPromoId() {
		return(pa.optInt("promoact_id"));
	}
	public int getClientId() {
		return(pa.optInt("client_id"));
	}
	public String getClientName() {
		return(pa.optString("client_name"));
	}
	public String getBarcode() {
		return(pa.optString("barcode"));
	}
	public int getRew1() {
		return(pa.optInt("rew1"));
	}
	public int getRew2() {
		return(pa.optInt("rew2"));
	}
	public int getMult() {
		return(pa.optInt("multiple"));
	}
}
