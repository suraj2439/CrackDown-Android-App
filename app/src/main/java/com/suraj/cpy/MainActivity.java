package com.suraj.cpy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import uk.co.senab.photoview.PhotoViewAttacher;


public class MainActivity extends AppCompatActivity {
    String groupId;
    static final int UPLOAD_ON_DB  = 10;
    static final int SEARCH_FOR_RES = 20;
    static final int UPLOAD_RESULT = 30;
    static final int POOR_MATCHING = 20;
    static final int SEARCH_LOCALLY = 40;
    static final int ALL_PERMISSIONS = 100;
    static final String TMP_GRP_ID = "99999";

    ImageView captured_img;
    Switch wa_switch;
    PhotoViewAttacher pattacher;
    ImageButton take_pic;
    ImageButton search_img;
    ImageButton local_search;
    ImageButton upload;
    ImageButton next;
    ImageButton prev;
    Uri imageUri;
    //TODO take input sender name
    String senderName = "undefine";
    String imgPath;
    HashMap<String, List<Integer>> store_txt = new HashMap<>();
    List<Upload> images = new ArrayList<>();
    List<Integer> result = new ArrayList<>();
    // Create a Cloud Storage reference from the app
    FirebaseStorage storage;
    StorageReference storageReference;
    FirebaseUser fuser;
    String uid;
    static int count = 0;
    String path;
    String wa_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/WhatsApp/Media/WhatsApp Images";
    int curr_image_indx = 0;
    int curr_image_val = 0;
    int imageCnt = 0;

    // Write a message to the database
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference groups_ref;

    int tot_wa_images = -1;
    Long recent_file_time;
    Long wa_beginning_time, wa_end_time;
    boolean is_wa_enabled = false;
    boolean isPermissionsGranted = false;

