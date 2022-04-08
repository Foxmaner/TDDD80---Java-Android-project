package com.example.strinder.logged_out;

import com.android.volley.VolleyError;

public interface VolleyResponseListener {

    void onResponse(final Object response);

    void onError(final VolleyError error);
}
