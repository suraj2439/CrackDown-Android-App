package com.suraj.cpy;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link login#newInstance} factory method to
 * create an instance of this fragment.
 */
public class login extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FirebaseAuth auth;
    EditText email, pswd;

    public login() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment login.
     */
    // TODO: Rename and change types and number of parameters
    public static login newInstance(String param1, String param2) {
        login fragment = new login();
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

        View view = inflater.inflate(R.layout.fragment_login, container, false);

        EditText saved_email = (EditText) view.findViewById(R.id.email);
        EditText saved_pswd = (EditText) view.findViewById(R.id.pswd);

        SharedPreferences sp = ((PrimaryTask)getActivity()).login_details;
        String userEmail = sp.getString("UserName", null);
        String password = sp.getString("Pswd", null);

        if(userEmail != null && password!= null) {
            saved_email.setText(userEmail);
            saved_pswd.setText(password);
        }

        auth = FirebaseAuth.getInstance();
        Button login_btn = view.findViewById(R.id.login_btn);
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(! ((PrimaryTask)getActivity()).is_right_version) {
                    Toast.makeText(getContext(), "You are not allowed to use this version of app, please update your app",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                email  = view.findViewById(R.id.email);
                pswd = view.findViewById(R.id.pswd);
                signin_user();
            }
        });

        Button signup_btn = view.findViewById(R.id.signup);
        signup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(! ((PrimaryTask)getActivity()).is_right_version) {
                    Toast.makeText(getContext(), "You are not allowed to use this version of app, please update your app",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.group_container, new signup()).addToBackStack(null).commit();
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    private void signin_user() {
        String userEmail = email.getText().toString();
        String userPswd = pswd.getText().toString();

        if(userEmail.isEmpty()) {
            email.setError("Required");
            email.requestFocus();
            return;
        }
        if(userPswd.isEmpty()) {
            pswd.setError("Required");
            pswd.requestFocus();
            return;
        }

        ((PrimaryTask)getActivity()).progressBar.startProgressBar();
        Toast.makeText(getContext(), "Please wait...", Toast.LENGTH_LONG).show();
        auth.signInWithEmailAndPassword(userEmail, userPswd)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        ((PrimaryTask)getActivity()).progressBar.dismisprogressBar();
                        if(task.isSuccessful()) {
                            ((PrimaryTask)getActivity()).editor.putString("UserName", userEmail);
                            ((PrimaryTask)getActivity()).editor.putString("Pswd", userPswd);
                            ((PrimaryTask)getActivity()).editor.commit();

                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            FirebaseUser curr_user = auth.getCurrentUser();
                            ((PrimaryTask)getActivity()).fuser = curr_user;
                            Toast.makeText(getContext(), "You have successfully logged in", Toast.LENGTH_LONG).show();

                            FragmentManager fragmentManager = getFragmentManager();
                            fragmentManager.beginTransaction().replace(R.id.group_container, new groups()).addToBackStack(null).commit();
                        }
                        else {
                            Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            email.setError("Invalid login id or password");
                            email.requestFocus();
                            return;
                        }
                    }
                });
    }
}