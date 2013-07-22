package com.google.drive.samples.crossclientoauth2;

import java.util.Arrays;
import java.util.List;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

public class MainActivity extends Activity {

  final private String CLIENT_ID = "498930908887.apps.googleusercontent.com";
  final private List<String> SCOPES = Arrays.asList(new String[]{
      "https://www.googleapis.com/auth/plus.login",
      "https://www.googleapis.com/auth/drive"
  });
  
  private GoogleAccountCredential mCredential;
  private TextView mResultTextView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    setContentView(R.layout.activity_main);
    mResultTextView = (TextView) findViewById(R.id.textViewResult);
    
    // initiate a credential object with drive and plus.login scopes
    // cross identity is only available for tokens retrieved with plus.login
    mCredential = GoogleAccountCredential.usingOAuth2(this, null);
    startActivityForResult(mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
  }

  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    switch (requestCode) {
    // user has  returned back from the account picker,
    // initiate the rest of the flow with the account he/she has chosen.
    case REQUEST_ACCOUNT_PICKER:
      String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
      if (accountName != null) {
        mCredential.setSelectedAccountName(accountName);
        new RetrieveExchangeCodeAsyncTask().execute();
      }
      break;
    // user has returned back from the permissions screen,
    // if he/she has given enough permissions, retry the the request.
    case REQUEST_AUTHORIZATION:
      if (resultCode == Activity.RESULT_OK) {
        new RetrieveExchangeCodeAsyncTask().execute();
      } else {
        String msg = getString(R.string.error_notauthorized);
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
      }
      break;
    }
  }

  /**
   * Retrieves the exchange code to be sent to the
   * server-side component of the app.
   */
  public class RetrieveExchangeCodeAsyncTask extends RetrieveTokenAsyncTask {

    @Override
    protected String getScope() {
      return String.format("oauth2:server:client_id:%s:api_scope:%s",
          CLIENT_ID, TextUtils.join(" ", SCOPES));
    }
  }

  /**
   * Retrieves the id_token to identify the user without the
   * regular client-side authorization flow. The jwt payload needs to be
   * sent to the server-side component.
   */
  public class RetrieveIdTokenAsyncTask extends RetrieveTokenAsyncTask {

    @Override
    protected String getScope() {
      return "audience:server:client_id:" + CLIENT_ID;
    }
  }
  
  abstract public class RetrieveTokenAsyncTask extends
      AsyncTask<Void, Boolean, String> {
    
    abstract protected String getScope();
    
    @Override
    protected String doInBackground(Void... params) {
      try {
        return GoogleAuthUtil.getToken(
            MainActivity.this, mCredential.getSelectedAccountName(), getScope());
      } catch (UserRecoverableAuthException e) {
        startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
      } catch (Exception e) {
        // TODO: handle the exception
        e.printStackTrace();
      }
      return null;
    }
    
    @Override
    protected void onPostExecute(String result) {
      super.onPostExecute(result);
      mResultTextView.setText(result);
    }
  }

  private static final int REQUEST_ACCOUNT_PICKER = 100;
  private static final int REQUEST_AUTHORIZATION = 200;
  

}
