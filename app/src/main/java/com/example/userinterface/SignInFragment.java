// SignInFragment
package com.example.userinterface;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class SignInFragment extends Fragment {

    private EditText editTextUsernameOrEmail, editTextPassword;
    private Button btnSignIn;

    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_in, container, false);

        editTextUsernameOrEmail = view.findViewById(R.id.editTextUsernameOrEmail);
        editTextPassword = view.findViewById(R.id.editTextPassword);
        btnSignIn = view.findViewById(R.id.btnSignIn);

        mAuth = FirebaseAuth.getInstance();

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String usernameOrEmail = editTextUsernameOrEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                if (TextUtils.isEmpty(usernameOrEmail) || TextUtils.isEmpty(password)) {
                    Toast.makeText(getContext(), "Please enter username/email and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                signInWithEmailAndPassword(usernameOrEmail, password);
            }
        });

        return view;
    }

    private void signInWithEmailAndPassword(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("SignInFragment", "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            // Navigate to welcome fragment or desired screen
                            WelcomeFragment welcomeFragment = new WelcomeFragment();
                            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.frameLayoutContent, welcomeFragment);
                            transaction.addToBackStack(null);
                            transaction.commit();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("SignInFragment", "signInWithEmail:failure", task.getException());
                            Toast.makeText(getContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}