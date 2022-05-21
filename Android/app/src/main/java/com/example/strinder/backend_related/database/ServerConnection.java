package com.example.strinder.backend_related.database;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.strinder.backend_related.tables.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/** This class contains methods that sends requests to the server from a specific context. */
public class ServerConnection {

    private final RequestQueue requestQueue;
    private final Context context;
    private static final int ACCESS_TOKEN_MISSING = 401;
    /**
     * Initializes a ServerConnection for a specific Context.
     *
     * @param context - the context in which the connection will operate from.
     */
    public ServerConnection(final Context context) {

        if(context != null) {
            this.context = context;
            requestQueue = Volley.newRequestQueue(context);
        }
        else
            throw new IllegalArgumentException("Context is null in ServerConnection instance.");
    }

    private static final String BASE_URL = "https://strinder-android.herokuapp.com"; //Emulator: 10.0.2.2:5000

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
            StringRequest request = new StringRequest(method, url,
            (volleyResponseListener::onResponse), volleyResponseListener::onError) {
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
    /** Sends a request to the server, requesting a new access token.
     * @param user - the logged in User object, which allows the method to retrieve the
     *            refresh token
     */
    public void refresh(final User user) {
        sendStringJsonRequest("/refresh", new JSONObject(), Request.Method.POST,
                user.getRefreshToken(),
                new VolleyResponseListener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            user.setAccessToken((String)object.get("access_token"));
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(context, "Retrieved New Access Token From Server",
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(VolleyError error) {
                        Toast.makeText(context,"Failed To Retrieve Access Token From Server",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    /** Executes {@link #refresh(User) refresh} method if the status code of the error
     * equals 401.
     * @param error - the VolleyError.
     * @param user - the logged in User object.
     */
    public void maybeDoRefresh(final VolleyError error, final User user) {
        if(error.networkResponse.statusCode == ACCESS_TOKEN_MISSING) {
           refresh(user);
        }
    }


}
