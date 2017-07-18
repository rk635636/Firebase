package com.example.rushikesh.ria;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.concurrent.TimeUnit;

public class Main2Activity extends AppCompatActivity {


    private FirebaseAuth.AuthStateListener authStateListener;
    private int flag;
    private FirebaseAuth.AuthStateListener authStateListener1;
    private TextView name;
    private FirebaseAuth firebaseAuth;
    private String code;
    String text=null;
    String photo=null;
    boolean mVerificationInProgress = false;
    String mVerificationId;
    ImageView imageView;
    PhoneAuthProvider.ForceResendingToken mResendToken;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

   // private LoginButton logout;

    EditText phone,ver_code;
    private Button send,ver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);


        String url = getIntent().getStringExtra("Url");
        final String name_user = getIntent().getStringExtra("name");
        final String url_pic = url;

         imageView = (ImageView)findViewById(R.id.prof_img);
        phone = (EditText)findViewById(R.id.edit);
        name = (TextView)findViewById(R.id.name);
        ver_code = (EditText)findViewById(R.id.code);

       // logout = (LoginButton)findViewById(R.id.logout);


        firebaseAuth = FirebaseAuth.getInstance();
        send = (Button)findViewById(R.id.send);
        ver = (Button)findViewById(R.id.verify);



        //==========================================================================================================================================
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                Toast.makeText(Main2Activity.this,"verification done",Toast.LENGTH_LONG).show();
                flag=1;
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(Main2Activity.this,"verification fail",Toast.LENGTH_LONG).show();
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // [START_EXCLUDE]
                    Toast.makeText(Main2Activity.this,"invalid mob no",Toast.LENGTH_LONG).show();
                    // [END_EXCLUDE]
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // [START_EXCLUDE]
                    Toast.makeText(Main2Activity.this,"quota over" ,Toast.LENGTH_LONG).show();
                    // [END_EXCLUDE]
                }
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                //Log.d(TAG, "onCodeSent:" + verificationId);
                Toast.makeText(Main2Activity.this,"Verification code sent to mobile",Toast.LENGTH_LONG).show();
                // Save verification ID and resending token so we can use them later

                mVerificationId = verificationId;
                mResendToken = token;
                phone.setVisibility(View.INVISIBLE);
                send.setVisibility(View.INVISIBLE);
                ver.setVisibility(View.VISIBLE);
                ver_code.setVisibility(View.VISIBLE);

            }
        };



        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        "+91"+phone.getText().toString(),        // Phone number to verify
                        60,                 // Timeout duration
                        TimeUnit.SECONDS,   // Unit of timeout
                        Main2Activity.this,               // Activity (for callback binding)
                        mCallbacks);        // OnVerificationStateChangedCallbacks

            }
        });

        ver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, ver_code.getText().toString());
                signInWithPhoneAuthCredential(credential);


            }
        });

        Picasso.with(getApplicationContext()).load(url).into(imageView);


        authStateListener1 = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user !=null)
                {
                    for(UserInfo userInfo : user.getProviderData())
                    {
                        Log.d("TAG",userInfo.getProviderId());
                    }

                  if(flag==0)
                  {
                      name.setText("Welcome "+user.getDisplayName());
                      Picasso.with(Main2Activity.this).load(user.getPhotoUrl()).into(imageView);
                  }
                }
                else
                {
                    Intent intent = new Intent(Main2Activity.this,MainActivity.class);
                    intent.putExtra("logout",true);
                    startActivity(intent);
                    finish();
                }
            }
        };
//        authStateListener = new FirebaseAuth.AuthStateListener() {
//            @Override
//            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
//
//                FirebaseUser user = firebaseAuth.getCurrentUser();
//                if(user !=null)
//                {
//                    Toast.makeText(Main2Activity.this,"Logged In",Toast.LENGTH_LONG).show();
//
//                }
//            }
//        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "signInWithCredential:success");
                            Toast.makeText(Main2Activity.this,"Verification done",Toast.LENGTH_LONG).show();
                            FirebaseUser user = task.getResult().getUser();
//                            name.setText("Welcome "+user.getDisplayName());
//                           // photo = user.getPhotoUrl().toString();
//                            //text = user.getDisplayName();
//                            Picasso.with(Main2Activity.this).load(user.getPhotoUrl()).into(imageView);
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            //Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                Toast.makeText(Main2Activity.this,"Verification failed code invalid",Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //firebaseAuth.addAuthStateListener(authStateListener);
        flag=0;
        firebaseAuth.addAuthStateListener(authStateListener1);
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
      if (item.getItemId()==R.id.logout)
        {
            FirebaseAuth.getInstance().signOut();
        }
        return super.onOptionsItemSelected(item);
    }

}
