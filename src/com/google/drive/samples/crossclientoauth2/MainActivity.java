/**
 * Copyright 2013 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.drive.samples.crossclientoauth2;

import java.util.Arrays;
import java.util.List;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

public class MainActivity extends Activity {

  final private String CLIENT_ID = "abc123.apps.googleusercontent.com";
  final private List<String> SCOPES = Arrays.asList(new String[]{
      "https://www.googleapis.com/auth/plus.login",
      "https://www.googleapis.com/auth/drive"
  });
  
  private GoogleAccountCredential mCredential;
  
  private EditText mExchangeCodeEditText;
  private EditText mIdTokenEditText;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    setContentView(R.layout.activity_main);
    mExchangeCodeEditText = (EditText) findViewById(R.id.editTextExchangeCode);
    mIdTokenEditText = (EditText) findViewById(R.id.editTextIdToken);
    
    // initiate a credential object with drive and plus.login scopes
    // cross identity is only available for tokens retrieved with plus.login
    mCredential = GoogleAccountCredential.usingOAuth2(this, null);
    
    // user needs to select an account, start account picker
    startActivityForResult(
        mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
  }

  /**
   * Handles the callbacks from result returning
   * account picker and permission requester activities.
   */
  @Override
  protected void onActivityResult(
      final int requestCode, final int resultCode, final Intent data) {
    switch (requestCode) {
    // user has  returned back from the account picker,
    // initiate the rest of the flow with the account he/she has chosen.
    case REQUEST_ACCOUNT_PICKER:
      String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
      if (accountName != null) {
        mCredential.setSelectedAccountName(accountName);
        new RetrieveExchangeCodeAsyncTask().execute();
        new RetrieveJwtAsyncTask().execute();
      }
      break;
    // user has returned back from the permissions screen,
    // if he/she has given enough permissions, retry the the request.
    case REQUEST_AUTHORIZATION:
      if (resultCode == Activity.RESULT_OK) {
        // replay the same operations
        new RetrieveExchangeCodeAsyncTask().execute();
        new RetrieveJwtAsyncTask().execute();
      }
      break;
    }
  }

  /**
   * Retrieves the exchange code to be sent to the
   * server-side component of the app.
   */
  public class RetrieveExchangeCodeAsyncTask
      extends AsyncTask<Void, Boolean, String> {
    
    @Override
    protected String doInBackground(Void... params) {
      String scope = String.format("oauth2:server:client_id:%s:api_scope:%s",
          CLIENT_ID, TextUtils.join(" ", SCOPES));
      try {
        return GoogleAuthUtil.getToken(
            MainActivity.this, mCredential.getSelectedAccountName(), scope);
      } catch (UserRecoverableAuthException e) {
        startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
      } catch (Exception e) {
        e.printStackTrace(); // TODO: handle the exception
      }
      return null;
    }
    
    @Override
    protected void onPostExecute(String code) {
      // exchange code with server-side to retrieve an additional
      // access token on the server-side.
      mExchangeCodeEditText.setText(code);
    }
  }

  /**
   * Retrieves a JWT to identify the user without the
   * regular client-side authorization flow. The jwt payload needs to be
   * sent to the server-side component.
   */
  public class RetrieveJwtAsyncTask
      extends AsyncTask<Void, Boolean, String> {

    @Override
    protected String doInBackground(Void... params) {
      String scope = "audience:server:client_id:" + CLIENT_ID;
      try {
        return GoogleAuthUtil.getToken(
            MainActivity.this, mCredential.getSelectedAccountName(), scope);
      } catch(UserRecoverableAuthIOException e) {
        startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
      } catch (Exception e) {
        e.printStackTrace(); // TODO: handle the exception
      }
      return null;
    }

    @Override
    protected void onPostExecute(String idToken) {
      // exchange encrypted idToken with server-side to identify the user
      mIdTokenEditText.setText(idToken);
    }
  }
  
  private static final int REQUEST_ACCOUNT_PICKER = 100;
  private static final int REQUEST_AUTHORIZATION = 200;

}
