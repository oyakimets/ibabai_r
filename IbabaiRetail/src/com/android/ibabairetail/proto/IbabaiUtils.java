package com.android.ibabairetail.proto;

public class IbabaiUtils {
	public static final String PREFERENCES = "MyPrefs";
	public static final String STORE_ID = "store_id";
	public static final String CITY="city";
	public static final String USER_ID="user_id";
	public static final String BALANCE = "Balance";
	public static final String API_KEY = "api_key";
	public static final String EMAIL = "email";
	public static final String PHONE = "phone";
	public static final String AGE = "age";
	public static final String GENDER="gender";
	public static final String LAST_STORE = "last_store";
	public static final String PASS = "password";
	public static final String PASS_CONF = "password_confirmation";
	public static final String C_ID = "customer_id";
	public static final String EXTRA_CODE = "code";
	public static final String EXTRA_PA = "pa_id";
	public static final String AGENT_ID = "agent_id";
	public static final String AGENT_NAME = "agent_name";
	public static final String AMOUNT = "amount";
	public static final String ACCOUNT = "account";
	public static final String P_ID = "promoact_id";
	public static final String FLAG = "dc_flag";
	public static final String EXTRA_POSITION="position";
	public static final String EXTRA_NI="position";
	public static final String LOAD_TOGGLE = "load_toggle";
	public static final String MODEL="promo_model";
	public static final String EXTRA_DIR="directory";
	public static final String VEN_EXT="ven_ext.jpg";
	public static final String PREF_VEN_DIR="pendingConDir";
	public static final String VEN_BASEDIR="vendors";
	public static final String SL_BASEDIR="stop_list";
	public static final String PROMO_CODE="code";
	public static final String ACTIVE_PROMO="active_promo";
	public static final String PA_UPDATE="updated";
	public static final String STORE_TIME="store_time";
	public static final String STORE_ENTRY_TIME="store_entry_time";
	public static final String BLOCK_COUNTER="block_counter";
	
	
	public final static String BASE_API_ENDPOINT_URL="http://192.168.1.103:3000/api/v1/";
	public static final String LOGS_API_ENDPOINT_URL= BASE_API_ENDPOINT_URL+"customer_logs.json";
	public static final String CON_BASE_URL = "http://192.168.1.103:3000/promo_zip/";
	public static final String PAYMENT_API_ENDPOINT_URL= BASE_API_ENDPOINT_URL+"transactions.json";
	public final static String REGISTER_API_ENDPOINT_URL= BASE_API_ENDPOINT_URL+"customers";
	public static final String CITIES_URL = BASE_API_ENDPOINT_URL+"city_uploads";
	public static final String CITIES_UPDATE_URL = BASE_API_ENDPOINT_URL+"city_updates";
	public static final String STORE_BASE_URL = BASE_API_ENDPOINT_URL+"store_uploads.json";
	public static final String STORE_UPDATE_URL = BASE_API_ENDPOINT_URL+"store_updates.json";
	public static final String PROMO_NEW_USER_URL = BASE_API_ENDPOINT_URL+"promo_uploads.json";
	public static final String PROMO_UPDATE_URL = BASE_API_ENDPOINT_URL+"promo_updates.json";
	public static final String PS_BASE_URL = BASE_API_ENDPOINT_URL+"ps_uploads";
	public static final String CON_EXT="con_ext.zip";
	public static final String CON_BASEDIR="promo_content";
	public static final String PREF_CON_DIR="pendingConDir";
}
