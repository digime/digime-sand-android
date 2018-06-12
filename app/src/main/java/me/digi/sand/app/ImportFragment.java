/*
 * Copyright (c) 2009-2018 digi.me Limited. All rights reserved.
 */

package me.digi.sand.app;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.TextView;

import me.digi.sand.app1.R;

public class ImportFragment extends Fragment {
    private static final int LABEL_ID_BASE = 10000;
    private static final int LABEL_DISPLAY_LENGTH = 1500;
    private static final int LABEL_ANIMATION_LENGTH = 400;
    private final String[] labels = {
            "Monday Moaner",
            "Tuesday Trumpeteer",
            "Wednesday Wonderer",
            "Thursday Thinker",
            "Friday Fighter",
            "Saturday Surveyer",
            "Sunday Slumberer"
    };

    private int currentVisibleLabel = LABEL_ID_BASE;
    private boolean animating = false;

    public ImportFragment() {
    }

    @SuppressWarnings("WeakerAccess")
    public void setAnimating(boolean animating) {
        this.animating = animating;
        currentVisibleLabel = LABEL_ID_BASE;
    }

    @SuppressWarnings("unused")
    public boolean getAnimating() {
        return this.animating;
    }

    public static ImportFragment newInstance() {
        return new ImportFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_import, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        for (int i = 0; i < labels.length; i++) {
            String label = labels[i];
            TextView textLabel = new TextView(getActivity());
            textLabel.setId(LABEL_ID_BASE + i + 1);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(35, 10, 35, 10);
            textLabel.setLayoutParams(params);
            Typeface latoFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Lato-Light.ttf");
            textLabel.setTypeface(latoFont);
            textLabel.setTextSize(20);
            textLabel.setGravity(Gravity.CENTER);
            textLabel.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            textLabel.setText(label);
            textLabel.setTextColor(Color.WHITE);
            if (i != 0) textLabel.setAlpha(0.0f);
            ((LinearLayout) view).addView(textLabel);
        }
        scheduleAnimation();
    }

    public void startAnimating() {
        setAnimating(true);
        scheduleAnimation();
    }

    public void stopAnimating() {
        setAnimating(false);
        scheduleAnimation();
    }

    private void scheduleAnimation() {
        if (!animating) return;
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                animateSeque();
                scheduleAnimation();
            }
        }, LABEL_DISPLAY_LENGTH);
    }

    private void animateSeque() {
        if (getView() == null) return;
        final Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(LABEL_ANIMATION_LENGTH);
        fadeIn.setRepeatCount(0);
        final Animation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(LABEL_ANIMATION_LENGTH);
        fadeOut.setRepeatCount(0);

        int current = currentVisibleLabel == LABEL_ID_BASE ? LABEL_ID_BASE + 1 : currentVisibleLabel;
        int next = currentVisibleLabel == LABEL_ID_BASE ? LABEL_ID_BASE + 2 : (currentVisibleLabel - LABEL_ID_BASE + 1 <= labels.length ? currentVisibleLabel + 1 : LABEL_ID_BASE + 1);
        currentVisibleLabel = next;

        @SuppressWarnings("ConstantConditions") final TextView label = (TextView) getView().findViewById(current);
        final TextView nextLabel = (TextView) getView().findViewById(next);
        if (label != null) {
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    label.setAlpha(0.0f);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            label.startAnimation(fadeOut);
        }
        if (nextLabel != null) {
            fadeIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    nextLabel.setAlpha(1.0f);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            nextLabel.startAnimation(fadeIn);
        }
    }
}
