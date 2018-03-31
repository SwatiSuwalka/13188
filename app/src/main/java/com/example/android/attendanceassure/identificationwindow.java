package com.example.android.attendanceassure;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.AddPersistedFaceResult;
import com.microsoft.projectoxford.face.contract.CreatePersonResult;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.FaceList;
import com.microsoft.projectoxford.face.contract.FaceListMetadata;
import com.microsoft.projectoxford.face.contract.FaceMetadata;
import com.microsoft.projectoxford.face.contract.FaceRectangle;
import com.microsoft.projectoxford.face.contract.GroupResult;
import com.microsoft.projectoxford.face.contract.IdentifyResult;
import com.microsoft.projectoxford.face.contract.LargeFaceList;
import com.microsoft.projectoxford.face.contract.LargePersonGroup;
import com.microsoft.projectoxford.face.contract.Person;
import com.microsoft.projectoxford.face.contract.PersonFace;
import com.microsoft.projectoxford.face.contract.PersonGroup;
import com.microsoft.projectoxford.face.contract.SimilarFace;
import com.microsoft.projectoxford.face.contract.SimilarPersistedFace;
import com.microsoft.projectoxford.face.contract.TrainingStatus;
import com.microsoft.projectoxford.face.contract.VerifyResult;
import com.microsoft.projectoxford.face.rest.ClientException;


public class identificationwindow extends AppCompatActivity
{
    private Uri filePath;
    private FaceServiceClient faceServiceClient = new FaceServiceRestClient("https://westcentralus.api.cognitive.microsoft.com/face/v1.0","api_key") ;
    private final String personGroupId = "friends";
    ImageView imageView;
    Bitmap bitmap;
    StorageReference ref;
    Bitmap mBitmap;
    DatabaseReference reference;
    Face[] facesDetected;
    String FUri;
    Button takepicture;
    //Firebase
    FirebaseStorage storage;
    StorageReference storageReference;
    private static final int CAM_REQUEST =1313;


    //detecting how many faces in the image
    class detectTask extends AsyncTask<InputStream,String,Face[]> {
        private ProgressDialog mDailog = new ProgressDialog(identificationwindow.this);

        @Override
        protected Face[] doInBackground(InputStream... params) {
            try
            {
                publishProgress("Detecting.....");
                Face[] results = faceServiceClient.detect(params[0],true,false,null);
                if(results == null)
                {
                    publishProgress("Detection finished ...Nothing Detected");
                    return null;
                }

                else{
                    publishProgress(String.format("Detection finished %d  faces(s) detected " ,results.length));
                    return results;
                }
            }
            catch (Exception ex)
            {
                return null;
            }
        }
        @Override
        protected void onPreExecute()
        {
            mDailog.show();
        }

        @Override
        protected void onPostExecute(com.microsoft.projectoxford.face.contract.Face[] faces) {
            mDailog.dismiss();

            facesDetected = faces;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            mDailog.setMessage(values[0]);
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identificationwindow);


        Intent intent=  new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,CAM_REQUEST);

        storage=FirebaseStorage.getInstance();
        storageReference=storage.getReferenceFromUrl("gs://attn-e877a.appspot.com");

