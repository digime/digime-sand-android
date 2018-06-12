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
import android.widget.Button;

import me.digi.sand.app1.R;

public class InstallDigimeFragment extends Fragment {
    private InstallActionListener listener;

    public static InstallDigimeFragment newInstance() {
        return new InstallDigimeFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof InstallActionListener) {
            listener = (InstallActionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement HomeActionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_install_layout, container, false);
        Button contBut = (Button) view.findViewById(R.id.continueButton);
        contBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onInstallClicked();
                }
            }
        });
        return view;
    }

    interface InstallActionListener {
        void onInstallClicked();
    }
}
