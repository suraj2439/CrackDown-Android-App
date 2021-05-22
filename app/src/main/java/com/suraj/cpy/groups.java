package com.suraj.cpy;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link groups#newInstance} factory method to
 * create an instance of this fragment.
 */
public class groups extends Fragment {
    private int click_event;
    String groupId;
    static final int CREATE_GRP_EVENT = 3;
    static final int JOIN_GRP_EVENT = 5;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

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
        Log.d("hello", "in group fragment");
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        Button create_grp = (Button) view.findViewById(R.id.create_grp);
        create_grp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                click_event = CREATE_GRP_EVENT;
                groupId = Long.toString(System.currentTimeMillis());
                groupId = groupId.substring(2);

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
                click_event = JOIN_GRP_EVENT;
                TextView grpId_txt = view.findViewById(R.id.grp_id_txt);
                EditText grpId = view.findViewById(R.id.grp_id);
                Button proceed = view.findViewById(R.id.proceed);

                grpId_txt.setVisibility(View.VISIBLE);
                grpId.setVisibility(View.VISIBLE);
                proceed.setVisibility(View.VISIBLE);
            }
        });

        Button proceed = view.findViewById(R.id.proceed);
        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText wa = view.findViewById(R.id.wa_time);
                String time = wa.getText().toString();

                Intent mainActivity = new Intent(getActivity(), MainActivity.class);
                mainActivity.putExtra("waTime", time);
                if(click_event == JOIN_GRP_EVENT) {
                    EditText grpId = view.findViewById(R.id.grp_id);
                    String groupId = grpId.getText().toString();

                    mainActivity.putExtra("senderName", ((PrimaryTask) getActivity()).senderName);
                    mainActivity.putExtra("groupId", groupId);
                    startActivity(mainActivity);
                }
                else if(click_event == CREATE_GRP_EVENT) {
                    mainActivity.putExtra("senderName", ((PrimaryTask)getActivity()).senderName);
                    mainActivity.putExtra("groupId", groupId);
                    startActivity(mainActivity);
                }
            }
        });

        return view;
    }
}