package com.thecoffeecoders.chatex.backupAndStuff;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thecoffeecoders.chatex.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragmentBackup extends Fragment {


    public RequestsFragmentBackup() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_requests, container, false);
    }

}