    private AdView mAdView;
    private InterstitialAd mInterstitialAd;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mInterstitialAd != null) {
            mInterstitialAd.show(this);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Bundle extra = getIntent().getExtras();
        fuser = (FirebaseUser) extra.get("userId");
        uid = fuser.getUid();
        senderName = extra.getString("senderName");
        is_wa_enabled = extra.getBoolean("wa_feature");
        groupId = extra.getString("groupId");
        wa_path = extra.getString("wa_path");
        recent_file_time = System.currentTimeMillis();
        wa_beginning_time = System.currentTimeMillis() - Long.valueOf(extra.getString("waTime")) * 60 * 1000;
        wa_end_time = System.currentTimeMillis();
        groups_ref = db.getReference(groupId);

        captured_img = (ImageView) findViewById(R.id.captured_img);
        PrevImages prevImages = new PrevImages();
        pattacher = new PhotoViewAttacher(captured_img);
        pattacher.update();
        wa_switch = (Switch) findViewById(R.id.wa_switch);
        take_pic = (ImageButton) findViewById(R.id.take_pic);
        search_img = (ImageButton) findViewById(R.id.search_img);
        local_search = (ImageButton) findViewById(R.id.local_search);
        upload = (ImageButton) findViewById(R.id.upload);
        next = (ImageButton) findViewById(R.id.next);
        prev = (ImageButton) findViewById(R.id.prev);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        mAdView = findViewById(R.id.adBanner);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        load_banner_ad(mAdView, adRequest);
        load_interstitialAd();

        handle_permissions();

        if(! groupId.equals(TMP_GRP_ID)) {
            add_user_in_group(groupId);
        }

        if(is_wa_enabled)
            wa_switch.setChecked(true);
        else wa_switch.setChecked(false);

        Thread wa_load = new Thread(new Runnable() {
            @Override
            public void run() {
                start_loading_wa_images();
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(groupId.equals(TMP_GRP_ID)) {
                    Toast.makeText(getApplicationContext(), "Oops! you are in whatsapp only mode.", Toast.LENGTH_LONG).show();
                    return;
                }
                handle_permissions();
                upload_from_gallery();
            }
        });

        local_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType(("image/*"));
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SEARCH_LOCALLY);
            }
        });

        take_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(groupId.equals(TMP_GRP_ID)) {
                    Toast.makeText(getApplicationContext(), "Oops! you are in whatsapp only mode.", Toast.LENGTH_LONG).show();
                    return;
                }
                take_photo(UPLOAD_ON_DB);
            }
        });

        search_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                take_photo(SEARCH_FOR_RES);
            }
        });

        /*
        groups_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snap : snapshot.getChildren()) {
                    Upload u = snap.getValue(Upload.class);
                    if(u == null) {
                        Log.d("hello", "data not available kkk");
                        break;
                    }
                    Log.d("hello", u.getFilename());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });*/

        groups_ref.child("data").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.getValue(Upload.class) == null)
                    return;
                else{
                    Upload retrived_img = snapshot.getValue(Upload.class);
                    if(retrived_img == null)
                        return;
                    String local_dir_path = get_local_storage_path();
                    File imgs = new File(local_dir_path);
                    File[] image_list = imgs.listFiles();
                    for (File f : image_list) {
                        if(f.getName().equals(retrived_img.getFilename())) {
                            Bitmap bitmap = load_image_from_storage(local_dir_path, f.getName());
                            if (bitmap == null) {
                                //file cannot be loaded maybe it is folder
                                continue;
                            }
                            try {
                                StringBuilder sb = extract_text_from_image(bitmap);
                                Upload tmp = new Upload(f.getName(), "no uri", sb.toString(), retrived_img.getSenderName());
                                store_retrived_image(tmp);
                            } catch (Exception ex) {
                                Log.d("hello", "Error is : " + ex.getMessage());
                            }

                            return;
                        }
                    }
                    if (! retrived_img.getSenderName().equals(senderName)) {
                        download_image(retrived_img);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                /*
                Upload retrived_img = snapshot.getValue(Upload.class);
                if(retrived_img != null) {
                    String fname = retrived_img.getFilename();
                    Log.d("hello", fname + " gg");
                    tmp_name = fname;
                    StorageReference pathref = storageReference.child(fname);
                    pathref.getBytes(1024 * 1024).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            path = save_file_locally(bitmap, fname);
                            //captured_img.setImageBitmap(load_image_from_storage(path));
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_LONG).show();
                        }
                    });
                }*/
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(prevImages.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "No more images", Toast.LENGTH_LONG).show();
                    return;
                }
                Pair<Integer, Integer> pair = prevImages.pop();
                result.set(curr_image_indx, curr_image_val);
                curr_image_val = pair.first;
                curr_image_indx = pair.second;

                Upload most_matched_img = images.get(curr_image_indx);

                String fname = most_matched_img.getFilename();
                String storageDir;

                // TODO apply better logic
                if((fname.substring(0, 3)).equals("cpy")) {
                    storageDir = get_local_storage_path();
                }
                else {
                    storageDir = wa_path;
                }
                Bitmap img = load_image_from_storage(storageDir, most_matched_img.getFilename());
                String sender= most_matched_img.getSenderName();
                captured_img.setImageBitmap(img);
                pattacher = new PhotoViewAttacher(captured_img);
                pattacher.update();
                Toast.makeText(getApplicationContext(), "Sender: " + sender, Toast.LENGTH_LONG).show();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int max_indx = 0, max = 0;
                for(int i = 0; i < result.size(); i++) {
                    if(result.get(i) > max) {
                        max = result.get(i);
                        max_indx = i;
                    }
                }

                // if no match found
                if(max == 0) {
                    Toast.makeText(getApplicationContext(), "No more matching images.", Toast.LENGTH_LONG).show();
                    return;
                }

                if(max <= POOR_MATCHING)
                    Toast.makeText(getApplicationContext(), "Poor results: It looks like your image is not matching with any image.",
                            Toast.LENGTH_LONG).show();

                prevImages.push(curr_image_val, curr_image_indx);
                curr_image_indx = max_indx;
                curr_image_val = result.get(max_indx);
                result.set(max_indx, 0);
                Upload most_matched_img = images.get(max_indx);

                String fname = most_matched_img.getFilename();
                String storageDir;

                // TODO apply better logic
                if((fname.substring(0, 3)).equals("cpy")) {
                    storageDir = get_local_storage_path();
                }
                else {
                    storageDir = wa_path;
                }
                Bitmap img = load_image_from_storage(storageDir, most_matched_img.getFilename());
                String sender = most_matched_img.getSenderName();
                captured_img.setImageBitmap(img);
                pattacher = new PhotoViewAttacher(captured_img);
                pattacher.update();
                Toast.makeText(getApplicationContext(), "Sender: " + sender, Toast.LENGTH_LONG).show();
            }
        });

        /*
        captured_img.setOnTouchListener(new OnSwipeTouchListener(getApplicationContext()) {
            // TODO onSwipeRight listener
            @Override
            public void onSwipeLeft() {
                Toast.makeText(getApplicationContext(), "swipe left", Toast.LENGTH_LONG).show();
                int max_indx = 0, max = 0;
                for(int i = 0; i < result.size(); i++) {
                    if(result.get(i) > max) {
                        max = result.get(i);
                        max_indx = i;
                    }
                }

                // if no match found
                if(max == 0) {
                    Toast.makeText(getApplicationContext(), "No more matching images.", Toast.LENGTH_LONG).show();
                    return;
                }

                result.set(max_indx, 0);
                Upload most_matched_img = images.get(max_indx);

                String fname = most_matched_img.getFilename();
                String storageDir;

                // TODO apply better logic
                if(fname.substring(0, 3) == "cpy") {
                    storageDir = get_local_storage_path();
                }
                else {
                    storageDir = wa_path;
                }
                Bitmap img = load_image_from_storage(storageDir, most_matched_img.getFilename());
                captured_img.setImageBitmap(img);
                pattacher = new PhotoViewAttacher(captured_img);
                pattacher.update();
            }

            public void onSwipeRight() {
            }

        });*/


        if(is_wa_enabled)
            wa_load.start();
        wa_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(wa_switch.isChecked()) {
                    recent_file_time = System.currentTimeMillis();
                    is_wa_enabled = true;
                    load_wa_images();
                }
                else
                    is_wa_enabled = false;
            }
        });


        /*
        //TODO maybe error is in this event listner
        data_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("Error:", "hello");
                Upload retrived_img = snapshot.getValue(Upload.class);
                if(retrived_img != null) {
                    String fname = retrived_img.getFilename();
                    tmp_name = fname;
                    StorageReference pathref = storageReference.child(fname);
                    pathref.getBytes(1024 * 1024).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            path = save_file_locally(bitmap, fname);
                            //captured_img.setImageBitmap(load_image_from_storage(path));
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });*/
    }



    public class PrevImages {
        private List<Integer> prev_match_val;
        private List<Integer> prev_match_indx;

        public PrevImages() {
            this.prev_match_val = new ArrayList<>();
            this.prev_match_indx = new ArrayList<>();
        }

        public void push(int val, int indx) {
            this.prev_match_indx.add(indx);
            this.prev_match_val.add(val);
        }

        public Pair<Integer, Integer> pop() {
            Pair <Integer, Integer> pair = new Pair(this.prev_match_val.get(prev_match_val.size()-1),
                    this.prev_match_indx.get(prev_match_indx.size()-1));
            this.prev_match_indx.remove(prev_match_indx.size()-1);
            this.prev_match_val.remove(prev_match_val.size()-1);
            return pair;
        }

        public boolean isEmpty() {
            if(prev_match_val.size() == 0)
                return true;
            else return false;
        }
    }

    /*
    public class OnSwipeTouchListener implements View.OnTouchListener {

        private final GestureDetector gestureDetector;

        public OnSwipeTouchListener(Context context) {
            gestureDetector = new GestureDetector(context, new GestureListener());
        }

        public void onSwipeLeft() {
        }

        public void onSwipeRight() {
        }

        public void onSwipeUp() {
        }

        public void onSwipeDown() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }

        private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

            private static final int SWIPE_DISTANCE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float distanceX = e2.getX() - e1.getX();
                float distanceY = e2.getY() - e1.getY();
                if (Math.abs(distanceX) > Math.abs(distanceY) && Math.abs(distanceX) > SWIPE_DISTANCE_THRESHOLD &&
                        Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (distanceX > 0)
                        onSwipeRight();
                    else
                        onSwipeLeft();
                    return true;
                }
                else if(Math.abs(distanceY) > Math.abs(distanceX) && Math.abs(distanceY) > SWIPE_DISTANCE_THRESHOLD &&
                        Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (distanceY > 0)
                        onSwipeDown();
                    else
                        onSwipeUp();
                    return true;
                }
                return false;
            }
        }
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        Intent menuActivity = new Intent(MainActivity.this, ActivityMenu.class);
        menuActivity.putExtra("groupId", groupId);

        int itemId = item.getItemId();
        if(itemId == R.id.group_info) {
            if(groupId.equals(TMP_GRP_ID)) {
                Toast.makeText(getApplicationContext(), "Oops! you are in whatsapp Only mode.", Toast.LENGTH_LONG).show();
                return true;
            }
            else menuActivity.putExtra("action", 0);
        }
        else if(itemId == R.id.feedback)
            menuActivity.putExtra("action", 2);
        else if(itemId == R.id.help)
            menuActivity.putExtra("action", 3);

        startActivity(menuActivity);
        return true;
    }

    private void add_user_in_group(String groupId) {
        FirebaseFirestore ref = FirebaseFirestore.getInstance();
        ref.collection("Groups").document(groupId).get().addOnSuccessListener(
                new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Group group = documentSnapshot.toObject(Group.class);
                        List<String> users = null;
                        if(group != null)
                             users = group.getUsers();
                        for(int i = 0; users!=null &&  i < users.size(); i++) {
                            Log.d("error", users.get(i) + " "+ senderName);
                            if(users.get(i).equals(senderName))
                                return;
                        }

                        group.addUsers(senderName);
                        ref.collection("Groups").document(groupId).set(group);
                    }
                }
        ).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        add_user_in_group(senderName);
                    }
                }, 60000);
            }
        });

    }


    private void handle_permissions() {
        if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},ALL_PERMISSIONS);
        }
        else if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},ALL_PERMISSIONS);
        }
        else if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA},ALL_PERMISSIONS);
        }
        else isPermissionsGranted = true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == ALL_PERMISSIONS)
            handle_permissions();
    }


    private void load_interstitialAd() {
        AdRequest interstitialReq = new AdRequest.Builder().build();
        InterstitialAd.load(this,"ca-app-pub-2142739626174652/5044959491", interstitialReq,
                new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                // The mInterstitialAd reference will be null until
                // an ad is loaded.
                mInterstitialAd = interstitialAd;
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        load_interstitialAd();
                    }
                }, 60000);
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                // Handle the error
                Log.d("error", loadAdError.getMessage());
                mInterstitialAd = null;
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        load_interstitialAd();
                    }
                }, 1000);
            }
        });
    }

    private void load_banner_ad(AdView mAdView, AdRequest adRequest) {
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                super.onAdLoaded();
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                // Code to be executed when an ad request fails.
                Log.d("error", adError.getMessage());
                super.onAdFailedToLoad(adError);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mAdView.loadAd(adRequest);
                    }
                }, 1000);
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        });
    }

    private void download_image(Upload retrived_img) {
        String fname = retrived_img.getFilename();
        StorageReference pathref = storageReference.child(fname);
        pathref.getBytes(3072 * 3072).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                store_retrived_image(retrived_img);
                Toast.makeText(getApplicationContext(), "Downloaded, Sender: " + retrived_img.getSenderName(), Toast.LENGTH_LONG).show();

                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                path = save_file_locally(bitmap, fname);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void upload_from_gallery() {
        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), UPLOAD_RESULT);
    }

    private void start_loading_wa_images() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File imgs = new File(wa_path);
                File[] image_list = imgs.listFiles();

                if(image_list == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Please enable Storage Permission from, " +
                                    "Permissions -> Storage Permission -> Enable", Toast.LENGTH_LONG).show();
                        }
                    });

                    final Handler myhandler = new Handler(Looper.getMainLooper());
                    myhandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        }
                    }, 3000);
                    return;
                }
                int counter = 0;
                for (File f : image_list) {
                    if (f.lastModified() > wa_beginning_time) {
                        counter++;
                    }
                }
                tot_wa_images = counter;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(tot_wa_images < 10)
                            Toast.makeText(getApplicationContext(), "Total whatsapp images to load: "+Integer.valueOf(tot_wa_images-1) , Toast.LENGTH_LONG ).show();
                        else
                            Toast.makeText(getApplicationContext(), "Total whatsapp images to load: "+Integer.valueOf(tot_wa_images-1) +" , Operations will be slow during this time",
                                    Toast.LENGTH_LONG ).show();
                    }
                });

                final Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        File imgs = new File(wa_path);
                        File[] image_list = imgs.listFiles();
                        boolean flag = false;
                        for (File f : image_list) {
                            if (f.lastModified() > wa_beginning_time && f.lastModified() < wa_end_time) {
                                boolean already_stored= false;
                                for(Upload u:images) {
                                    if(u.getFilename().equals(f.getName())) {
                                        already_stored = true;
                                        break;
                                    }
                                }
                                if(already_stored)
                                    continue;
                                Bitmap bitmap = load_image_from_storage(wa_path, f.getName());
                                if (bitmap == null) {
                                    //file cannot be loaded maybe it is folder
                                    continue;
                                }
                                try {
                                    StringBuilder sb = extract_text_from_image(bitmap);
                                    Upload tmp = new Upload(f.getName(), "no uri", sb.toString(), "WA image");
                                    store_retrived_image(tmp);
                                } catch (Exception ex) {
                                    Log.d("hello", "Error is : " + ex.getMessage());
                                }
                                flag = true;
                                break;
                            }
                        }
                        if(flag && is_wa_enabled)
                            handler.postDelayed(this, 40);
                    }
                }, 40);

            }
        }).start();

        load_wa_images();
    }

    private void load_wa_images() {
        File imgs = new File(wa_path);
        File[] image_list = imgs.listFiles();

        if(image_list == null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "To use whatsapp feature you need to enable Storage Permission from, " +
                            "Permissions -> Storage Permission -> Enable", Toast.LENGTH_LONG).show();
                }
            });

            final Handler myhandler = new Handler(Looper.getMainLooper());
            myhandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                }
            }, 3000);
            return;
        }

        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                File imgs = new File(wa_path);
                File[] image_list = imgs.listFiles();
                Long max_lastModified = recent_file_time;
                for (File f : image_list) {
                    if (f.lastModified() > recent_file_time) {
                        Bitmap bitmap = load_image_from_storage(wa_path, f.getName());
                        if (bitmap == null) {
                            //file cannot be loaded maybe it is folder
                            continue;
                        }
                        if (f.lastModified() > max_lastModified)
                            max_lastModified = f.lastModified();
                        try {
                            StringBuilder sb = extract_text_from_image(bitmap);
                            Upload tmp = new Upload(f.getName(), "no uri", sb.toString(), "WA image");
                            store_retrived_image(tmp);
                        } catch (Exception ex) {
                            Log.d("hello", "Error is : " + ex.getMessage());
                        }
                        count++;
                    }
                }
                recent_file_time = max_lastModified;
                if(is_wa_enabled)
                    handler.postDelayed(this, 2000);
            }
        }, 2000);
    }

    private void update_imageCnt() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView txt = findViewById(R.id.totImages);
                txt.setText(Integer.toString(imageCnt));
                return;
            }
        });
    }

    private void store_retrived_image(Upload retrived_img) {
        // store info in list for further use
        int value = images.size();
        images.add(retrived_img);
        imageCnt = images.size();
        update_imageCnt();

        String lines[] = retrived_img.getImg_text().split("\n");
        for(String line : lines) {
            String words[] = line.split(" ");
            for(String word : words) {
                // if corresponding key doesn't have any value
                if(store_txt.get(word) == null) {
                    // create new index for that image
                    List<Integer> tmp= new ArrayList<>();
                    tmp.add(value);
                    store_txt.put(word, tmp);
                }
                // if key already present add value
                else
                    store_txt.get(word).add(value);
            }
        }
    }

    private void take_photo(int code) {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filename = "cpy_" + timestamp;
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File image = File.createTempFile(filename, ".jpg", storageDir);
            imgPath = image.getAbsolutePath();

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            imageUri = FileProvider.getUriForFile(this, "com.suraj.cpy.fileprovider", image);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent, code);
        }
        catch(Exception ex) {
            Log.d("Error:", ex.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == UPLOAD_RESULT && resultCode == Activity.RESULT_OK && data != null) {
            if(data.getClipData() != null) {
                ClipData mclipdata = data.getClipData();
                if(mclipdata.getItemCount() > 20) {
                    Toast.makeText(getApplicationContext(), "You can upload maximum 20 images at a time", Toast.LENGTH_LONG).show();
                    return;
                }
                for(int i = 0; i < mclipdata.getItemCount(); i++) {
                    ClipData.Item item = mclipdata.getItemAt(i);
                    Uri uri = item.getUri();
                    Bitmap bit;
                    try {
                        InputStream is =getContentResolver().openInputStream(uri);
                        bit = BitmapFactory.decodeStream(is);
                    }catch (Exception ex) {
                        Log.d("error", ex.getMessage());
                        return;
                    }
                    if(bit == null)
                        return;

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            upload_on_db(bit);
                        }
                    }).start();
                }
            }
            else if (data.getData() != null ) {
                Uri uri= data.getData();
                Bitmap bit;
                try {
                    InputStream is =getContentResolver().openInputStream(uri);
                    bit = BitmapFactory.decodeStream(is);

                }catch (Exception ex) {
                    Log.d("error", ex.getMessage());
                    return;
                }
                if(bit == null)
                    return;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        upload_on_db(bit);
                    }
                }).start();
            }

        }

        else if(requestCode == SEARCH_LOCALLY && resultCode == Activity.RESULT_OK) {
            Uri uri= data.getData();
            Bitmap bit = null;
            try {
                InputStream is =getContentResolver().openInputStream(uri);
                bit = BitmapFactory.decodeStream(is);
            }catch (Exception ex) {
                Log.d("error", ex.getMessage());
                return;
            }

            search_for_image(bit);
        }

        else if ((requestCode == UPLOAD_ON_DB || requestCode == SEARCH_FOR_RES) && resultCode == Activity.RESULT_OK) {
            Bitmap bit;
            try {
                bit = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            delete_file(imgPath);
            // Process image and store image and its metadata on database
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if(requestCode == UPLOAD_ON_DB)
                        upload_on_db(bit);
                    else if(requestCode == SEARCH_FOR_RES)
                        search_for_image(bit);
                }
            }).start();
        }
    }

    private StringBuilder extract_text_from_image(Bitmap bitmap) {
        // extract text
        if(bitmap == null) {
            return new StringBuilder();
        }
        TextRecognizer recognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        StringBuilder sb = new StringBuilder();
        if (!recognizer.isOperational()) {
            Toast.makeText(getApplicationContext(), "Text OCR error", Toast.LENGTH_LONG).show();
        } else {
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> items = recognizer.detect(frame);
            for (int i = 0; i < items.size(); i++) {
                TextBlock myItem = items.valueAt(i);
                sb.append(myItem.getValue());
                sb.append(" ");
            }
        }
        return sb;
    }

    /*
    private void upload_on_database(String fname, StringBuilder sb) {
        if(imageBitmap == null) {
            Log.d("error", "imagebitmap null");
            return;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 25, baos);
        byte[] compressed_file = baos.toByteArray();

        StorageReference ref = storageReference.child(fname);
        ref.putBytes(compressed_file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Upload upload = new Upload(fname, "no uri", sb.toString(), senderName);
                DatabaseReference grp_data = groups_ref.child("data").push();
                String key = grp_data.getKey();
                groups_ref.child("data").child(key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        groups_ref.child("data").child(key).setValue(upload);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                Toast.makeText(getApplicationContext(), "Image Uploaded Successfully.", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Error:", e.getMessage());
                Toast.makeText(getApplicationContext(), "Image is not uploded.", Toast.LENGTH_LONG).show();
            }
        });
    }*/

    // TODO similar to above updating to use non global variables
    private void upload_on_db(Bitmap bit) {
        if(bit == null) {
            return;
        }

        StringBuilder sb = extract_text_from_image(bit);

        Bitmap newbit = Bitmap.createBitmap(bit.getWidth(), bit.getHeight(),bit.getConfig());
        Canvas canvas = new Canvas(newbit);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bit, 0, 0, null);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        newbit.compress(Bitmap.CompressFormat.JPEG,25, outputStream);

        byte[] cfile = outputStream.toByteArray();

        int size = cfile.length/1024;
        Log.d("error", Integer.toString(size));
        if(size < 200) {
            outputStream.reset();
            newbit.compress(Bitmap.CompressFormat.JPEG, 18, outputStream);
        }
        else if(size < 400) {
            outputStream.reset();
            newbit.compress(Bitmap.CompressFormat.JPEG, 15, outputStream);
        }
        else if(size < 600) {
            outputStream.reset();
            newbit.compress(Bitmap.CompressFormat.JPEG, 12, outputStream);
        }
        else if(size < 1000) {
            outputStream.reset();
            newbit.compress(Bitmap.CompressFormat.JPEG, 11, outputStream);
        }
        else if (size < 2000) {
            outputStream.reset();
            newbit.compress(Bitmap.CompressFormat.JPEG, 9, outputStream);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Warning: Camera images have too high resolution, " +
                            "compression will reduce image quality badly", Toast.LENGTH_LONG).show();
                }
            });
        }
        else{
            outputStream.reset();
            newbit.compress(Bitmap.CompressFormat.JPEG, 8, outputStream);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Warning: Camera images have too high resolution, " +
                            "compression will reduce image quality badly", Toast.LENGTH_LONG).show();
                }
            });
        }
        /*
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bit.compress(Bitmap.CompressFormat.JPEG, 1, baos);*/
        /*
        int options = 40;
        Log.d("error", Integer.toString(options));
        while(baos.toByteArray().length/1024 > MAX_IMAGE_SIZE) {
            baos.reset();
            bit.compress(Bitmap.CompressFormat.JPEG, options, baos);
            Log.d("error", Integer.toString(options));
            options-=10;
        }*/

        byte[] compressed_file = outputStream.toByteArray();

        String timestamp = Long.toString(System.currentTimeMillis());
        String fname = "cpy_" + timestamp;

        StorageReference ref = storageReference.child(fname);
        ref.putBytes(compressed_file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Upload upload = new Upload(fname, "no uri", sb.toString(), senderName);
                DatabaseReference grp_data = groups_ref.child("data").push();
                String key = grp_data.getKey();
                groups_ref.child("data").child(key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        groups_ref.child("data").child(key).setValue(upload);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Image Uploaded Successfully.", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                ref.putBytes(compressed_file);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Error: Image is not uploded, Retrying...", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private String save_file_locally(Bitmap bitmap, String fname) {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File mypath = new File(storageDir, fname);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return storageDir.getAbsolutePath();
    }

    private String get_local_storage_path() {
        return getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
    }

    private Bitmap load_image_from_storage(String path, String fname) {
        Bitmap b = null;
        try {
            File f = new File(path, fname);
            b = BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return b;
    }

    private void delete_file(String file_path) {
        File file = new File(file_path);
        if(file.exists()) {
            file.delete();
        }
    }

    private int count_words(String img_text) {
        String lines[] = img_text.split("\n");
        int size = 0;
        for(String line : lines) {
            String words[] = line.split(" ");
            size+= words.length;
        }
        return size;
    }

    private void search_for_image(Bitmap bitmap) {
        StringBuilder sb = extract_text_from_image(bitmap);
        String img_text = sb.toString();

        result = new ArrayList<>();
        for(int i = 0; i < images.size(); i++) {
            result.add(0);
        }

        int word_count = 0;
        String lines[] = img_text.split("\n");
        for(String line : lines) {
            String words[] = line.split(" ");
            word_count+= words.length;
            for(String word : words) {
                List<Integer> match;
                match = store_txt.get(word);
                if(match != null) {
                    for(int i = 0; i < match.size(); i++) {
                        int indx = match.get(i);
                        int value = result.get(indx) + 1;
                        result.set(indx, value);
                    }
                }
            }
        }

        for(int i = 0; i < result.size(); i++) {
            int match_count = result.get(i);
            float mainImg = match_count*100 / word_count;
            int matchedImg_words = count_words(images.get(i).getImg_text());
            float matchedImg = match_count*100 / matchedImg_words;
            float resultant = (mainImg * 60 /100) + (matchedImg * 40 / 100);
            result.set(i, Math.round(resultant));
        }

        int max_indx = 0, max = 0;
        for(int i = 0; i < result.size(); i++) {
            if(result.get(i) > max) {
                max = result.get(i);
                max_indx = i;
            }
        }

        if(max == 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "No match found.", Toast.LENGTH_LONG).show();
                }
            });
            return;
        }

        if (result.get(max_indx) <= POOR_MATCHING) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Poor results: It looks like your image is not matching with any image.",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }

        curr_image_indx = max_indx;
        curr_image_val = result.get(max_indx);
        result.set(max_indx, 0);
        Upload most_matched_img = images.get(max_indx);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String fname = most_matched_img.getFilename();
                String sender = most_matched_img.getSenderName();
                String storageDir;

                // TODO apply better logic
                if((fname.substring(0, 3)).equals("cpy"))
                    storageDir = get_local_storage_path();
                else storageDir = wa_path;
                Bitmap img = load_image_from_storage(storageDir, most_matched_img.getFilename());
                captured_img.setImageBitmap(img);
                PhotoViewAttacher pattacher = new PhotoViewAttacher(captured_img);
                pattacher.update();
                Toast.makeText(getApplicationContext(), "Sender: " + sender, Toast.LENGTH_LONG).show();
            }
        });
    }
}
