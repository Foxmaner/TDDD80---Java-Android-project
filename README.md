# TDDD80 Project
![Android image](https://lh3.googleusercontent.com/GTmuiIZrppouc6hhdWiocybtRx1Tpbl52eYw4l-nAqHtHd4BpSMEqe-vGv7ZFiaHhG_l4v2m5Fdhapxw9aFLf28ErztHEv5WYIz5fA)

Developed by Liam Andersson and Eskil Brännerud.
## How To Build

The project is connected to a remote server hosted on Heroku, uses Google Sign In (which requires an app in Google Cloud), Firebase app and Google Maps.

In order to clone the repository, execute the following **git** command:

    git clone https://gitlab.liu.se/liaan514/tddd80-projekt.git

You might think that you can clone the repository and just run the program, however it is not quite that easy. We have a locked development environment in our Google Cloud - which basically means that all developers require a key.

Because of this, you need to contact us in order for us to give you access to the project inside **Android Studio**. You also need our web client id key and maps api key, which we will give to you with the client id key.

In order for us to create a client id key for you, we need your **Android Studio** environment SHA-1 fingerprint. You can do this by following the following instructions:
 - Open Android Studio
 - On the right side of Android Studio, you should see a **Gradle** tab. Click it.
 - You should now see a new tab. Under the title "Gradle", there is a button with an elephant on it. When hovering, it should say "Execute Gradle Task".
 - In the new window, run the command "gradle signInReport".

You should now, in the console, receive a SHA-1 key. This is **vital** in order for us to create a key.

When you have these keys, they will have the following format:
    
    X-Y.apps.googleusercontent.com where X and Y are random Strings.
    
When you open the project in **Android Studio** (the Android folder), you need to go to the **values** folder. 
The values folder path is *Android/app/src/main/res/values*. 

Right click the values folder, choose **New**, then click on **Values Resource File**. Name this file **tokens.xml**

    <?xml version="1.0" encoding="utf-8"?>  
    <resources>  
	     
      <string name="web_client_id">WEB_CLIENT_ID</string>  
      <string name="client_id">CLIENT_KEY_HERE</string>  
      
    </resources>

Create two `<string>` tags as above.
 
Replace *WEB_CLIENT_ID* with the web client key that you have received, and do the same with 
*CLIENT_ID*, but with the client id key of course.

Because we use Google Maps you need a maps api key in the **local.properties** file.

Add the following file to the **local.properties** file, which is located inside the Android folder.

    MAPS_API_KEY=THE_KEY

Replace **THE_KEY** with the key that we have given you.

Now you should be able to build and run the project in **Android Studio**. 

The gradle dependencies should be as follows:

    dependencies {  
      //Firestore  
      implementation platform('com.google.firebase:firebase-bom:30.0.1')  
        implementation 'com.google.firebase:firebase-analytics'  
      implementation 'com.google.firebase:firebase-storage'  
      implementation 'com.google.firebase:firebase-auth:21.0.4'  
      
      //Used to convert GoogleSignInAccount URI to an actual image.  
      implementation('com.squareup.picasso:picasso:2.5.2')  
        //Volley  
      implementation("com.android.volley:volley:1.2.1")  
        //Google  
      implementation 'com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava'  
      implementation 'com.google.apis:google-api-services-people:v1-rev255-1.23.0'  
      implementation 'com.google.api-client:google-api-client-android:1.23.0'  
      implementation 'com.google.android.gms:play-services-auth:20.2.0'  
      implementation 'com.google.android.gms:play-services-base:18.0.1'  
      implementation 'com.google.android.gms:play-services-maps:18.0.2'  
      implementation 'com.google.android.material:material:1.6.0'  
      implementation 'com.google.android.gms:play-services-auth:20.2.0'  
      implementation('com.google.maps.android:android-maps-utils:2.3.0')  
        // Java language implementation  
      implementation "androidx.navigation:navigation-fragment:2.4.2"  
      implementation "androidx.navigation:navigation-ui:2.4.2"  
      
      //GSON  
      implementation 'com.google.code.gson:gson:2.8.9'  
      
      //Other  
      implementation 'androidx.appcompat:appcompat:1.4.1'  
      implementation 'androidx.constraintlayout:constraintlayout:2.1.3'  
      implementation 'androidx.legacy:legacy-support-v4:1.0.0'  
      implementation 'androidx.annotation:annotation:1.3.0'  
      implementation 'androidx.lifecycle:lifecycle-livedata-ktx:2.4.1'  
      implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.1'  
      testImplementation 'junit:junit:4.13.2'  
      androidTestImplementation 'androidx.test.ext:junit:1.1.3'  
      androidTestImplementation 'androidx.test.espresso:espresso-core:3.4.0'  
      
    }


Make sure that your *build.gradle* file contains these implementations if you have build issues.
If some classes are not found, try clearing the cache as such:
Click **File**,  then **Invalidate Caches**. This will probably solve the issue you are facing.

NOTE: The Heroku server has to be online, otherwise you will be prompted an error!

## APK File
We have generated an .apk file for the project which can be found [here](https://www.mediafire.com/file/qx43q20spwn0yxq/strinder-apk.zip/file)

## Our Ambitions

We have the following features in our app, among other bonus features.
**Main Features:**

 - Google Sign In
 - Comment posts
 - Like posts
 - Follow other users
 - Camera
 - GPS

**Bonus Features**

 - File storage database (Firebase)
 - Upload images from device to database.
 - Delete posts and comments.
 - Google maps that collaborate with the GPS feature.
 - Security with Google Sign In

We are aiming for the grade **5**, because we have 8 points if we count the 2 points that we received from completing our lab tasks on time.




