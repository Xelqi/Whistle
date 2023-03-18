package com.example.ddating;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button logout;
    private Button delete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Variable
        logout = findViewById(R.id.logout);
        delete = findViewById(R.id.delete);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Variable


        // Back to Main Activity and logout
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(MainActivity.this, "Logged Out", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, startActivity.class));
                finish();
            }
        });

        // Delete all User profile
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Get the User Email
                FirebaseUser currentuser = FirebaseAuth.getInstance().getCurrentUser();
                // Sign out
                FirebaseAuth.getInstance().signOut();

                // Collect DataBase
                CollectionReference currentProfile = db.collection("Users");

                // Delete all Data in Collection -> Document -> Collection(Dogs)
                db.collection("Users").document(currentuser.getUid()).collection("Dogs").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                currentProfile.document(currentuser.getUid()).collection("Dogs").document(document.getId()).delete();
                            }

                            // Delete all Data
                            currentProfile.document(currentuser.getUid()).delete();

                            // Delete User Email and Password
                            currentuser.delete();

                            Toast.makeText(MainActivity.this, "Account Deleted !", Toast.LENGTH_SHORT).show();

                            // Back to StartActivity
                            startActivity(new Intent(MainActivity.this, startActivity.class));
                            finish();
                        } else {
                            Toast.makeText(MainActivity.this, "Error Delete Data", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
    }


    // Check Profile Missing
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Check User Profile
        DocumentReference docRef = db.collection("Users").document(currentUser.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    Log.d("Message", "User Collected");

                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        Log.d("Message", "User Profile found !");

                        // Check Dog Profile
                        db.collection("Users").document(currentUser.getUid()).collection("Dogs").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()){
                                    Log.d("Message", "Dog Collected");

                                    if (task.getResult().size() > 0) {
                                        Log.d("Message", "Dogs found");
                                    } else {
                                        Log.d("Message", "No Data");

                                        Toast.makeText(MainActivity.this, "Dog Profile Missing", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(MainActivity.this, addDogProfileActivity.class));
                                        finish();
                                    }
                                } else {
                                    Log.d("Message", "Dog Collection Error");
                                }
                            }
                        });
                    } else {
                        Log.d("Message", "No Data");

                        Toast.makeText(MainActivity.this, "User Profile Missing", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, addUserProfileActivity.class));
                        finish();
                    }
                } else {
                    Log.d("Message", "User Collection Error");
                }
            }
        });



    }
}