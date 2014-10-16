package com.android.ibabairetail.proto;

import com.google.android.gms.location.Geofence;

public class SimpleGeofence {
	private final String m_id;
	private final double m_latitude;
	private final double m_longitude;
	private final float m_radius;
	private long m_expiry;
	private int m_transition;
	
	public SimpleGeofence(String geofenceId, double latitude, double longitude, float radius, long expiration, int transition) {
		this.m_id = geofenceId;
		this.m_latitude = latitude;
		this.m_longitude = longitude;
		this.m_radius = radius;
		this.m_expiry = expiration;
		this.m_transition = transition;
	}
	public String getId() {
		return m_id;
	}
	public double getLatitude() {
		return m_latitude;
	}
	public double getLongitude() {
		return m_longitude;
	}
	public float getRadius() {
		return m_radius;
	}
	public long getExpiry() {
		return m_expiry;
	}
	public int getTransition() {
		return m_transition;
	}
	
	public Geofence toGeoFence() {
		return new Geofence.Builder().setRequestId(getId()).setTransitionTypes(m_transition).setCircularRegion(getLatitude(), getLongitude(), getRadius()).setExpirationDuration(m_expiry).build();
	}
}
