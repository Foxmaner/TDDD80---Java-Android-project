package com.example.strinder.backend_related;

import com.android.volley.VolleyError;
/** This listener interface is used when doing a request via Volley. */
public interface VolleyResponseListener {
    /** This is called when Volley gets a response from the given server url.
     *
     * @param response - the response Object.
     */
    void onResponse(final Object response);

    /** This is called when Volley gets an error response from the given server url.
     *
     * @param error - the VolleyError object.
     */
    void onError(final VolleyError error);
}
