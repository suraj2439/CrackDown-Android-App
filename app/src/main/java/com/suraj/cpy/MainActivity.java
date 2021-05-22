package com.suraj.cpy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    String groupId;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int UPLOAD_ON_DB  = 10;
    static final int SEARCH_FOR_RES = 20;
    ImageView captured_img;
    Bitmap imageBitmap;
    Button take_pic;
    Button search_img;
    TextView show_txt;
    Uri imageUri;
    //TODO take input sender name
    String senderName = "";
    String filename;
    String imgPath;
    HashMap<String, List<Integer>> store_txt = new HashMap<>();
    List<Upload> images = new ArrayList<>();
    List<Integer> result = new ArrayList<>();
    final int PICTURE_RES = 101;
    // Create a Cloud Storage reference from the app
    FirebaseStorage storage;
    StorageReference storageReference;
    static int count = 0;
    String path;
    String wa_path = "/storage/emulated/0/WhatsApp/Media/WhatsApp Images";

    // Write a message to the database
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference groups_ref;

    Long recent_file_time;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle extra = getIntent().getExtras();
        senderName = extra.getString("senderName");
        groupId = extra.getString("groupId");
        recent_file_time = System.currentTimeMillis() - Long.valueOf(extra.getString("waTime")) * 60 * 1000;
        groups_ref = db.getReference(groupId);

        captured_img = (ImageView) findViewById(R.id.captured_img);
        take_pic = (Button) findViewById(R.id.take_pic);
        search_img = (Button) findViewById(R.id.search_img);
        show_txt = (TextView) findViewById(R.id.show_txt);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        Thread wa_load = new Thread(new Runnable() {
            @Override
            public void run() {
                load_wa_images();
            }
        });

        Log.d("hello", "start");

        take_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                take_photo(UPLOAD_ON_DB);
            }
        });

        search_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                take_photo(SEARCH_FOR_RES);
            }
        });

        groups_ref.child("data").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.getValue(Upload.class) == null) {
                    Log.d("hello", "data not available");
                }
                else{
                    Upload retrived_img = snapshot.getValue(Upload.class);
                    if (retrived_img != null && retrived_img.getSenderName() != senderName) {

                        String fname = retrived_img.getFilename();
                        StorageReference pathref = storageReference.child(fname);
                        pathref.getBytes(3072 * 3072).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                store_retrived_image(retrived_img);
                                Toast.makeText(getApplicationContext(), "Downloaded, Sender: " + retrived_img.getSenderName(), Toast.LENGTH_LONG).show();

                                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                path = save_file_locally(bitmap, fname);
                                //captured_img.setImageBitmap(load_image_from_storage(path, filename));
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_LONG).show();
                            }
                        });
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

        captured_img.setOnTouchListener(new OnSwipeTouchListener(getApplicationContext()) {
            // TODO onSwipeRight listener
            @Override
            public void onSwipeLeft() {
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
            }

            public void onSwipeRight() {

            }
        });


        wa_load.start();


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

    public class OnSwipeTouchListener implements View.OnTouchListener {

        private final GestureDetector gestureDetector;

        public OnSwipeTouchListener(Context context) {
            gestureDetector = new GestureDetector(context, new GestureListener());
        }

        public void onSwipeLeft() {
        }

        public void onSwipeRight() {
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
                return false;
            }
        }
    }

    private void load_wa_images() {
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("hello", "start wa image");
                File imgs = new File(wa_path);
                File[] image_list = imgs.listFiles();
                Long max_lastModified = recent_file_time;
                Log.d("hello", Long.toString(recent_file_time));
                int count = 0;
                for (File f : image_list) {
                    //Log.d("hello", Long.toString(f.lastModified()) + "  "+ Long.toString(recent_file_time) + "  "+Long.toString(System.currentTimeMillis()));
                    if (f.lastModified() > recent_file_time) {
                        Bitmap bitmap = load_image_from_storage(wa_path, f.getName());
                        if (bitmap == null) {
                            //file cannot be loaded maybe it is folder
                            continue;
                        }
                        if (f.lastModified() > max_lastModified)
                            max_lastModified = f.lastModified();
                        //Log.d("hello", "wa image dl: "+ f.getName());
                        Log.d("hello", "tot images: " + Integer.toString(count));
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
                Log.d("hello", "final images: " + Integer.toString(count));
                Log.d("hello", Long.toString(recent_file_time));
                recent_file_time = max_lastModified;
                handler.postDelayed(this, 2000);
            }
        }, 2000);
    }

    private void store_retrived_image(Upload retrived_img) {
        // store info in list for further use
        int value = images.size();
        images.add(retrived_img);
        String lines[] = retrived_img.getImg_text().split("\n");
        for(String line : lines) {
            String words[] = line.split(" ");
            for(String word : words) {
                // if corresponding key doesn't have any value
                if(store_txt.get(word) == null) {
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
            filename = "cpy_" + timestamp;
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File image = File.createTempFile(filename, ".jpg", storageDir);
            imgPath = image.getAbsolutePath();

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //File file = new File(Environment.getExternalStorageDirectory(), "/cpyPaste/a" + "/photo_" + timestamp + ".png");
            imageUri = FileProvider.getUriForFile(this, "com.suraj.cpy.fileprovider", image);
            //intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            //intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            //ContentValues values = new ContentValues();
            //values.put(MediaStore.Images.Media.TITLE, "New Pic");
            //imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

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

        if (resultCode == Activity.RESULT_OK) {
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            delete_file(imgPath);

            String fname = filename;
            // Process image and store image and its metadata on database
            new Thread(new Runnable() {
                @Override
                public void run() {
                    StringBuilder sb = extract_text_from_image(imageBitmap);

                    if(requestCode == UPLOAD_ON_DB)
                        upload_on_database(fname, sb);
                    else if(requestCode == SEARCH_FOR_RES)
                        search_for_image(sb.toString());

                                /*
                                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        //Toast.makeText(getApplicationContext(), "Download url success" + String.valueOf(uri), Toast.LENGTH_LONG).show();
                                        Upload upload = new Upload(filename, String.valueOf(uri), "hello", "yerkal");
                                        data_ref.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                data_ref.setValue(upload);
                                                Toast.makeText(getApplicationContext(), "Download url success" + String.valueOf(uri), Toast.LENGTH_LONG).show();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getApplicationContext(), "Download url fail", Toast.LENGTH_LONG).show();
                                    }
                                });*/
                }
            }).start();
        }
    }
        /*
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            captured_img.setImageBitmap(imageBitmap);
            TextRecognizer recognizer = new TextRecognizer.Builder(getApplicationContext()).build();
            if(!recognizer.isOperational()) {
                Toast.makeText(this, "Error", Toast.LENGTH_LONG);
            }
            else {
                Frame frame = new Frame.Builder().setBitmap(imageBitmap).build();
                SparseArray<TextBlock> items = recognizer.detect(frame);
                StringBuilder sb = new StringBuilder();
                for(int i=0; i < items.size(); i++ ) {
                    TextBlock myItem = items.valueAt(i);
                    sb.append(myItem.getValue());
                    sb.append("\n");
                }
                show_txt.setText(sb.toString());
            }
        }*/

    private StringBuilder extract_text_from_image(Bitmap bitmap) {
        // extract text
        TextRecognizer recognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        StringBuilder sb = new StringBuilder();
        if (!recognizer.isOperational()) {
            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
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

    private void upload_on_database(String fname, StringBuilder sb) {
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

    private void search_for_image(String img_text) {
        result = new ArrayList<>();
        for(int i = 0; i < images.size(); i++) {
            result.add(0);
        }

        String lines[] = img_text.split("\n");
        for(String line : lines) {
            String words[] = line.split(" ");
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

        result.set(max_indx, 0);
        Upload most_matched_img = images.get(max_indx);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
            }
        });
    }
}

