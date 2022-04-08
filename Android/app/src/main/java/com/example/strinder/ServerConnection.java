package com.example.strinder;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.strinder.logged_out.VolleyResponseListener;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ServerConnection {

    private static final String BASE_URL = "http://10.0.2.2:5000"; //Emulator: 10.0.2.2:5000

    public static void sendStringJsonRequest(final Context context, final String route,
                                             final JSONObject json, final int method,
                                             final VolleyResponseListener volleyResponseListener) {

        if(method != Request.Method.POST && method != Request.Method.GET &&
                method != Request.Method.DELETE && method != Request.Method.PUT) {
            throw new IllegalArgumentException("Method is not valid. Accepts: GET,POST,PUT" +
                    ",DELETE");
        }
        //FIXME This queue should probably be outside.
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        String url = BASE_URL + route;
        String jsonString = json.toString();

        StringRequest request = new StringRequest(method, url, (volleyResponseListener::onResponse), volleyResponseListener::onError) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
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
