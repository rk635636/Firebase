package com.example.rushikesh.ria;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiActivity;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {

    private SignInButton signInButton;
    private static final int RC_SIGN_IN=1;
    private static final String TAG="LogIn Activity";
    private GoogleApiClient googleApiClient;
    private ProgressDialog progressDialog;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseAuth mauth;
    private CallbackManager mcallbackManager;
    private LoginButton loginButton;
    private int flag=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);



       // AppEventsLogger.activateApp(this);

        //===============================================Facebook Login=============================================



        if(getIntent().hasExtra("logout"))
        {
            LoginManager.getInstance().logOut();
        }
        mcallbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton)findViewById(R.id.login_button);
        loginButton.setReadPermissions("email","public_profile");
        loginButton.registerCallback(mcallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAcessToken(loginResult.getAccessToken());
                Log.d("","Facebook: onSuccess "+loginResult);
               // Toast.makeText(MainActivity.this,loginResult.toString(),Toast.LENGTH_LONG).show();
               // startActivity(new Intent(MainActivity.this,Main2Activity.class));
            }

            @Override
            public void onCancel() {
                Log.d("TAG","Facebook: OnCancel ");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("TAG","Facebook: OnError ");
            }
        });


        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();

                if(user!=null)
                {
                    Log.d("","Signed In"+user.getUid());

                    Intent intent = new Intent(MainActivity.this,Main2Activity.class);
                    String name = user.getDisplayName();
                    startActivity(intent);
                    finish();
                }
                else
                {
                    Log.d("TAG","SIGNED OUT");
                }
            }
        };
        //=========================================================================================================

        progressDialog = new ProgressDialog(this);
        mauth = FirebaseAuth.getInstance();
        signInButton = (SignInButton)findViewById(R.id.google);


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();


        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SignIn();
            }
        });

    }

    private void handleFacebookAcessToken(AccessToken accessToken) {

        Log.d("","Access Token FACEBOOK "+accessToken);
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        mauth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                    Log.d("","SIGNED IN With CREDENTIAL "+task.isSuccessful());
                        if(!task.isSuccessful())
                        {
                            Log.v("","Sign with Credential "+task.getException());
                            Toast.makeText(MainActivity.this,"FAILED",Toast.LENGTH_LONG).show();
                        }
                        else {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mauth.getCurrentUser();
                            startActivity(new Intent(MainActivity.this,Main2Activity.class));

                        }
                    }
                });
    }



    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mauth.getCurrentUser();
        if(currentUser!=null)
        {
            startActivity(new Intent(MainActivity.this,Main2Activity.class));
        }

        mauth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(authStateListener!=null)
        {
            mauth.removeAuthStateListener(authStateListener);
        }
    }

    private void SignIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Toast.makeText(LoginActivity.this,"Login",Toast.LENGTH_LONG).show();
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);

        //=====================================================================================FACEBOOK===============================================================


        //============================================================================================================================================================
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);


            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                progressDialog.setMessage("Signing In...");
                progressDialog.show();
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...

                progressDialog.dismiss();
            }
        }
        else {
            mcallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mauth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            progressDialog.dismiss();
                            FirebaseUser user = mauth.getCurrentUser();
                            String uri = user.getPhotoUrl().toString();
                          //  Toast.makeText(MainActivity.this,uri,Toast.LENGTH_LONG).show();
                            Log.e("U",uri);
                            Intent i = new Intent(MainActivity.this,Main2Activity.class);
                            i.putExtra("Url",uri);
                            i.putExtra("name",user.getDisplayName());
                            startActivity(new Intent(i));

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }


                    }


                });
    }

//    private void checkUserExists() {
//
//        if(firebaseAuth.getCurrentUser()!=null)
//        {
//            final String UID = firebaseAuth.getCurrentUser().getUid();
//
//            databaseReference.addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//
//                    if (dataSnapshot.hasChild(UID)) {
//                        Intent main = new Intent(LoginActivity.this, MainTabbedActivity.class);
//                        main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        startActivity(main);
//                    } else {
//                        Intent setup = new Intent(LoginActivity.this, SetupActivity.class);
//                        setup.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        startActivity(setup);
//                    }
//
//                }
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });
//        }
//    }

}
