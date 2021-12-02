package com.suraj.cpy;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Feedback#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Feedback extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Feedback() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Feedback.
     */
    // TODO: Rename and change types and number of parameters
    public static Feedback newInstance(String param1, String param2) {
        Feedback fragment = new Feedback();
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

        View view = inflater.inflate(R.layout.fragment_feedback, container, false);

        Button feedbackSubmit = view.findViewById(R.id.feedbackSubmit);
        feedbackSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FeedbackObj feedback = new FeedbackObj();

                EditText feedbackrating = view.findViewById(R.id.feedbackRating);
                int rating = Integer.valueOf(feedbackrating.getText().toString());

                if(0 <= rating && rating <= 5) {
                    feedback.setRating(rating);

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                    feedback.setDateTime(sdf.format(new Date()));

                    EditText feedbackMostLiked = view.findViewById(R.id.feedbackMostLiked);
                    String mostLiked = feedbackMostLiked.getText().toString();
                    feedback.setMosteLiked(mostLiked);

                    EditText feedbackLeastLiked = view.findViewById(R.id.feedbackLeastLiked);
                    String leastLiked = feedbackLeastLiked.getText().toString();
                    feedback.setLeastLiked(leastLiked);

                    EditText feedbackcomment = view.findViewById(R.id.feedbackComment);
                    String comment = feedbackcomment.getText().toString();
                    feedback.setLeastLiked(comment);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("Feedback").document(Long.toString(System.currentTimeMillis())).set(feedback);
                        }
                    }).start();

                    Toast.makeText(getContext(), "Thank You for submitting feedback !", Toast.LENGTH_LONG).show();

                    ((ActivityMenu)getActivity()).finish();
                }
                else {
                    feedbackrating.setError("Rating must be between 0-5.");
                    feedbackrating.requestFocus();
                }
            }
        });

        return view;
    }
}