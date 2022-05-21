package com.example.strinder.backend_related.private_data;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.services.people.v1.model.Person;

import java.util.List;

/** This interface allows the user to send code into
 * {@link GoogleServices#requestPrivateData(GoogleSignInAccount, List, String,
 * ThreadCode, Object, CompletionListener)}.*/
@FunctionalInterface
public interface ThreadCode {
    /** This method will be executed after Google has sent us a successful response. This code
     * can take information from the Person object and use it on the passed Object.
     * @param person - the Person object provided by Google.
     * @param obj - the Object that you wish to add the Person object's information to.
     */
    void run(final Person person, final Object obj);

}
