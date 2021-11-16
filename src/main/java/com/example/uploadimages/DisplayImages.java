package com.example.uploadimages;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DisplayImages extends AppCompatActivity implements RecyclerViewAdapter.OnItemClick{

    // Creating DatabaseReference.
    private DatabaseReference databaseReference;
    private FirebaseStorage mStorage;
    private ValueEventListener valueEventListener;
    // Creating List of ImageUploadInfo class.
    private List<DetailUpload> list;


    // Creating RecyclerView.
    RecyclerView recyclerView;
    // Creating RecyclerView.Adapter.
    RecyclerView.Adapter adapter ;


    // Creating Progress dialog
    ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_images);


// Assign id to RecyclerView.
        recyclerView = findViewById(R.id.recyclerView);
// Setting RecyclerView size true.
        recyclerView.setHasFixedSize(true);
// Setting RecyclerView layout as LinearLayout.
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        list = new ArrayList<>();
        adapter = new RecyclerViewAdapter(DisplayImages.this, list);
        recyclerView.setAdapter(adapter);

        // Assign activity this to progress dialog.
        progressDialog = new ProgressDialog(DisplayImages.this);
// Setting up message in Progress dialog.
        progressDialog.setMessage("Loading Images From Firebase.");
// Showing progress dialog.
        progressDialog.show();

        mStorage = FirebaseStorage.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference(Constans.DATABASE_PATH_UPLOADS);

        // Adding Add Value Event Listener to databaseReference.
        valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    DetailUpload detailUpload = postSnapshot.getValue(DetailUpload.class);
                    detailUpload.setKey(postSnapshot.getKey());
                    list.add(detailUpload);
                }

                adapter.notifyDataSetChanged();

                // Hiding the progress dialog.
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DisplayImages.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                // Hiding the progress dialog.
                progressDialog.dismiss();

            }
        });
    }

    @Override
    public void onItemClick(int position) {
        Toast.makeText(this, "Normal click at position: " + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onWhatEverClick(int position) {
        Toast.makeText(this, "Whatever click at position: " + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteClick(int position) {
        DetailUpload selectedItem = list.get(position);
        final String selectedKey = selectedItem.getKey();
        StorageReference imageRef = mStorage.getReferenceFromUrl(selectedItem.getImageURL());
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                databaseReference.child(selectedKey).removeValue();
                Toast.makeText(DisplayImages.this, "Item deleted", Toast.LENGTH_SHORT).show();
                DisplayImages.this.finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseReference.removeEventListener(valueEventListener);
    }

    //    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        databaseReference.removeEventListener(valueEventListener);
//    }
}