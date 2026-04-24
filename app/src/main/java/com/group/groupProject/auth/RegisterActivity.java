package com.group.groupProject.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.group.groupProject.core.MainActivity;
import com.group.groupProject.R;

public class RegisterActivity extends AppCompatActivity {
    EditText editTextEmail, editTextPassword, editTextName, editTextConfirmPassword;
    ImageButton buttonReg;
    TextView tv_go_login;
    FirebaseAuth mAuth;
    String TAG;
    ProgressBar progressBar;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        TAG = "Check log";
        mAuth = FirebaseAuth.getInstance();
        editTextName = findViewById(R.id.et_name);
        editTextEmail = findViewById(R.id.et_email);
        editTextPassword = findViewById(R.id.et_password);
        editTextConfirmPassword = findViewById(R.id.et_confirm_password);
        buttonReg = findViewById(R.id.img_Reg);
        tv_go_login = findViewById(R.id.tv_go_login);
        progressBar = findViewById(R.id.progressBar);

        buttonReg.setOnClickListener(view->{
            buttonReg.setEnabled(false);
            buttonReg.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

            String name, email, password, confirmPassword;

            name = String.valueOf(editTextName.getText()).trim();
            email = String.valueOf(editTextEmail.getText());
            password = String.valueOf(editTextPassword.getText());
            confirmPassword = String.valueOf(editTextConfirmPassword.getText());

            if (TextUtils.isEmpty(name)) {
                showError("Please enter your name");
                return;
            }

            if (TextUtils.isEmpty(email)) {
                showError("Please enter your email");
                return;
            }

            if (TextUtils.isEmpty(password)) {
                showError("Please enter the password");
                return;
            }

            if (TextUtils.isEmpty(confirmPassword)) {
                showError("Please confirm the password");
                return;
            }

            if (!password.equals(confirmPassword)) {
                showError("Password and confirm password are not the same");
                return;
            }

            if(password.length()<6){
                showError("Password should be at least 6 characters");
            }

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success");
                    FirebaseUser user = mAuth.getCurrentUser();

                    if (user != null) {
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(name).build();
                        user.updateProfile(profileUpdates).addOnCompleteListener(task2 -> {
                            if (task2.isSuccessful()) {
                                Log.d(TAG, "User Name updated.");
                                Toast.makeText(RegisterActivity.this, "Register success", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                    showError("Authentication failed");
                }
            });
        });

        tv_go_login.setOnClickListener(view -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

    }
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        buttonReg.setEnabled(true);
        buttonReg.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }
}