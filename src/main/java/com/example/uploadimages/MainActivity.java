package com.example.uploadimages;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.internal.Constants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    // Image request code for onActivityResult() .
    int IMAGE_REQUEST_CODE = 7;

    private static final int CAMERA_REQUEST_CODE = 1;

    // Folder path for Firebase Storage.
//    String storage_path = "all_image_uploads/";

    // Root Database Name for Firebase Database.
//    public static final String database_path = "all_image_uploads_database";

    // Creating button.
    Button chooseButton, displayImageButton;

    // Creating EditText.
    EditText imageName;

    // Creating ImageView.
    ImageView selectImage, uploadButton, chooseCamera;

    Bitmap photo;

    // Creating URI.
    Uri filePathUri;

    // Creating StorageReference and DatabaseReference object.
    StorageReference storageReference;
    DatabaseReference databaseReference;


    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Assign FirebaseStorage instance to storageReference.
        storageReference = FirebaseStorage.getInstance().getReference();

        // Assign FirebaseDatabase instance with root database name.
        databaseReference = FirebaseDatabase.getInstance().getReference(Constans.DATABASE_PATH_UPLOADS);

        //Assign ID'S to button.
        chooseButton = (Button) findViewById(R.id.ButtonChooseImage);
        uploadButton = (ImageView) findViewById(R.id.ButtonUploadImage);

        displayImageButton = (Button) findViewById(R.id.DisplayImagesButton);

        chooseCamera = (ImageView) findViewById(R.id.ButtonChooseCamera);

        // Assign ID's to EditText.
        imageName = (EditText) findViewById(R.id.ImageNameEditText);

        // Assign ID'S to image view.
        selectImage = (ImageView) findViewById(R.id.ShowImageView);

        // Assigning Id to ProgressDialog.
        progressDialog = new ProgressDialog(MainActivity.this);

        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Creating intent.
                Intent intent = new Intent();

                // Setting intent type as image to select image from phone storage.
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Please Select Image"), IMAGE_REQUEST_CODE);

            }
        });

        chooseCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, CAMERA_REQUEST_CODE);
                }
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Calling method to upload selected image on Firebase storage.
                uploadImageToFirebaseStorage();
            }
        });

        displayImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DisplayImages.class);
                startActivity(intent);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {
            filePathUri = data.getData();

            Picasso.with(this).load(filePathUri).into(selectImage);

            CropImage.activity(filePathUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);

            try {

                // Getting selected image into Bitmap.
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePathUri);

                // Setting up bitmap selected image into ImageView.
                selectImage.setImageBitmap(bitmap);

                // After selecting image change choose button above text.
//                chooseButton.setText("Image Selected");

            } catch (IOException e) {

                e.printStackTrace();
            }

        } else if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {

            filePathUri = data.getData();
            selectImage.setImageURI(filePathUri);

            CropImage.activity(filePathUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                selectImage.setImageURI(resultUri);
                filePathUri = resultUri;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

    public String GetFileExtension(Uri uri) {

        ContentResolver contentResolver = getContentResolver();

        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        // Returning the file Extension.
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));

    }

    private void uploadImageToFirebaseStorage() {

        // Checking whether FilePathUri Is empty or not.
        if (filePathUri != null) {

            // Setting progressDialog Title.
            progressDialog.setTitle("Image is Uploading...");

            // Showing progressDialog.
            progressDialog.show();

            // Creating second StorageReference.
            StorageReference storageReference2nd = storageReference.child(Constans.STORAGE_PATH_UPLOADS + System.currentTimeMillis() +
                    "." + GetFileExtension(filePathUri));

            // Adding addOnSuccessListener to second StorageReference.
            storageReference2nd.putFile(filePathUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!urlTask.isSuccessful()) ;
                            Uri downloadUrl = urlTask.getResult();

                            // Getting image name from EditText and store into string variable.
                            DetailUpload detailUpload = new DetailUpload(imageName.getText().toString().trim(),
                                    downloadUrl.toString());



                            // Hiding the progressDialog after done uploading.
                            progressDialog.dismiss();


                            // Showing toast message after done uploading.
                            Toast.makeText(getApplicationContext(), "Image Uploaded Successfully ", Toast.LENGTH_LONG).show();

                            @SuppressWarnings("VisibleForTests")

                            // Getting image upload ID.
                            String imageUploadId = databaseReference.push().getKey();

                            // Adding image upload id s child element into databaseReference.
                            databaseReference.child(imageUploadId).setValue(detailUpload);

                        }
                    })

                    // If something goes wrong
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            // Hiding the progressDialog.
                            progressDialog.dismiss();

                            // Showing exception erro message.
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();

                        }
                    })

                    // On progress change upload time.
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                            // Setting progressDialog Title.
                            progressDialog.setTitle("Image is Uploading...");

                        }
                    });

        } else {

            Toast.makeText(MainActivity.this, "Please Select Image or Add Image Name", Toast.LENGTH_LONG).show();

        }

    }
}