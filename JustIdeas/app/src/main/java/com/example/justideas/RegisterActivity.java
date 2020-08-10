package com.example.justideas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.justideas.Prevalent.Prevalent;
import com.example.justideas.model.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import io.paperdb.Paper;

public class RegisterActivity extends AppCompatActivity {

    private Button CreateAccountBtn;
    private EditText InputName, InputPhoneNumber, InputPassword;
    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        CreateAccountBtn = (Button) findViewById(R.id.register_btn);
        InputName = (EditText) findViewById(R.id.register_user_name_input);
        InputPhoneNumber = (EditText) findViewById(R.id.register_phone_number_input);
        InputPassword = (EditText) findViewById(R.id.register_password_input);
        loadingBar = new ProgressDialog(this);

        Paper.init(this);

        CreateAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateAccount();
            }
        });
    }

    private void CreateAccount()
    {
        String name= InputName.getText().toString();
        String phone= InputPhoneNumber.getText().toString();
        String password= InputPassword.getText().toString();

        if (!TextUtils.isEmpty(name))
        {
            if (!TextUtils.isEmpty(phone))
            {
                if (!TextUtils.isEmpty(password))
                {
                    Toast.makeText(this, "Please write your password....", Toast.LENGTH_LONG);
                    loadingBar.setTitle("Create Account");
                    loadingBar.setMessage("Please wait, while your account is verified");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    ValidatePhoneNumber(name, phone, password);
                }
            }
        }
        else {
                Toast.makeText(this, "Please fill all the fields.", Toast.LENGTH_LONG).show();
        }
    }

    private void ValidatePhoneNumber(final String name, final String phone, final String password)
    {
        // reference to database created connects to the firebase database
        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference();

        //
        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // checks to see if data already exists
                if(!(dataSnapshot.child("Users").child(phone).exists())){
                    // Creates a user map to store data
                    HashMap<String, Object> userdataMap = new HashMap<>();
                    // Maps the data to correct place
                    userdataMap.put("phone", phone);
                    userdataMap.put("name", name);
                    userdataMap.put("password", password);
                    // create a parent note for all users
                    RootRef.child("Users").child(phone).updateChildren(userdataMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        Toast.makeText(RegisterActivity.this, "Congratulations, your account has been created!", Toast.LENGTH_LONG).show();
                                        loadingBar.dismiss();
                                        // Pass the data to the prevalent class for user to keep logged in
                                        Paper.book().write(Prevalent.UserPhoneNumber, phone);
                                        Paper.book().write(Prevalent.UserPasswordKey, password);
                                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(RegisterActivity.this,"Network Error: 404 Try Again!", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                }
                            });
                }
                else {
                    // Tells user, phone number exists
                    Toast.makeText(RegisterActivity.this, "This " + phone + "already exists.", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                    Toast.makeText(RegisterActivity.this, "Please use a different number", Toast.LENGTH_SHORT).show();

                    // Sends user to the main activity (main page)
                    Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
