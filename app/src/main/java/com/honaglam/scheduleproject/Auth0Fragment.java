package com.honaglam.scheduleproject;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.auth0.android.Auth0;
import com.auth0.android.authentication.AuthenticationAPIClient;
import com.auth0.android.authentication.AuthenticationException;
import com.auth0.android.callback.Callback;
import com.auth0.android.provider.WebAuthProvider;
import com.auth0.android.result.Credentials;
import com.auth0.android.result.UserProfile;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Auth0Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Auth0Fragment extends DialogFragment {

  public Auth0Fragment() {
  }


  public static Auth0Fragment newInstance() {
    Auth0Fragment fragment = new Auth0Fragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }


  @Nullable
  Context context;


  @NonNull
  @Override
  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
    Dialog dialog = super.onCreateDialog(savedInstanceState);
    dialog.setCancelable(false);
    return dialog;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return super.onCreateView(inflater, container, savedInstanceState);
    //return inflater.inflate(R.layout.fragment_autho, container, false);

  }



  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    this.context = context;
  }








}