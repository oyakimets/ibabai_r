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
	public int getDiscount() {
		return(pa.optInt("discount"));
	}
	public int getCatInd() {
		return(pa.optInt("category"));
	}	
}
