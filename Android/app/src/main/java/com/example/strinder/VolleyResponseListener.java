package com.example.strinder;

import com.android.volley.VolleyError;

public interface VolleyResponseListener {

    void onResponse(final Object response);

    void onError(final VolleyError error);
}
