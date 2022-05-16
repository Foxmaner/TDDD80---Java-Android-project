package com.example.strinder.backend_related.storage;

import android.content.Context;
import android.util.Log;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DeleteResult;
import com.dropbox.core.v2.files.WriteMode;
import com.dropbox.core.v2.sharing.ListSharedLinksResult;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;
import com.example.strinder.R;
import com.example.strinder.backend_related.tables.User;

import java.io.ByteArrayInputStream;
import java.io.IOException;

//FIXME
//  This is a note for the developers, and if it has not been completed before the hand in,
//  the professors. DropBox API is not intended to be used in this way. It is meant to use
//  people's DropBox accounts in order to upload files to that specific account. What is happening
//  below is that we have a central dropbox account - which is not ideal for security and production
//  reasons. Thus, an ideal solution would be to use Google Cloud and store files there as a central
//  database.
//  // Liam


/*
    Most developers do not like Singletons, however I can't pass this between the fragments and
    activities.
    Thus, I made it a singleton. It is a utility class after all.

 */
public class DropBoxServices {

    private DbxClientV2 client;
    private static DropBoxServices instance;
    private DropBoxServices() {

    }

    public static DropBoxServices getInstance() {
        if(instance == null) {
            instance = new DropBoxServices();
        }

        return instance;

    }

    public void initialize(final Context context) {
        // Create Dropbox client
        if(context != null) {
            DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/strinder").
                    build();
            client = new DbxClientV2(config, context.getString(R.string.dropbox_token));

        }
        else {
            throw new IllegalArgumentException("Context was null during DropBoxServices " +
                    "initialization.");
        }
    }

    public void getLinkToImage(final User user, final DbxCompletionListener listener) {
        Thread thread = new Thread(() -> {
            String result = null;
            SharedLinkMetadata data;
            String path = "/"+user.getUsername()+".png";


            try {
                /*
                We need an if statement below, because there can only be one shared link. Either
                we create one  - or we get the existing one.
                */
               ListSharedLinksResult results = client.sharing().listSharedLinksBuilder().
                       withPath(path).withDirectOnly(true).start();

               if(results.getLinks().isEmpty()) {
                   data = client.sharing().createSharedLinkWithSettings(path);

               }
               else {
                   data = results.getLinks().get(0);
               }

               //We want raw format, so we replace dl=0 with raw=1
               result = data.getUrl().replace("dl=0","raw=1");
            }
            catch (DbxException e) {
                e.printStackTrace();
                Log.e("DropBox Retrieve Error", "Failed to get temporary link to image.");
            }


            listener.onFinish(result);
        });

        thread.start();

    }


    public void saveImage(final ByteArrayInputStream stream, final User user,
                          final DbxCompletionListener listener) {

        Thread thread = new Thread(() -> {
            try {
                client.files().uploadBuilder("/"+user.getUsername()+".png").
                        withMode(WriteMode.OVERWRITE).
                        uploadAndFinish(stream);
            }
            catch (DbxException | IOException e) {
                e.printStackTrace();
                Log.e("DropBox Upload Error","Failed to upload image.");
                listener.onFinish(false);
            }

            listener.onFinish(true);
        });

        thread.start();

    }

    public void deleteImage(final User user, final DbxCompletionListener listener) {
        Thread thread = new Thread(() -> {
            DeleteResult result = null;
            try {
                result = client.files().deleteV2("/"+user.getUsername()+".png");
            }
            catch (DbxException e) {
                e.printStackTrace();
                Log.e("DropBox Delete Error", "Failed to remove image " + user.getUsername());
            }

            listener.onFinish(result);

        });

        thread.start();

    }

    public static String getUserImagePath(final User  user) {
        return "https://www.dropbox.com/s/g3ybnjebb26s51t/"+
                user.getUsername()+".png?raw=1";
    }

}