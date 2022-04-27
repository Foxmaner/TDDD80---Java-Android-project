package com.example.strinder.private_data;

import android.accounts.Account;
import android.app.Activity;

import com.example.strinder.R;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.Person;

import java.io.IOException;
import java.util.List;

/** This class handles the connections to Google Services when it comes to their People API.
 * The signing in process is still kept inside the LoginFragment.
 */
public class GoogleServices {

    private final Activity activity;

    /** Initialize a GoogleServices object.
     *
     * @param activity - the activity object.
     */
    public GoogleServices(final Activity activity) {
        this.activity = activity;
    }

    /** Returns a Person object that potentially contains the data requested. This method HAS to be
     * called within a separate thread.
     * @param account - the GoogleSignInAccount object.
     * @param scopes - a list of valid scopes in String format.
 *                  See: https://developers.google.com/people/v1/how-tos/authorizing
     * @param fields - A field mask to restrict which fields on the person are returned.
     *               Multiple fields can be specified by separating them with commas.
     *               Valid values are: * addresses * ageRanges * biographies * birthdays *
     *               braggingRights * coverPhotos * emailAddresses * events * genders * imClients
     *              ' * interests * locales * memberships * metadata * names * nicknames *
     *               occupations * organizations * phoneNumbers * photos * relations *
     *               relationshipInterests * relationshipStatuses * residences * skills *
     *               taglines * urls
     * @param completionCode - the code that will be run after we successfully have received
     *                         a response from Google. This code will be run inside the GUI thread.
     * @param obj - an Object that you can alter in your completionCode.
     *              This can be null if you don't need it. The code will be run inside
     *                  the GUI thread so it would make sense to change the GUI.
     *
     */
    public void requestPrivateData(final GoogleSignInAccount account,
                                   List<String> scopes, String fields, ThreadCode completionCode,
                                   Object obj, final CompletionListener listener) {

            Thread thread = new Thread(() -> {
                Person person = null;

                if (activity != null) {

                    HttpTransport httpTransport = new NetHttpTransport();
                    JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();

                    GoogleAccountCredential credential =
                            GoogleAccountCredential.usingOAuth2(activity, scopes);
                    credential.setSelectedAccount(
                            new Account(account.getEmail(), "com.google"));

                    PeopleService service = new PeopleService.Builder(httpTransport, jsonFactory, credential)
                            .setApplicationName(activity.getString(R.string.app_name))
                            .build();


                    try {
                        person = service.people().get("people/me").setPersonFields(fields).execute();
                    }
                    catch (IOException e) {
                        //TODO Improve error handling
                        e.printStackTrace();
                    }
                }

                onComplete(person,completionCode,obj,listener);
            });

        thread.start();

    }

    /** This method is executed after the thread is completed.
     * @param person - the Person object recieved from the request.
     * @param code - the code that will run, its parameters are the other two parameters.
     * @param obj - an Object that can be altered inside the thread. It is set inside the UI thread.
     */
    private void onComplete(final Person person, final ThreadCode code, final Object obj,
                            final CompletionListener listener) {
        activity.runOnUiThread(() -> {
            code.run(person,obj);
            listener.onCompletion();
        });
    }
}
