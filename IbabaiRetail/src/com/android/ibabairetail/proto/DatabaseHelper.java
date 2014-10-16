package com.android.ibabairetail.proto;

import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME="ibabai.db";
	private static final int SCHEMA_VERSION=1;
	static final String DATE="date";
	static final String AGENT="agent";
	static final String AMOUNT="amount";
	static final String S_ID="store_id";
	static final String C_ID="city_id";
	static final String LAT = "latitude";
	static final String LON = "longitude";
	static final String P_ID="promoact_id";
	static final String CL_ID="client_id";
	static final String CL_NAME="client_name";
	static final String CAT="category";
	static final String DISC="discount";	
	static final String STOP="stopped";
	static final String RAD = "radius";
	static final String V_ID="vendor_id";
	static final String V_NAME="vendor_name";
	static final String TYPE="type";
	static final String TABLE="logbook";
	static final String TABLE_C="cities";
	static final String TABLE_S="stores";
	static final String TABLE_P="promoacts";
	static final String TABLE_V="vendors";
	static final String TABLE_SP="promo_stores";
	private static DatabaseHelper singleton=null;
	private Context ctxt=null;
	
	synchronized static DatabaseHelper getInstance(Context ctxt) {
		if (singleton == null) {
			singleton = new DatabaseHelper(ctxt.getApplicationContext());
		}
		return (singleton);
	}
	
	private DatabaseHelper(Context ctxt) {
		super(ctxt, DATABASE_NAME, null, SCHEMA_VERSION);
		this.ctxt = ctxt;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {		
		db.execSQL("CREATE TABLE stores (_id INTEGER PRIMARY KEY, store_id INTEGER, client_id INTEGER, client_name STRING, latitude REAL, longitude REAL, radius INTEGER, category INTEGER);");
		db.execSQL("CREATE TABLE promoacts (_id INTEGER PRIMARY KEY, promoact_id INTEGER, client_id INTEGER, discount REAL DEFAULT 0.0, stopped INTEGER DEFAULT 0, category INTEGER);");		
		db.execSQL("CREATE TABLE cities (_id INTEGER PRIMARY KEY, city_id INTEGER, latitude REAL, longitude REAL, radius INTEGER);");
		db.execSQL("CREATE TABLE promo_stores (_id INTEGER PRIMARY KEY, store_id INTEGER, promoact_id INTEGER);");
				
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		throw new RuntimeException(ctxt.getString(R.string.on_upgrade_error));
	}
	public void AddCity(City c) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues city_cv = new ContentValues();
		city_cv.put(DatabaseHelper.C_ID, c.getCityId());
		city_cv.put(DatabaseHelper.LAT, c.getCityLat());
		city_cv.put(DatabaseHelper.LON, c.getCityLon());
		city_cv.put(DatabaseHelper.RAD, c.getCityRadius());
		db.insert(DatabaseHelper.TABLE_C, DatabaseHelper.C_ID, city_cv);
		db.close();
	}
	
	public void AddStore(Store s) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues s_cv = new ContentValues();
		s_cv.put(DatabaseHelper.S_ID, s.getStoreId());
		s_cv.put(DatabaseHelper.CL_ID, s.getClientId());
		s_cv.put(DatabaseHelper.CL_NAME, s.getClientName());
		s_cv.put(DatabaseHelper.CAT, s.getCatInd());
		s_cv.put(DatabaseHelper.LAT, s.getLat());
		s_cv.put(DatabaseHelper.LON, s.getLon());
		s_cv.put(DatabaseHelper.RAD, s.getStoreRadius());		
		db.insert(DatabaseHelper.TABLE_S, DatabaseHelper.S_ID, s_cv);
		db.close();
	}
	public void AddPromo(Promoact p) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues p_cv = new ContentValues();
		p_cv.put(DatabaseHelper.P_ID, p.getPromoId());
		p_cv.put(DatabaseHelper.CL_ID, p.getClientId());
		p_cv.put(DatabaseHelper.DISC, p.getDiscount());	
		p_cv.put(DatabaseHelper.CAT, p.getCatInd());
		db.insert(DatabaseHelper.TABLE_P, DatabaseHelper.P_ID, p_cv);
		db.close();
	}
	
	public void deletePromo(int id) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(DatabaseHelper.TABLE_P, DatabaseHelper.P_ID+"=?", new String[] {Integer.toString(id)});
		db.close();
	}	
	
	public void addPromoStores(int store_id, int promoact_id) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues ps_cv = new ContentValues();
		ps_cv.put(DatabaseHelper.S_ID, store_id);
		ps_cv.put(DatabaseHelper.P_ID, promoact_id);
		db.insert(DatabaseHelper.TABLE_SP, DatabaseHelper.S_ID, ps_cv);
		db.close();
	}
	public void ClearCities() {
		 SQLiteDatabase db = this.getWritableDatabase();
		 if (db != null) {
			db.delete(DatabaseHelper.TABLE_C, null, null);
			db.close();			 
		}		
	 }
	public void ClearStores() {
		 SQLiteDatabase db = this.getWritableDatabase();
		 if (db != null) {
			db.delete(DatabaseHelper.TABLE_S, null, null);
			db.close();			 
		}		
	 }
	public void ClearPromoStores() {
		SQLiteDatabase db = this.getWritableDatabase();
		 if (db != null) {
			db.delete(DatabaseHelper.TABLE_SP, null, null);
			db.close();			 
		}		
	}
	public void ClearPromos() {
		SQLiteDatabase db = this.getWritableDatabase();
		 if (db != null) {
			db.delete(DatabaseHelper.TABLE_P, null, null);
			db.close();			 
		}		
	}
	
	public void updateStatusBlock(String cl_id) {		
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(DatabaseHelper.STOP, 1);
		db.update(DatabaseHelper.TABLE_P, cv, DatabaseHelper.CL_ID+"="+cl_id, null);
		db.close();
	}
	public void updateStatusUnblock(String cl_id) {		
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(DatabaseHelper.STOP, 0);
		db.update(DatabaseHelper.TABLE_P, cv, DatabaseHelper.CL_ID+"="+cl_id, null);
		db.close();
	}
	public void paStopUpdate(String pa_id, int code) {		
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(DatabaseHelper.STOP, code);
		db.update(DatabaseHelper.TABLE_P, cv, DatabaseHelper.P_ID+"="+pa_id, null);
		db.close();
	}	
	
}
