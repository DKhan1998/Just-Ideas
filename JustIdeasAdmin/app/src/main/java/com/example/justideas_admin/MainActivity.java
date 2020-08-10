package com.example.justideas_admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.justideas_admin.Prevalent.Prevalent;
import com.example.justideas_admin.model.Admins;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {

    private EditText InputID, InputPassword;
    private Button LoginButton;
    private ProgressDialog loadingBar;

    private String parentDbName = "Admins";
    private CheckBox CheckboxRememberMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LoginButton = (Button) findViewById(R.id.login_btn);
        InputID = (EditText) findViewById(R.id.login_ID_input);
        InputPassword = (EditText) findViewById(R.id.login_password_input);
        loadingBar = new ProgressDialog(this);

        CheckboxRememberMe = (CheckBox) findViewById(R.id.remember_me_chkb);
        Paper.init(this);

        //  Admin to be signed in automatically
        String AdminID = Paper.book().read(Prevalent.AdminID);
        String AdminPasswordKey = Paper.book().read(Prevalent.AdminPasswordKey);

        if (AdminID != "" && AdminPasswordKey != "")
        {
            if (!TextUtils.isEmpty(AdminID) && !TextUtils.isEmpty(AdminPasswordKey))
            {
                loadingBar.setTitle("Already Logged in");
                loadingBar.setMessage("Please wait.....");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                AllowAccessToAccount(AdminID, AdminPasswordKey);
            }
        }

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });
    }

    private void loginUser() {
        String ID = InputID.getText().toString();
        String password= InputPassword.getText().toString();

        if (TextUtils.isEmpty(ID)){
            Toast.makeText(this, "Please write your Admin ID....", Toast.LENGTH_LONG);
            loadingBar.dismiss();
        } else if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please write your password....", Toast.LENGTH_LONG);
        } else {
            loadingBar.setTitle("Login Account");
            loadingBar.setMessage("Please wait, while your account is verified");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            AllowAccessToAccount(ID, password);
        }
    }

    private void AllowAccessToAccount(String ID, String password) {

        if (CheckboxRememberMe.isChecked()){
            // Pass the data to the prevalent class for user to keep logged in
            Paper.book().write(Prevalent.AdminID, ID);
            Paper.book().write(Prevalent.AdminPasswordKey, password);
        }

        // reference to database created connects to the firebase database
        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference();

        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(parentDbName).child(ID).exists())
                {
                    Admins usersData = dataSnapshot.child(parentDbName).child(ID).getValue(Admins.class);

                    if (usersData.getAdminID().equals(ID))
                    {
                        if (usersData.getPassword().equals(password))
                        {
                            Toast.makeText(MainActivity.this, "Logged in successfully....", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();

                            Intent intent = new Intent(MainActivity.this, CategoryActivity.class);
                            startActivity(intent);
                        }
                        else {
                            Toast.makeText(MainActivity.this, "Password incorrect!", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                }
                else {
                    Toast.makeText(MainActivity.this, "Account with this " + ID + "Does not exists", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}

