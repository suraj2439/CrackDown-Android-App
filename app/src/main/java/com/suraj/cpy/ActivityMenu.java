package com.suraj.cpy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ActivityMenu extends AppCompatActivity {
    String groupId = "Undefine";
    LoadProgressBar progressBar;
    static final String TMP_GRP_ID = "99999";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        progressBar = new LoadProgressBar(ActivityMenu.this);

        Bundle extra = getIntent().getExtras();
        groupId = extra.getString("groupId");
        int action = extra.getInt("action");

        if(action == 0) {
            if(groupId.equals(TMP_GRP_ID)) {
                Toast.makeText(getApplicationContext(), "Oops! you are in whatsapp only mode", Toast.LENGTH_LONG).show();
                return;
            }
            else getSupportFragmentManager().beginTransaction().replace(R.id.group_container, new GroupInfo()).commit();
        }
            /*
        else if(action == 1) {
            //website
            FirebaseFirestore DB = FirebaseFirestore.getInstance();
            DB.collection("About").document("about").get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            About about1 = documentSnapshot.toObject(About.class);

                            Intent intent = new Intent("android.intent.action.VIEW",
                                    Uri.parse(about1.getWebsite()));
                            startActivity(intent);

                            finish();
                        }
                    });
        }*/
        else if(action == 2) {
            //feedback
            try {
                getSupportFragmentManager().beginTransaction().replace(R.id.group_container, new Feedback()).commit();
            }catch (Exception ex) {
                Log.d("error", ex.getMessage());
            }
        }
        else if(action == 3) {
            //help
            FirebaseFirestore DB = FirebaseFirestore.getInstance();
            DB.collection("About").document("about").get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            About about1 = documentSnapshot.toObject(About.class);

                            Intent intent = new Intent("android.intent.action.VIEW",
                                    Uri.parse(about1.getHelpLink()));
                            startActivity(intent);

                            finish();
                        }
                    });
        }

    }
}