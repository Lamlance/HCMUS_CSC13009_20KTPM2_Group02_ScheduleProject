package com.honaglam.scheduleproject.Authorizer;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.auth0.android.Auth0;
import com.auth0.android.authentication.AuthenticationAPIClient;
import com.auth0.android.authentication.AuthenticationException;
import com.auth0.android.callback.Callback;
import com.auth0.android.provider.WebAuthProvider;
import com.auth0.android.result.Credentials;
import com.auth0.android.result.UserProfile;
import com.honaglam.scheduleproject.Auth0Fragment;

import java.util.HashMap;
import java.util.Map;

public class MyAuthorizer {
  static Auth0 auth0Account = new Auth0("6auXenOyXE3RcK51Syb5hoeu55DAbfDy", "dev-j07rhfbc.us.auth0.com");

  public interface OnLoginSuccess {
    void onLogin(@Nullable UserProfile userProfile);
  }

  public interface OnLogout{
    void onLogout(boolean isSuccess);
  }

  public interface CredentialCallBack {
    void onCredential(Credentials credentials);
  }

  private static void Login(@NonNull Context context, OnLoginSuccess loginCallBack) {
    new Thread(new AuthThread(context, new UserInfoCallback(loginCallBack))).start();
  }

  private static class AuthThread implements Runnable {
    Context context;
    @NonNull
    UserInfoCallback userInfoCallback;

    AuthThread(Context context, @NonNull UserInfoCallback infoCallback) {
      this.context = context;
      userInfoCallback = infoCallback;
    }

    @Override
    public void run() {
      Map<String,Object> params = new HashMap<>();
      params.put("prompt","select_account");

      WebAuthProvider.login(auth0Account)
              .withParameters(params)
              .withScheme("demo")
              .withScope("openid profile email")
              .start(context, new UserLogInCallback(context, credentials -> {
                Log.i("AUTH", "Log in credential: " + credentials);
                new AuthenticationAPIClient(auth0Account)
                        .userInfo(credentials.getAccessToken())
                        .start(userInfoCallback);
              }));
    }
  }

  static class UserInfoCallback implements Callback<UserProfile, AuthenticationException> {
    OnLoginSuccess loginCallBack;

    UserInfoCallback(@NonNull OnLoginSuccess callback) {
      loginCallBack = callback;
    }

    @Override
    public void onFailure(@NonNull AuthenticationException e) {
      Log.e("AUTH", e.getDescription());
      loginCallBack.onLogin(null);
    }

    @Override
    public void onSuccess(UserProfile userProfile) {
      loginCallBack.onLogin(userProfile);
    }
  }

  static class UserLogInCallback implements Callback<Credentials, AuthenticationException> {
    CredentialCallBack callBack;
    Context context;

    UserLogInCallback(@NonNull Context ctx, @NonNull CredentialCallBack credentialCallBack) {
      callBack = credentialCallBack;
      context = ctx;
    }

    @Override
    public void onSuccess(Credentials credentials) {
      //userAccessToken = credentials.getAccessToken();
      //Log.i("AUTH", "Log in credential: " + userAccessToken);
      Handler mainHandler = new Handler(context.getMainLooper());
      mainHandler.post(new Runnable() {
        @Override
        public void run() {
          callBack.onCredential(credentials);
        }
      });
      //getUserInformation();
    }

    @Override
    public void onFailure(@NonNull AuthenticationException e) {
      Log.e("AUTH", e.getDescription());
    }
  }

  static class UserLogOutCallback implements Callback<Void, AuthenticationException> {
    @Override
    public void onSuccess(Void unused) {
      Log.i("AUTH", "User logged out successfully");
    }

    @Override
    public void onFailure(@NonNull AuthenticationException e) {
      Log.i("AUTH", "User logged out failed");
    }
  }

  @NonNull
  Context context;

  public MyAuthorizer(@NonNull Context ctx) {
    context = ctx;
  }

  public void login(OnLoginSuccess loginCallBack) {
    MyAuthorizer.Login(context, loginCallBack);
  }

  public void logout(@NonNull OnLogout onLogout) {
    WebAuthProvider.logout(auth0Account).withScheme("demo").start(context, new Callback<Void, AuthenticationException>() {
      @Override
      public void onSuccess(Void unused) {
        onLogout.onLogout(true);
      }

      @Override
      public void onFailure(@NonNull AuthenticationException e) {
        e.printStackTrace();
        onLogout.onLogout(false);
      }
    });
  }
}
