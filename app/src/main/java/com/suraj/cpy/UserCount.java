package com.suraj.cpy;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;

public class UserCount {
    private int visited;
    private int signup;
    private int createGroup;
    private int joinGroup;
    private int whatsappOnly;

//    public UserCount(int visited, int signup, int createGroup, int joinGroup, int whatsappOnly) {
//        this.visited = visited;
//        this.signup = signup;
//        this.createGroup = createGroup;
//        this.joinGroup = joinGroup;
//        this.whatsappOnly = whatsappOnly;
//    }

    public UserCount() {
        this.visited = 0;
        this.signup = 0;
        this.createGroup = 0;
        this.joinGroup = 0;
        this.whatsappOnly = 0;
    }

    public int getVisited() {
        return visited;
    }

    public void setVisited(int visited) {
        this.visited = visited;
    }

    public int getSignup() {
        return signup;
    }

    public void setSignup(int signup) {
        this.signup = signup;
    }

    public int getCreateGroup() {
        return createGroup;
    }

    public void setCreateGroup(int createGroup) {
        this.createGroup = createGroup;
    }

    public int getJoinGroup() {
        return joinGroup;
    }

    public void setJoinGroup(int joinGroup) {
        this.joinGroup = joinGroup;
    }

    public int getWhatsappOnly() {
        return whatsappOnly;
    }

    public void setWhatsappOnly(int whatsappOnly) {
        this.whatsappOnly = whatsappOnly;
    }

    public static void incrUserCount(String type) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                Date date = new Date();
                String currDate = formatter.format(date);
                currDate = currDate.replace("/", ".");
                Log.d("hello", currDate);

                FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
                DocumentReference docIdRef = rootRef.collection("UserCount").document(currDate);
                docIdRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d("hello", "Document exists!");
                                UserCount userCount = document.toObject(UserCount.class);
                                if(type == "visited")
                                    userCount.setVisited(userCount.getVisited()+1);
                                else if(type == "signup")
                                    userCount.setSignup(userCount.getSignup()+1);
                                else if(type == "createGroup")
                                    userCount.setCreateGroup(userCount.getCreateGroup()+1);
                                else if(type == "joinGroup")
                                    userCount.setJoinGroup(userCount.getJoinGroup()+1);
                                else if(type == "whatsappOnly")
                                    userCount.setWhatsappOnly(userCount.getWhatsappOnly()+1);
                                docIdRef.set(userCount);
                            } else {
                                UserCount userCount = new UserCount();
                                if(type == "visited")
                                    userCount.setVisited(userCount.getVisited()+1);
                                else if(type == "signup")
                                    userCount.setSignup(userCount.getSignup()+1);
                                else if(type == "createGroup")
                                    userCount.setCreateGroup(userCount.getCreateGroup()+1);
                                else if(type == "joinGroup")
                                    userCount.setJoinGroup(userCount.getJoinGroup()+1);
                                else if(type == "whatsappOnly")
                                    userCount.setWhatsappOnly(userCount.getWhatsappOnly()+1);
                                docIdRef.set(userCount);
                                Log.d("hello", "Document does not exist!");
                            }
                        } else {
                            Log.d("hello", "Failed with: ", task.getException());
                        }
                    }
                });
            }
        }).start();
    }
}