        imageView = (ImageView)findViewById(R.id.image);

    }

    //taking picture from camera and set it on display and save in firebase
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CAM_REQUEST){
             bitmap = (Bitmap) data.getExtras().get("data");
             imageView.setImageBitmap(bitmap);
             filePath =data.getData();
            if(filePath != null)
            {
                final ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("Uploading...");
                progressDialog.show();

                 ref = storageReference.child("images/"+ UUID.randomUUID().toString());
                ref.putFile(filePath)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                progressDialog.dismiss();
                                Toast.makeText(identificationwindow.this, "Uploaded", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(identificationwindow.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                                progressDialog.setMessage("Uploaded "+(int)progress+"%");
                            }
                        });


            }
        }


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        new detectTask().execute(inputStream);

        final UUID[] faceIds = new UUID[facesDetected.length];
        for(int i=0;i<facesDetected.length;i++){
            faceIds[i]=facesDetected[i].faceId;
        }
        new IdentificationTask(personGroupId).execute(faceIds);

    }


    private class IdentificationTask extends AsyncTask<UUID,String,IdentifyResult[]>
    {
        private ProgressDialog mDailog = new ProgressDialog(identificationwindow.this);

        String personGroupId;

        public IdentificationTask(String personGroupId)
        {
            this.personGroupId = personGroupId;
        }

        @Override
        protected IdentifyResult[] doInBackground(UUID... params)
        {
            try{
                publishProgress("Getting person group status...");
                TrainingStatus trainingStatus = faceServiceClient.getPersonGroupTrainingStatus(this.personGroupId);

                if(trainingStatus.status != TrainingStatus.Status.Succeeded)
                {
                    publishProgress("Person group training status is" + trainingStatus.status);
                    return  null;
                }
                publishProgress("Identifying....");
                return faceServiceClient.identity(personGroupId,//person group id
                        params,//faceids
                        1); // max no. of condidate return
            }
            catch (Exception ex)
            {
                return null;
            }
        }

        @Override
        protected void onPreExecute()
        {
            mDailog.show();
        }

        @Override
        protected void onPostExecute(IdentifyResult[] identifyResults) {
            mDailog.dismiss();

            for(IdentifyResult identifyResult:identifyResults){
                new PersonDetectionTask(personGroupId).execute(identifyResult.candidates.get(0).personId);
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            mDailog.setMessage(values[0]);
        }
    }



    private class PersonDetectionTask extends  AsyncTask<UUID,String,Person>{
        private ProgressDialog mDailog = new ProgressDialog(identificationwindow.this);
        private  String personGroupId;
        public PersonDetectionTask(String personGroupId) {
            this.personGroupId=personGroupId;
        }

        @Override
        protected Person doInBackground(UUID... params) {
            try{
                publishProgress("Getting person group status");
                return faceServiceClient.getPerson(personGroupId,params[0]);
            }
            catch (Exception ex){
                return null;
            }
        }



        @Override
        protected void onPreExecute()
        {
            mDailog.show();
        }

        @Override
        protected void onPostExecute(Person person) {
            mDailog.dismiss();

            imageView = (ImageView) findViewById(R.id.image);
            imageView.setImageBitmap(drawFaceRectangleOnBitmap(bitmap,facesDetected,person.name));

        }

        @Override
        protected void onProgressUpdate(String... values) {
            mDailog.setMessage(values[0]);
        }
    }


    //drawing rectangle on person face
    private Bitmap drawFaceRectangleOnBitmap(Bitmap mBitmap, Face[] faceDetected, String name)
    {
        Bitmap bitmap = mBitmap.copy(Bitmap.Config.ARGB_8888,true);
        Canvas canvas = new Canvas(bitmap);
        Paint paint= new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(12);
        if(faceDetected != null)
        {
            for(Face face : faceDetected)
            {
                FaceRectangle faceRectangle = face.faceRectangle;
                canvas.drawRect( faceRectangle.left,
                        faceRectangle.top,
                        faceRectangle.left+faceRectangle.width,
                        faceRectangle.top+faceRectangle.height,
                        paint);
                drawTextOnCanvas(canvas,100,((faceRectangle.left+faceRectangle.width)/2)+100,(faceRectangle.top+faceRectangle.height)+50,Color.WHITE,name);
            }
        }
        return  bitmap;
    }

    //show person name of whoes face is detected
    private void  drawTextOnCanvas(Canvas canvas ,int textSize, int x, int y, int color, String name)
    {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        paint.setTextSize(textSize);

        float textwidth = paint.measureText(name);
        canvas.drawText(name,x-(textwidth/2),y-(textSize/2),paint);
    }
}



