package com.honaglam.scheduleproject;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.auth0.android.Auth0;
import com.auth0.android.authentication.AuthenticationAPIClient;
import com.auth0.android.authentication.AuthenticationException;
import com.auth0.android.callback.Callback;
import com.auth0.android.provider.WebAuthProvider;
import com.auth0.android.result.Credentials;
import com.auth0.android.result.UserProfile;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Auth0Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Auth0Fragment extends Fragment {


  public Auth0Fragment() {
  }

  public static final String USER_ACCESS_TOKEN_ARG = "AUTH0_USER_ACCESS_TOKEN";

  public static Auth0Fragment newInstance() {
    Auth0Fragment fragment = new Auth0Fragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }


  @Nullable private String userAccessToken = null;
  @NonNull Auth0 auth0Account = new Auth0("6auXenOyXE3RcK51Syb5hoeu55DAbfDy","dev-j07rhfbc.us.auth0.com");
  @Nullable UserProfile userProfile;
  @Nullable Context context;
  //ReminderTaskFireBase fireBase;

  @Override public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    this.context = context;
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      userAccessToken = getArguments().getString(USER_ACCESS_TOKEN_ARG,null);
    }
    //fireBase = new ReminderTaskFireBase("lamhoangdien113@gmail,com");
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_autho, container, false);
  }

  @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    view.findViewById(R.id.btnLoginAuthFragment).setOnClickListener((clickedView) -> {
      auth0LoginWithBrowser();
    });
    view.findViewById(R.id.btnLogoutAuthFragment).setOnClickListener((clickedView) -> {
      logOutUserWithBrowser();
    });
    view.findViewById(R.id.btnSyncAuthFragment).setOnClickListener((clickedView) -> {
      syncData();
    });
  }

  public void syncData(){
    if (context == null){
      return;
    }
    if(userAccessToken == null || userAccessToken.isEmpty()){
      auth0LoginWithBrowser();
    }
    if(userProfile == null){
      getUserInformation();
    }
    String email = userProfile.getEmail();
    if(email == null){
      return;
    }

  }

  public void showAlertDialog(){
    if(context == null){
      return;
    }

    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
    builder.setMessage("HELLO");
    builder.setView(new ProgressBar(context));



  }



  private void auth0LoginWithBrowser(){
    if(context == null){
      return;
    }

    WebAuthProvider.login(auth0Account).withScheme("demo")
            .withScope("openid profile email")
            .start(context, new UserLogInCallback());
  }
  public void getUserInformation(){
    if(context == null || userAccessToken == null){
      return;
    }

    AuthenticationAPIClient client = new AuthenticationAPIClient(auth0Account);
    client.userInfo(userAccessToken).start(new UserInfoCallback());
  }
  public void logOutUserWithBrowser(){
    if(context == null){
      return;
    }
    WebAuthProvider.logout(auth0Account)
            .withScheme("demo")
            .start(context, new UserLogOutCallback());
  }



  class UserLogOutCallback implements Callback<Void, AuthenticationException>{
    @Override
    public void onSuccess(Void unused) {
      Log.i("AUTH","User logged out successfully");
      Auth0Fragment.this.userProfile = null;
      Auth0Fragment.this.userAccessToken = null;
    }

    @Override
    public void onFailure(@NonNull AuthenticationException e) {
      Log.i("AUTH","User logged out failed");
    }
  }
  class UserLogInCallback implements Callback<Credentials, AuthenticationException>{
    @Override
    public void onSuccess(Credentials credentials) {
      userAccessToken = credentials.getAccessToken();
      Log.i("AUTH","Log in credential: " + userAccessToken );
      getUserInformation();
    }
    @Override
    public void onFailure(@NonNull AuthenticationException e) {
      Log.e("AUTH",e.getDescription());
    }
  }
  class UserInfoCallback implements Callback<UserProfile, AuthenticationException>{
    @Override
    public void onFailure(@NonNull AuthenticationException e) {
      Auth0Fragment.this.userProfile = null;
      Log.e("AUTH",e.getDescription());
    }

    @Override
    public void onSuccess(UserProfile userProfile) {
      Auth0Fragment.this.userProfile = userProfile;

      Log.i("AUTH","User email: " + userProfile.getEmail());
      Log.i("AUTH","User id " + userProfile.getId());
      Log.i("AUTH","Picture " + userProfile.getPictureURL());
      Log.i("AUTH","Name " + userProfile.getName() + "|" + userProfile.getFamilyName() + "|" + userProfile.getNickname());
    }
  }
}