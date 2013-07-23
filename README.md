# Cross-Client OAuth 2.0 for Android

It's very common to build software with multiple components: a mobile app, Web back-end and etc. You Drive app should be in the same category and requires you to manage user authorization for multiple entities of the sample app. Google's OAuth 2.0 implementation provides some extentions to address the following cases:

* Help server-side to retrieve an acess token for itself, when user authorizes the Android app.
* Help Android app to identify user if user is once authorized with the server-side.


**Warning**: These extentions are only available if authorization scopes contain `https://www.googleapis.com/auth/plus.login` to use Google+ sign-in.

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

## Retrieve exchange code for server-side

~~~java
String scope = String.format("oauth2:server:client_id:%s:api_scope:%s",
          CLIENT_ID, TextUtils.join(" ", SCOPES));
String exchangeCode = GoogleAuthUtil.getToken(context, accountName, scope);
~~~

## Retrieve a JWT to identify user on server-side

Retrieve an JSON Web Token (JWT) to identify user. You can exchange the JWT payload with your server-side to decrypt and identify user with his/her email address.

~~~java
String scope = "audience:server:client_id:" + CLIENT_ID;
String idToken = GoogleAuthUtil.getToken(context, accountName, scope);
~~~

More details are explained on [Google's OAuth 2.0 public docs](https://developers.google.com/accounts/docs/CrossClientAuth).