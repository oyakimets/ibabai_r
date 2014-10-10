package com.android.ibabairetail.proto;

import org.apache.http.client.methods.HttpPost;

public class DeleteWithBody extends HttpPost {
	public DeleteWithBody(String url) {
		super(url);
	}
	@Override
	public String getMethod() {
		return "DELETE";
	}

}
