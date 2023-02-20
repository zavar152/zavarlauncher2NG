package com.zavar.zavarlauncher.auth;

public final class AuthConstants {
    public static final String AUTHORIZATION_ENDPOINT = "https://login.microsoftonline.com/consumers/oauth2/v2.0/authorize";
    public static final String CLIENT_ID = "fa3dee6a-27cd-4f30-8344-c438b3f5c037";
    public static final String RESPONSE_TYPE = "code";
    public static final String REDIRECT_URI = "http://localhost:8000";
    public static final String SCOPE = "XboxLive.signin";

    public static final String AUTH_URL = AUTHORIZATION_ENDPOINT + "?client_id=" +
            CLIENT_ID + "&response_type=" + RESPONSE_TYPE + "&redirect_uri=" +
            REDIRECT_URI + "&scope=" + SCOPE;
}
