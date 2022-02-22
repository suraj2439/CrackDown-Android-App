package com.suraj.cpy;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link signup#newInstance} factory method to
 * create an instance of this fragment.
 */
public class signup extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private EditText rname, remail, rpswd, rconfirm;
    private Button signup;
    private String name, email, pswd, confirm;
    private FirebaseAuth auth;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public signup() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment signup.
     */
    // TODO: Rename and change types and number of parameters
    public static signup newInstance(String param1, String param2) {
        signup fragment = new signup();
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

        View view = inflater.inflate(R.layout.fragment_signup, container, false);
        UserCount.incrUserCount("signup");

        auth = FirebaseAuth.getInstance();

        rname = (EditText) view.findViewById(R.id.signup_name);
        remail = (EditText)view.findViewById(R.id.signup_email);
        rpswd = (EditText)view.findViewById(R.id.signup_pswd);
        rconfirm = (EditText)view.findViewById(R.id.signup_reconfirm);

        Button register_user = view.findViewById(R.id.register_user);
        register_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUser();
            }
        });
        // Inflate the layout for this fragment
        return view;
    }
    private void createUser() {
        name = rname.getText().toString();
        email = remail.getText().toString();
        pswd = rpswd.getText().toString();
        confirm = rconfirm.getText().toString();
        if(name.isEmpty()) {
            rname.setError("Required");
            rname.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            remail.setError("Required");
            remail.requestFocus();
            return;
        }
        if(pswd.isEmpty()) {
            rpswd.setError("Required");
            rpswd.requestFocus();
            return;
        }
        if(confirm.isEmpty()) {
            rconfirm.setError("Required");
            rconfirm.requestFocus();
            return;
        }
        if(! pswd.equals(confirm)) {
            rpswd.setError("Please recheck password");
            rpswd.requestFocus();
            return;
        }

        ((PrimaryTask)getActivity()).progressBar.startProgressBar();
        auth.createUserWithEmailAndPassword(email, pswd)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        ((PrimaryTask)getActivity()).progressBar.dismisprogressBar();
                        if(task.isSuccessful()) {
                            Toast.makeText(getContext(), "You have successfully registered.", Toast.LENGTH_LONG).show();

                            FirebaseUser curr_usr = auth.getCurrentUser();
                            User u = new User(curr_usr.getUid(), name, email);

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    db.collection("Users").document(curr_usr.getUid()).set(u)
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    db.collection("Users").document(curr_usr.getUid()).set(u);
                                                }
                                            });
                                }
                            }).start();

                            FragmentManager fragmentManager = getFragmentManager();
                            fragmentManager.beginTransaction().replace(R.id.group_container, new login()).commit();
                        }
                        else {
                            Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}