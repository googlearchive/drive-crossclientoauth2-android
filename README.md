# Cross-Client OAuth 2.0 for Android

It's very common to build software with multiple components: a mobile app, Web back-end and etc. You Drive app should be in the same category and requires you to manage user authorization for multiple entities of the sample app. Google's OAuth 2.0 implementation provides some extentions to address the following cases:

* Resolving user identity on Android apps without requiring the user to sign-in, if the user has already connected himself/herself on the Web application.
* Enabling an Android app to retrieve an exchange code for its server-side component.


**Warning**: These extentions are only available if authorization scopes contain `https://www.googleapis.com/auth/plus.login` for now.

## Configuration

Create or reuse a project on [API console](https://code.google.com/apis/console/). You need to create two client IDs; one for your Web app, the other for your Android app.

Configure the sample with your Web app `CLIENT_ID` and the scopes required.

~~~java
final private String CLIENT_ID = "abc123.apps.googleusercontent.com";
final private List<String> SCOPES = Arrays.asList(new String[]{
    "https://www.googleapis.com/auth/plus.login",
    "https://www.googleapis.com/auth/drive"
});
~~~~

## Resolve user identity with no sign-in

Retrieve an JSON Web Token (JWT) to identify user. You can exchange the JWT payload with your server-side to decrypt and identify user with his/her email address.

~~~java
String scope = "audience:server:client_id:" + CLIENT_ID;
String idToken = GoogleAuthUtil.getToken(context, accountName, scope);
~~~

## Retrieve access/refresh tokens for the server

~~~java
String scope = String.format("oauth2:server:client_id:%s:api_scope:%s", CLIENT_ID, TextUtils.join(" ", SCOPES));
String exchangeCode = GoogleAuthUtil.getToken(context, accountName, scope);
~~~

Send the code to the server and exchange your code with Google to retrieve an access and a refresh token for your server-side.

    POST https://accounts.google.com/o/oauth2/token
    Content-Type: application/x-www-form-urlencoded

    code=<exchangeCode>&
    client_id=<CLIENT_ID>&
    client_secret=<CLIENT_SECRET>&
    grant_type=authorization_code

More details are explained on [Google Drive's Cross-client Identity](https://developers.google.com/drive/auth/android#cross-client_identity) docs.