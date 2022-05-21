package com.example.strinder.backend_related.database;

import com.android.volley.VolleyError;

//We use generics here, which isn't really necessary. In all cases it is a String.

/** This listener interface is used when doing a request via Volley. */
public interface VolleyResponseListener<T> {
    /** This is called when Volley gets a response from the given server url.
     *
     * @param response - the response Object.
     */
    void onResponse(final T response);

    /** This is called when Volley gets an error response from the given server url.
     *
     * @param error - the VolleyError object.
     */
    void onError(final VolleyError error);
}
