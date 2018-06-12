/*
 * Copyright (c) 2009-2018 digi.me Limited. All rights reserved.
 */

package me.digi.sand.app;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import me.digi.sand.app1.R;

public class SurveyFragment extends Fragment {
    private RatingBar ratingBar;
    private EditText surveyAnswer;

    private SurveyInteractionListener mListener;

    public SurveyFragment() { }

    public static SurveyFragment newInstance(@SuppressWarnings("SameParameterValue") int page) {
        SurveyFragment fragment = new SurveyFragment();
        Bundle args = new Bundle();
        args.putInt("pageNum", page);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //noinspection UnusedAssignment
            int page = getArguments().getInt("pageNum");
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser) {
            if (surveyAnswer != null) {
                surveyAnswer.setText("");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_survey, container, false);
        Button submit = (Button) view.findViewById(R.id.submitButton);
        ratingBar = (RatingBar) view.findViewById(R.id.ratingBar);

        surveyAnswer = (EditText) view.findViewById(R.id.surveyText);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (surveyAnswer.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(), "You must provide a reason ", Toast.LENGTH_SHORT).show();
                    return;
                }
                InputMethodManager inputManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(surveyAnswer.getApplicationWindowToken(), 0);

                if (mListener != null) {
                    String cleanString = surveyAnswer.getText().toString().replaceAll("\r", " ").replaceAll("\n", " ");
                    mListener.onSurveySubmitSelected(ratingBar.getRating(), cleanString);
                }
                surveyAnswer.setText("");
            }
        });

        TextView skipLabel = (TextView) view.findViewById(R.id.skipLabel);
        skipLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onMainMenuClicked();
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SurveyInteractionListener) {
            mListener = (SurveyInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement SurveyInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        surveyAnswer.setText("");
        mListener = null;
    }

    @SuppressWarnings("unused")
    public void clear()
    {
        if (surveyAnswer != null) {
            surveyAnswer.setText("");
        }
    }

    interface SurveyInteractionListener {

        void onSurveySubmitSelected(float rating, String reason);
        void onMainMenuClicked();
    }
}
