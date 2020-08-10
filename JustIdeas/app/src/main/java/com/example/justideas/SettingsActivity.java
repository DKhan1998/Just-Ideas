package com.example.justideas;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.justideas.Prevalent.Prevalent;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private CircleImageView profileImageSetting;
    private EditText nameSetting, phoneSetting, passwordSetting, addressSetting;

    private Uri imageUri;
    private String myUrl = "";
    private StorageTask uploadTask;
    private StorageReference storageProfileRef;
    private Button imageUpload, updateBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        storageProfileRef = FirebaseStorage.getInstance().getReference().child("Profile Pictures");
        profileImageSetting = (CircleImageView) findViewById(R.id.pp_chng_btn);
        nameSetting = (EditText) findViewById(R.id.name_update_btn);
        phoneSetting = (EditText) findViewById(R.id.phone_update_btn);
        passwordSetting = (EditText) findViewById(R.id.password_update_btn);
        addressSetting = (EditText) findViewById(R.id.address_update_btn);
        TextView closeBtn = (TextView) findViewById(R.id.close_settings_btn);
        updateBtn = (Button) findViewById(R.id.update_settings_btn);
        imageUpload = (Button) findViewById(R.id.imageUploadBtn);

        userInfoDisplay(nameSetting, profileImageSetting, phoneSetting, passwordSetting, addressSetting);

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                userInfoUpdate();
            }
        });

        profileImageSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                // start cropping activity for pre-acquired image saved on the device
                CropImage.activity(imageUri)
                        .setAspectRatio(1,1)
                        .start(SettingsActivity.this);
            }
        });

       imageUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog = new ProgressDialog(SettingsActivity.this);
                progressDialog.setTitle("Uploading Image");
                progressDialog.setMessage("Please wait whilst your image is uploaded!");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                if(imageUri != null)
                {
                    final StorageReference fileRef = storageProfileRef
                            .child(Prevalent.currentOnlineUser.getPhone() + ".jpg");
                    uploadTask = fileRef.putFile(imageUri);

                    uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot,Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws
                                Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            return fileRef.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if(task.isSuccessful()){
                                try {
                                    Uri downloadUrl = task.getResult();
                                    myUrl = downloadUrl.toString();

                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
                                    HashMap<String, Object> userMap = new HashMap<>();
                                    userMap.put("image", myUrl);
                                    ref.child(Prevalent.currentOnlineUser.getPhone()).updateChildren(userMap);
                                    progressDialog.dismiss();

                                    Toast.makeText(SettingsActivity.this, "Profile Info Updated", Toast.LENGTH_SHORT).show();
                                } catch (Exception e){
                                    Toast.makeText(SettingsActivity.this, "failed to upload" + "Error : " + e.toString(), Toast.LENGTH_SHORT).show();
                                }

                            }
                            else {
                                progressDialog.dismiss();
                                Toast.makeText(SettingsActivity.this, "error.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else
                {
                    progressDialog.dismiss();
                    Toast.makeText(SettingsActivity.this, "Image not selected.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode==RESULT_OK && data!=null)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();
            profileImageSetting.setImageURI(imageUri);
        } else {
            Toast.makeText(this, "Error Try again.", Toast.LENGTH_LONG);
            startActivity(new Intent(SettingsActivity.this, SettingsActivity.class));
        }
    }

    private void userInfoUpdate()
    {
        if(TextUtils.isEmpty(nameSetting.getText().toString()))
        {
            Toast.makeText(this, "Name is mandatory!", Toast.LENGTH_SHORT);
        }
        else if (TextUtils.isEmpty(phoneSetting.getText().toString()))
        {
            Toast.makeText(this, "Phone number is mandatory!", Toast.LENGTH_SHORT);
        }
        else if (TextUtils.isEmpty(addressSetting.getText().toString()))
        {
            Toast.makeText(this, "Address is mandatory!", Toast.LENGTH_SHORT);
        }
        else if (TextUtils.isEmpty(passwordSetting.getText().toString()))
        {
            Toast.makeText(this, "Password is mandatory!", Toast.LENGTH_SHORT);
        }
        else
        {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
            HashMap<String, Object> userMap = new HashMap<>();
            userMap.put("name", nameSetting.getText().toString());
            userMap.put("address", addressSetting.getText().toString());
            userMap.put("phone", phoneSetting.getText().toString());
            userMap.put("password", passwordSetting.getText().toString());

            ref.child(Prevalent.currentOnlineUser.getPhone()).updateChildren(userMap);
            startActivity(new Intent(SettingsActivity.this, HomeActivity.class));
            Toast.makeText(SettingsActivity.this, "Profile Info Updated", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void userInfoDisplay(EditText nameSetting, CircleImageView profileSettingsView, EditText phoneSetting, EditText passwordSetting, EditText addressSetting)
    {
        DatabaseReference UsersRef = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(Prevalent.currentOnlineUser.getPhone());

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.child("image").exists())
                    {
                        String image = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(image).into(profileSettingsView);
                    }
                    if(dataSnapshot.child("address").exists())
                    {
                        String address = dataSnapshot.child("address").getValue().toString();
                        addressSetting.setText(address);
                    }
                        String name = dataSnapshot.child("name").getValue().toString();
                        String phone = dataSnapshot.child("phone").getValue().toString();
                        String pass = dataSnapshot.child("password").getValue().toString();
                        nameSetting.setText(name);
                        phoneSetting.setText(phone);
                        passwordSetting.setText(pass);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }
}
