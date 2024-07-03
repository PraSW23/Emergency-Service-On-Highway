//registration fragment

package com.example.userinterface;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.recaptcha.Recaptcha;
import com.google.android.recaptcha.RecaptchaTasksClient;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

public class RegistrationFragment extends Fragment {

    private EditText editTextName, editTextEmail, editTextMobile, editTextPassword, editTextConfirmPassword, editTextOTP;
    private Button btnRegister, btnSignIn, btnSendEmailVerification, btnSendMobileVerification, btnVerifyOTP;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    //private boolean isEmailVerified = false;
    //private boolean isMobileVerified = false;
    public RegistrationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registration, container, false);

        editTextName = view.findViewById(R.id.editTextName);
        editTextEmail = view.findViewById(R.id.editTextEmail);
        editTextMobile = view.findViewById(R.id.editTextMobile);
        editTextPassword = view.findViewById(R.id.editTextPassword);
        editTextConfirmPassword = view.findViewById(R.id.editTextConfirmPassword);
        btnRegister = view.findViewById(R.id.btnRegister);
        btnSignIn = view.findViewById(R.id.btnSignIn);
        btnSendEmailVerification = view.findViewById(R.id.btnSendEmailVerification);
        btnSendMobileVerification = view.findViewById(R.id.btnSendOTP);
        editTextOTP = view.findViewById(R.id.editTextOTP);
        btnVerifyOTP = view.findViewById(R.id.btnVerifyOTP);



        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnRegister.setEnabled(false);
                registerUser();
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnSignIn.setEnabled(false);
                loadSignInFragment();
            }
        });

        btnSendEmailVerification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEmailVerification();
            }
        });

        btnSendMobileVerification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnSendMobileVerification.setEnabled(false);
                sendMobileVerification();
            }
        });

        btnVerifyOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String otp = editTextOTP.getText().toString();
                if (!otp.isEmpty()) {
                    btnVerifyOTP.setEnabled(false);
                    verifyPhoneNumberWithCode(mVerificationId, otp);
                } else {
                    Toast.makeText(getContext(), "Please enter OTP", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    private void initializeRecaptchaClient() {
        Recaptcha
                .getTasksClient(requireActivity().getApplication(), "6LcQcrwpAAAAAGuPSEfTXi80JNM40V8jmTD21hOp")
                .addOnSuccessListener(
                        requireActivity(), // Pass activity context here
                        new OnSuccessListener<RecaptchaTasksClient>() {
                            @Override
                            public void onSuccess(RecaptchaTasksClient client) {
                                // Assign the RecaptchaTasksClient instance to a variable for later use
                                // MainActivity.this.recaptchaTasksClient = client; // Remove this line, it's not needed
                            }
                        })
                .addOnFailureListener(
                        requireActivity(), // Pass activity context here
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Handle communication errors ...
                                // See "Handle communication errors" section
                            }
                        });
    }


    private void registerUser() {
        String name = editTextName.getText().toString();
        String email = editTextEmail.getText().toString();
        String mobileNumber = editTextMobile.getText().toString();
        String password = editTextPassword.getText().toString();
        String confirmPassword = editTextConfirmPassword.getText().toString();

        // Perform input validation
        if (name.isEmpty() || email.isEmpty() || mobileNumber.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(getContext(), "Invalid email format", Toast.LENGTH_SHORT).show();
        } else if (!isValidMobileNumber(mobileNumber)) {
            Toast.makeText(getContext(), "Invalid mobile number format", Toast.LENGTH_SHORT).show();
        } else if (!password.equals(confirmPassword)) {
            Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
        } else {
            // Register user with Firebase Authentication
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                if (user != null) {
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(name)
                                            .build();
                                    user.updateProfile(profileUpdates)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        sendEmailVerification();
                                                        if (isValidMobileNumber(mobileNumber)) {
                                                            // Start phone number verification
                                                            sendMobileVerification();
                                                        } else {
                                                            Toast.makeText(getContext(), "Invalid mobile number", Toast.LENGTH_SHORT).show();
                                                        }

                                                        saveUserDataToFirestore(user.getUid(), name, email, mobileNumber);
                                                        WelcomeFragment welcomeFragment = new WelcomeFragment();
                                                        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                                                        transaction.replace(R.id.frameLayoutContent, welcomeFragment);
                                                        transaction.addToBackStack(null);
                                                        transaction.commit();
                                                    } else {
                                                        Toast.makeText(getContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }
                            } else {
                                Toast.makeText(getContext(), "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }

    private void loadSignInFragment() {
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.frameLayoutContent, new SignInFragment())
                .commit();
    }

    private boolean isValidMobileNumber(String mobileNumber) {
        return !mobileNumber.isEmpty() && mobileNumber.matches("\\d{10}");
    }

    private void sendEmailVerification() {
        // Implement sending email verification logic here
        // For example:
        String email = editTextEmail.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(getContext(), "Please enter your email address", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "Verification email sent", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Failed to send verification email", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }

    private void sendMobileVerification() {
        // Format the phone number to E.164 format
        String mobileNumber = editTextMobile.getText().toString();
        if (!isValidMobileNumber(mobileNumber)) {
            Toast.makeText(getContext(), "Invalid mobile number", Toast.LENGTH_SHORT).show();
            return;
        }
        String formattedMobileNumber = "+91" + mobileNumber; // Adjust country code as needed
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                        .setPhoneNumber(formattedMobileNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(requireActivity())
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(PhoneAuthCredential credential) {
                                signInWithPhoneAuthCredential(credential);
                            }

                            @Override
                            public void onVerificationFailed(FirebaseException e) {
                                Toast.makeText(getContext(), "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                                mVerificationId = verificationId;
                                mResendToken = token;
                                editTextOTP.setVisibility(View.VISIBLE);
                                btnVerifyOTP.setVisibility(View.VISIBLE);
                            }
                        })
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        // This method is called when phone number verification is completed successfully.
        // You can sign in the user with the provided credential.
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = task.getResult().getUser();
                            if (user != null) {
                                // Update user profile, save data to Firestore, etc.
                                // Then navigate to welcome fragment or any other desired action
                            }
                        } else {
                            Toast.makeText(getContext(), "Verification failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveUserDataToFirestore(String userId, String name, String email, String mobileNumber) {
        // Save user data to Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        UserData userData = new UserData(name, email, mobileNumber);
        db.collection("user_info").document(userId)
                .set(userData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Data saved successfully
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Error saving data
                    }
                });
    }

    // Define a UserData class to represent user data
    private static class UserData {
        private String name;
        private String email;
        private String mobileNumber;

        public UserData(String name, String email, String mobileNumber) {
            this.name = name;
            this.email = email;
            this.mobileNumber = mobileNumber;
        }

        // Getter methods for each field
        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public String getMobileNumber() {
            return mobileNumber;
        }
    }
}


