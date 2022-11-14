package com.example.faceemotiontest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.net.URI;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;


public class MainActivity extends AppCompatActivity {
    private ImageView imageView; // variable to hold the image view in our activity_main.xml
    private TextView resultText; // variable to hold the text view in our activity_main.xml
    private static final int RESULT_LOAD_IMAGE  = 100;
    private static final int REQUEST_CAMERA_CODE = 300;
    private static final int REQUEST_PERMISSION_CODE = 200;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // hold our image view and text view
        imageView = findViewById(R.id.imageView);
        resultText = findViewById(R.id.resultText);
        Button button2 = (Button) findViewById(R.id.button);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openactivity_negative();
            }
        });

        Button button = (Button) findViewById(R.id.button3);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openactivity_positive();
            }
        });
    }
    public void openactivity_positive(){
        Intent intent = new Intent(this, activity_positive.class);
        startActivity(intent);
    }

    public void openactivity_negative(){
        Intent intent = new Intent(this, activity_negative.class);
        startActivity(intent);
    }


    /**
     * ensures we have permission to look the media gallery then makes the request to look for a picture
     * when the "GET IMAGE" Button is clicked
     */
    public void getImage(View view) {
        // check if user has given us permission to access the gallery
        if(checkPermission()) {
            Intent choosePhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
            // specify that we're going to look for any known image type - *. could specify .png etc.
            choosePhotoIntent.setType("image/*");
            launchGalleryImageGetter.launch(choosePhotoIntent);
        }
        else {
            requestPermission();
        }


    }

    /**
     * this defines an activity that will handle the intent of dealing with loading the
     * image from the gallery and registers it with your app
     */
    ActivityResultLauncher<Intent> launchGalleryImageGetter
            = registerForActivityResult(
            new ActivityResultContracts
                    .StartActivityForResult(),
            result -> {
                if (result.getResultCode()
                        == Activity.RESULT_OK) {
                    Intent data = result.getData();

                    if (data != null
                            && data.getData() != null) {
                        Uri selectedImageUri = data.getData();
                        Bitmap selectedImageBitmap;
                        try {
                            // ask the media store for the actual image resource as a bitmap
                            selectedImageBitmap
                                    = MediaStore.Images.Media.getBitmap(
                                    this.getContentResolver(),
                                    selectedImageUri);
                            // actually use the image resource we loaded
                            // if you were doing something else with it, here's where to do it
                            imageView.setImageBitmap(selectedImageBitmap);

                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            });

    /**
     * this defines an activity that will handle the intent of dealing with loading the
     * image from the camera and registers it with your app
     */
    ActivityResultLauncher<Intent> launchCameraGetter
            = registerForActivityResult(
            new ActivityResultContracts
                    .StartActivityForResult(),
            result -> {
                if (result.getResultCode()
                        == Activity.RESULT_OK) {
                    Intent data = result.getData();

                    if (data != null
                            && data.getData() != null) {

                        Bitmap takenImageBitmap;
                        // load the image from the data payload
                        takenImageBitmap
                                =  (Bitmap) data.getExtras().get("data");
                        // actually use the image resource we loaded
                        // if you were doing something else with it, here's where to do it
                        imageView.setImageBitmap(takenImageBitmap);

                    }
                }
            });

    /**
     * uses the currently set image to make the API request when the "GET EMOTION" Button is clicked
     */
    public void getEmotion(View view) {
        final String TAG = "getEmotion";
        Log.i(TAG, "example printing to logcat from getEmotion()");
        // run the GetEmotionCall class in the background
        GetEmotionCall emotionCall = new GetEmotionCall(imageView);
        emotionCall.execute();
    }


    /**
     * if permission is not given we ask permission
     */
    private void requestPermission() {
        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA}, REQUEST_PERMISSION_CODE);
    }

    /**
     * determine if the right permissions have been set
     */
    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.READ_EXTERNAL_STORAGE);
        int result2 = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
        return result == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * gets the image from the camera and launches the appropriate activity to handle that pic
     */
    @SuppressLint("QueryPermissionsNeeded")
    public void getCameraImage(View view) {
        if(checkPermission()) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                //startActivityForResult(takePictureIntent, REQUEST_CAMERA_CODE);
                launchCameraGetter.launch(takePictureIntent);
            }

        }
        else {
            requestPermission();
        }
    }

    /**
     * asynchronous task class which makes the api call in the background
     */
    private class GetEmotionCall extends AsyncTask<Void, Void, String> {

        private final ImageView img;

        GetEmotionCall(ImageView img) {
            this.img = img;
        }

        // this function is called before the api call is made to let the user know we're working
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            resultText.setText("Getting results...");
        }

        // this function is called when the api call is made
        @Override
        protected String doInBackground(Void... params) {
            // set up a http client for making the API call
            HttpClient httpclient = HttpClients.createDefault();
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            try {
                // this URI comes from the API itself and can be modified to change what is requested
                URIBuilder builder = new URIBuilder("https://canadacentral.api.cognitive.microsoft.com/face/v1.0/detect?returnFaceId=false&returnFaceLandmarks=false&returnFaceAttributes=emotion,age,gender,headPose,smile,facialHair,glasses,hair,makeup&recognitionModel=recognition_01&returnRecognitionModel=false&detectionModel=detection_01");
                //URIBuilder builder = new URIBuilder("https://canadacentral.api.cognitive.microsoft.com/face/v1.0/detect?returnFaceId=true&returnFaceLandmarks=false&returnFaceAttributes=emotion,age,gender,headPose,smile,facialHair,glasses,hair,makeup&recognitionModel=recognition_01&returnRecognitionModel=false&detectionModel=detection_01\n");
                URI uri = builder.build();
                // make a new POST request since we need to send our image to the API server
                HttpPost request = new HttpPost(uri);
                // required type for uploading a file
                request.setHeader("Content-Type", "application/octet-stream");
                // enter your subscription key here
                request.setHeader("Ocp-Apim-Subscription-Key", "0d3836e987594b01856f42f38f8a59fd");

                // Request body. setEntity method converts the image to base64
                request.setEntity(new ByteArrayEntity(toBase64(img), ContentType.APPLICATION_OCTET_STREAM));

                // getting a response and assigning it to the string res
                ClassicHttpResponse response = (ClassicHttpResponse) httpclient.execute(request);
                Log.i("doInBackground", response.toString());
                HttpEntity entity = response.getEntity();
                String res = EntityUtils.toString(entity);
                Log.i("doInBackground",res);
                return res;

            }
            catch (Exception e){
                return "null";
            }

        }

        /**
         * this function is called when we get a result from the API call
         * we get a string from the API that is (in this case) JSON which can be
         * converted into an array (list) of data objects to process
         */
        @Override
        protected void onPostExecute(String result) {
            JSONArray jsonArray = null;
            try {
                // convert the result string to JSONArray
                jsonArray = new JSONArray(result);
                String emotions = "";

                // get the scores object from the results
                // jsonArray will have length based on number of faces in image
                // so for selfies it will be 1 but coding for multiples is safer (although your app logic would be confused)
                for(int i = 0;i<jsonArray.length();i++) {
                    // this object has two elements: face rectangle and face attributes, we want the second
                    JSONObject jsonParentObject = new JSONObject(jsonArray.get(i).toString());
                    JSONObject jsonObject = jsonParentObject.getJSONObject("faceAttributes");
                    // within faceAttributes, we want the emotions
                    JSONObject scores = jsonObject.getJSONObject("emotion");

                    double max = 0;
                    String emotion = "";
                    // common "find max" algorithm, look through all and keep track of the highest score
                    for (int j = 0; j < scores.names().length(); j++) {
                        if (scores.getDouble(scores.names().getString(j)) > max) {
                            max = scores.getDouble(scores.names().getString(j));
                            emotion = scores.names().getString(j);
                        }
                    }
                    emotions += emotion + "\n";
                }
                // we built a string and place it on result text
                // TODO: change output mechanism to be more interesting
                resultText.setText(emotions);

            } catch (JSONException e) {
                resultText.setText("No emotion detected. Try again later");
            }
        }
    }

    /**
     * convert image to base 64 so that we can send the image to the Emotion API
     */
    public byte[] toBase64(ImageView imgPreview) {
        Bitmap bm = ((BitmapDrawable) imgPreview.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
        return baos.toByteArray();
    }

    /**
     * This function gets the selected picture from the camera and puts it on the image view
     * unused but I left it here because I haven't tested the camera due to emulator
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CAMERA_CODE && resultCode == RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photo);
        }
    }
}