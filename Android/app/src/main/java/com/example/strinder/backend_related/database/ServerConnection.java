package com.example.strinder.backend_related.database;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/** This class contains methods that sends requests to the server from a specific context. */
public class ServerConnection {

    private final RequestQueue requestQueue;


    /**
     * Initializes a ServerConnection for a specific Context.
     *
     * @param context - the context in which the connection will operate from.
     */
    public ServerConnection(final Context context) {
        if(context != null)
            requestQueue = Volley.newRequestQueue(context);
        else
            requestQueue = null;

    }

    private static final String BASE_URL = "http://10.0.2.2:5000"; //"https://strinder.herokuapp.com"; //Emulator: 10.0.2.2:5000

    /**
     * Send a StringRequest with a bound JsonObject.
     *
     * @param route                  - the server route. This is the part after the BASE_URL and should start with
     *                               a '/'.
     * @param json                   - the JsonObject that is to be sent with the request.
     * @param method                 - the method that is to be used. POST,GET,PUT and DELETE are allowed.
     *                               POST is set as such: Request.Method.POST.
     * @param token                  - the authentication token if there is any. If not needed, simply set it to
     *                               null.
     * @param volleyResponseListener - the listener that will receive the onResponse or
     *                               onError callback.
     * @see VolleyResponseListener
     */
    public void sendStringJsonRequest(final String route,
                                      final JSONObject json, final int method,
                                      final String token,
                                      final VolleyResponseListener<String> volleyResponseListener) {

        if (method != Request.Method.POST && method != Request.Method.GET &&
                method != Request.Method.DELETE && method != Request.Method.PUT) {
            throw new IllegalArgumentException("Method is not valid. Accepts: GET,POST,PUT" +
                    ",DELETE");
        }

        String url = BASE_URL + route;
        String jsonString = json.toString();

        if(requestQueue != null) {
            StringRequest request = new StringRequest(method, url, (volleyResponseListener::onResponse), volleyResponseListener::onError) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> params = new HashMap<>();
                    if (token != null) {
                        params.put("Authorization", "Bearer " + token);
                    }

                    params.put("Content-Type", "application/json");


                    return params;
                }

                @Override
                public byte[] getBody() {
                    return jsonString.getBytes(StandardCharsets.UTF_8);
                }
            };
            requestQueue.add(request);
        }


    }


}
