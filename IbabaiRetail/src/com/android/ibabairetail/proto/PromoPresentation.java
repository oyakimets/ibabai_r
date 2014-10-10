package com.android.ibabairetail.proto;

import org.json.JSONArray;
import org.json.JSONObject;

public class PromoPresentation {
	JSONObject raw = null;
	JSONArray slides;
	JSONObject client_id;
	
	PromoPresentation(JSONObject raw) {
		this.raw=raw;
		slides=raw.optJSONArray("slides");
		client_id=raw.optJSONObject("client_id");
	}
	
	int getSlidesCount() {
		return(slides.length());
	}
	String getSlideFile(int position) {
		JSONObject slide = slides.optJSONObject(position);
		return(slide.optString("file"));
	}
	String getClientId() {		
		return(client_id.optString("client_id"));
	}
}
