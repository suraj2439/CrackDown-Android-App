package com.suraj.cpy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link groups#newInstance} factory method to
 * create an instance of this fragment.
 */
public class groups extends Fragment {
    private int click_event;
    String groupId="0";
    static final int CREATE_GRP_EVENT = 3;
    static final int JOIN_GRP_EVENT = 5;
    static final int WA_MODE_EVENT = 7;
    static final String TMP_GRP_ID = "99999";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private String senderName = "undefine";
    private Group group;
    private boolean is_wa_enabled = false;

    public groups() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment groups.
     */
    // TODO: Rename and change types and number of parameters
    public static groups newInstance(String param1, String param2) {
        groups fragment = new groups();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        EditText manual_path = (EditText)view.findViewById(R.id.manual_path);
        manual_path.setText(Environment.getExternalStorageDirectory().getAbsolutePath() + "/WhatsApp/Media/WhatsApp Images");

        ((PrimaryTask)getActivity()).progressBar.startProgressBar();

        if(! ((PrimaryTask)getActivity()).is_right_version) {
            Toast.makeText(getContext(), "You are not allowed to use this version of app, please update your app",
                    Toast.LENGTH_LONG).show();
            return view;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("Users").document(((PrimaryTask)getActivity()).fuser .getUid()).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                ((PrimaryTask)getActivity()).progressBar.dismisprogressBar();
                                User curr_user = documentSnapshot.toObject(User.class);
                                if(curr_user != null)
                                    senderName = curr_user.getName();
                                else senderName = "Unknown";
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        ((PrimaryTask)getActivity()).progressBar.dismisprogressBar();
                        senderName = "Unknown";
                    }
                });
            }
        }).start();

        Switch wa_feature = (Switch)view.findViewById(R.id.wa);
        Button create_grp = (Button) view.findViewById(R.id.create_grp);
        Button wa_mode = (Button) view.findViewById(R.id.wa_mode);

        wa_feature.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                EditText time = (EditText) view.findViewById(R.id.wa_time);
                if (wa_feature.isChecked()) {
                    time.setEnabled(true);
                    Toast.makeText(getContext(), "Enter time to collect previous images.", Toast.LENGTH_LONG).show();
                    is_wa_enabled = true;
                } else {
                    time.setEnabled(false);
                    is_wa_enabled = false;
                }
            }
        });

        create_grp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(senderName.equals("undefine")) {
                    Toast.makeText(getContext(), "Please wait, processing your request", Toast.LENGTH_LONG).show();
                    return;
                }
                SharedPreferences sp = ((PrimaryTask)getActivity()).login_details;
                int createGrpNo = sp.getInt("createGrpNo", 0);

                if(createGrpNo < 15) {
                    createGrpNo++;
                    ((PrimaryTask)getActivity()).editor.putInt("createGrpNo", createGrpNo);
                    ((PrimaryTask)getActivity()).editor.commit();
                }
                else if(((PrimaryTask)getActivity()).mInterstitialAd != null) {
                    ((PrimaryTask)getActivity()).mInterstitialAd.show((PrimaryTask)getActivity());
                }

                click_event = CREATE_GRP_EVENT;
                groupId = Long.toString(System.currentTimeMillis());
                groupId = groupId.substring(2);

                group = new Group(System.currentTimeMillis(), senderName);

                // TODO failure handling
                FirebaseFirestore fbase = FirebaseFirestore.getInstance();
                fbase.collection("Groups").document(groupId)
                        .set(group).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Error: Group is not created, try again after some time.", Toast.LENGTH_LONG).show();
                        click_event = -1;
                        return;
                    }
                });

                TextView grpId_txt = view.findViewById(R.id.grp_id_txt);
                EditText grpId = view.findViewById(R.id.grp_id);
                Button proceed = view.findViewById(R.id.proceed);

                grpId_txt.setVisibility(View.VISIBLE);
                grpId.setVisibility(View.VISIBLE);
                proceed.setVisibility(View.VISIBLE);

                grpId.setText(groupId);
                Toast.makeText(getActivity(), "Share Group Id to join other members.", Toast.LENGTH_LONG).show();
            }
        });

        Button join_grp = (Button) view.findViewById(R.id.join_grp);
        join_grp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(senderName.equals("undefine")) {
                    Toast.makeText(getContext(), "Please wait, processing your request", Toast.LENGTH_LONG).show();
                    return;
                }
                SharedPreferences sp = ((PrimaryTask)getActivity()).login_details;
                int joinGrpNo = sp.getInt("joinGrpNo", 0);

                if(joinGrpNo < 20) {
                    joinGrpNo++;
                    ((PrimaryTask)getActivity()).editor.putInt("joinGrpNo", joinGrpNo);
                    ((PrimaryTask)getActivity()).editor.commit();
                }
                else if(((PrimaryTask)getActivity()).mInterstitialAd != null) {
                    ((PrimaryTask)getActivity()).mInterstitialAd.show((PrimaryTask)getActivity());
                }

                click_event = JOIN_GRP_EVENT;
                TextView grpId_txt = view.findViewById(R.id.grp_id_txt);
                EditText grpId = view.findViewById(R.id.grp_id);
                Button proceed = view.findViewById(R.id.proceed);

                grpId_txt.setVisibility(View.VISIBLE);
                grpId.setVisibility(View.VISIBLE);
                proceed.setVisibility(View.VISIBLE);

                SharedPreferences spgrp = ((PrimaryTask)getActivity()).groupId;
                String prevGrpId = spgrp.getString("GroupId", "");
                Log.d("error", prevGrpId);
                grpId.setText(prevGrpId);
            }
        });

        Button proceed = view.findViewById(R.id.proceed);
        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((PrimaryTask)getActivity()).progressBar.startProgressBar();

                EditText grpId = view.findViewById(R.id.grp_id);
                EditText wa = view.findViewById(R.id.wa_time);
                String time = wa.getText().toString();

                if(time.isEmpty()) {
                    ((PrimaryTask)getActivity()).progressBar.dismisprogressBar();
                    wa.setError("Required");
                    wa.requestFocus();
                    return;
                }

                Intent mainActivity = new Intent(getActivity(), MainActivity.class);
                mainActivity.putExtra("waTime", time);
                if(click_event == JOIN_GRP_EVENT) {
                    String groupId = grpId.getText().toString();

                    if(groupId.isEmpty()) {
                        ((PrimaryTask)getActivity()).progressBar.dismisprogressBar();
                        grpId.setError("Required");
                        grpId.requestFocus();
                        return;
                    }

                    ((PrimaryTask)getActivity()).groupIdEditor.putString("GroupId", groupId);
                    ((PrimaryTask)getActivity()).groupIdEditor.commit();

                    FirebaseFirestore dbase = FirebaseFirestore.getInstance();
                    dbase.collection("Groups").document(groupId).get()
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    ((PrimaryTask)getActivity()).progressBar.dismisprogressBar();
                                    Toast.makeText(getContext(), "Error: Check your group Id or Internet connection", Toast.LENGTH_LONG).show();
                                }
                            }).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            ((PrimaryTask)getActivity()).progressBar.dismisprogressBar();
                            if(documentSnapshot.exists()) {
                                Group group = documentSnapshot.toObject(Group.class);
                                if(System.currentTimeMillis() - group.getTimestamp() < 86400000)
                                    Toast.makeText(getContext(), "Group Exists", Toast.LENGTH_LONG).show();
                                else {
                                    Toast.makeText(getContext(), "This group no longer exist, " +
                                            "you can use one group for 1 day only, Please create new group", Toast.LENGTH_LONG).show();
                                    return;
                                }
                            }
                            else {
                                Toast.makeText(getContext(), "Group does not exist, check Group Id again.", Toast.LENGTH_LONG).show();
                                return;
                            }
                            mainActivity.putExtra("adClicks", ((PrimaryTask) getActivity()).adClicks);
                            mainActivity.putExtra("wa_feature", is_wa_enabled);
                            mainActivity.putExtra("senderName", senderName);
                            mainActivity.putExtra("userId", ((PrimaryTask) getActivity()).fuser);
                            mainActivity.putExtra("groupId", groupId);
                            mainActivity.putExtra("wa_path", manual_path.getText().toString());
                            startActivity(mainActivity);
                        }
                    });
                }
                else if(click_event == CREATE_GRP_EVENT) {
                    ((PrimaryTask) getActivity()).progressBar.dismisprogressBar();
                    mainActivity.putExtra("wa_feature", is_wa_enabled);
                    mainActivity.putExtra("senderName", senderName);
                    mainActivity.putExtra("userId", ((PrimaryTask) getActivity()).fuser);
                    mainActivity.putExtra("groupId", groupId);
                    mainActivity.putExtra("wa_path", manual_path.getText().toString());
                    startActivity(mainActivity);
                }
            }
        });

        wa_mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wa_feature.setChecked(true);
                proceed.setVisibility(View.VISIBLE);

                if(senderName.equals("undefine")) {
                    Toast.makeText(getContext(), "Please wait, processing your request", Toast.LENGTH_LONG).show();
                    return;
                }
                SharedPreferences sp = ((PrimaryTask)getActivity()).login_details;
                int createGrpNo = sp.getInt("createGrpNo", 0);

                if(createGrpNo < 15) {
                    createGrpNo++;
                    ((PrimaryTask)getActivity()).editor.putInt("createGrpNo", createGrpNo);
                    ((PrimaryTask)getActivity()).editor.commit();
                }
                else if(((PrimaryTask)getActivity()).mInterstitialAd != null) {
                    ((PrimaryTask)getActivity()).mInterstitialAd.show((PrimaryTask)getActivity());
                }

                click_event = WA_MODE_EVENT;
                Intent mainActivity = new Intent(getActivity(), MainActivity.class);
                ((PrimaryTask)getActivity()).progressBar.dismisprogressBar();
                mainActivity.putExtra("wa_feature", is_wa_enabled);
                mainActivity.putExtra("senderName", "Undefined");
                mainActivity.putExtra("userId", ((PrimaryTask) getActivity()).fuser);
                mainActivity.putExtra("groupId", TMP_GRP_ID);
                mainActivity.putExtra("wa_path", manual_path.getText().toString());
                startActivity(mainActivity);
            }
        });

        return view;
    }

}