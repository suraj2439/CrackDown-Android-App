package com.suraj.cpy;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GroupInfo#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GroupInfo extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public GroupInfo() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GroupInfo.
     */
    // TODO: Rename and change types and number of parameters
    public static GroupInfo newInstance(String param1, String param2) {
        GroupInfo fragment = new GroupInfo();
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

        View view = inflater.inflate(R.layout.fragment_group_info, container, false);

        ((ActivityMenu)getActivity()).progressBar.startProgressBar();

        LinearLayout layout = (LinearLayout)view.findViewById(R.id.infoMembers);

        FirebaseFirestore ref = FirebaseFirestore.getInstance();
        ref.collection("Groups").document(((ActivityMenu) getActivity()).groupId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        ((ActivityMenu)getActivity()).progressBar.dismisprogressBar();

                        Group group = documentSnapshot.toObject(Group.class);

                        TextView grpId = view.findViewById(R.id.infoGrpId);
                        grpId.setText(((ActivityMenu) getActivity()).groupId);
                        TextView admin = view.findViewById(R.id.infoAdmin);
                        admin.setText(group.getAdmin());

                        List<String> users = null;
                        if (group != null)
                            users = group.getUsers();
                        for (int i = 0; users != null && i < users.size(); i++) {
                            TextView txt = new TextView(getContext());
                            txt.setText(users.get(i));
                            txt.setTextColor(0xFFFFFFFF);
                            layout.addView(txt);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                ((ActivityMenu)getActivity()).progressBar.dismisprogressBar();
                Toast.makeText(getContext(), "Oops! Something went wrong please try again later.", Toast.LENGTH_LONG);
            }
        });

        return view;
    }
}