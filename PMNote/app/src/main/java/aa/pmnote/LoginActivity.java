package aa.pmnote;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private EditText mEmailField;
    private EditText mPasswordField;
    private Button mSigninButton;
    private SignInButton mGoogleSigninButton;
    private LinearLayout mSigninProgressBar;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private GoogleApiClient mGoogleApiClient;

    private static final int RC_SIGN_IN = 1;
    private static final String TAG = "LOGIN_ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmailField = (EditText) findViewById(R.id.emailTextField);
        mPasswordField = (EditText) findViewById(R.id.passwordField);
        mSigninButton = (Button) findViewById(R.id.signinButton);
        mGoogleSigninButton = (SignInButton) findViewById(R.id.googleSigninButton);
        mSigninProgressBar = (LinearLayout) findViewById(R.id.signingProgressBar);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //if client logged in
                if(firebaseAuth.getCurrentUser() != null)
                {
                    //clear password
                    mPasswordField.setText("");
                    //go to the project activity
                    startActivity(new Intent(LoginActivity.this, ProjectsActivity.class));
                }
            }
        };

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(LoginActivity.this, "Error: Connection failed", Toast.LENGTH_LONG).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mGoogleSigninButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSigninProgressBar.setVisibility(View.VISIBLE);
                signInWithGoogle();
            }
        });

        mSigninButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSigninProgressBar.setVisibility(View.VISIBLE);
                signInWithEmailPassword();
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mSigninProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void signInWithGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
        ((TextView)mSigninProgressBar.getChildAt(1)).setText("Signing in Google...");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                Toast.makeText(LoginActivity.this, "Failed to sign in with Google.", Toast.LENGTH_LONG).show();
                mSigninProgressBar.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        ((TextView)mSigninProgressBar.getChildAt(1)).setText("Getting credentials...");

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Failed to sign in with Google.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signInWithEmailPassword()
    {
        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();
        ((TextView)mSigninProgressBar.getChildAt(1)).setText("Signing in...");

        if(email.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Email is empty", Toast.LENGTH_LONG).show();
            mSigninProgressBar.setVisibility(View.INVISIBLE);
        }
        else if(password.isEmpty()){
            Toast.makeText(LoginActivity.this, "Password is empty", Toast.LENGTH_LONG).show();
            mSigninProgressBar.setVisibility(View.INVISIBLE);
        }
        else
        {
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(!task.isSuccessful())
                    {
                        registerWithEmailPassword();
                    }
                }
            });
        }
    }

    private void registerWithEmailPassword()
    {
        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();
        ((TextView)mSigninProgressBar.getChildAt(1)).setText("Registering...");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Register failed.",
                                    Toast.LENGTH_SHORT).show();
                            mSigninProgressBar.setVisibility(View.INVISIBLE);
                        }
                        else
                        {
                            signInWithEmailPassword();
                        }
                    }
                });
    }
}
