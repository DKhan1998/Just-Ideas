package com.example.justideas_admin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class AddNewProductActivity extends AppCompatActivity {

    private String Price, category, Description, Name, Categories, uploadDate, uploadTime;
    private Button AddNewProduct;
    private ProgressDialog loadingBar;
    private ImageView ProductImage;
    private EditText pName, pDescription, pPrice, pCategory, pColor;
    private static final int GalleryPic = 1;
    private Uri ImageUri;
    private String ProductKey, ImgUrl;
    private StorageReference ProductImgRef;
    private DatabaseReference ProductRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_product);

        category = getIntent().getExtras().get("category").toString();
        ProductImgRef = FirebaseStorage.getInstance().getReference().child("ProductImg");
        ProductRef = FirebaseDatabase.getInstance().getReference().child("Products");
        loadingBar = new ProgressDialog(this);
        AddNewProduct = (Button) findViewById(R.id.add_product);
        ProductImage = (ImageView) findViewById(R.id.product_image);
        pName = (EditText) findViewById(R.id.product_name);
        pDescription = (EditText) findViewById(R.id.product_description);
        pPrice = (EditText) findViewById(R.id.product_price);
        pCategory = (EditText) findViewById(R.id.tag_categories);


        ProductImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                OpenGallery();
            }
        });

        AddNewProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateProductData();
            }
        });

    }

    private void ValidateProductData()
    {
        Description = pDescription.getText().toString();
        Categories = pCategory.getText().toString();
        Name = pName.getText().toString();
        Price = pPrice.getText().toString();

        if(ImageUri == null)
        {
            Toast.makeText(this, "Product Image Mandatory", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(Description))
        {
            Toast.makeText(this, "Please Write Product Description", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(Name))
        {
            Toast.makeText(this, "Please Specify a name", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(Categories))
        {
            Toast.makeText(this, "Please Specify Categories", Toast.LENGTH_SHORT).show();
        }
        else if (Price == null)
        {
            Toast.makeText(this, "Please Specify a Price", Toast.LENGTH_SHORT).show();
        }
        else
        {
            StoreProductInfo();
        }
    }

    private void StoreProductInfo()
    {
        loadingBar.setTitle("Launching Product");
        loadingBar.setMessage("Please wait.....");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("mm, dd, yyyy");
        uploadDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        uploadTime = currentTime.format(calendar.getTime());

        ProductKey = uploadDate + uploadTime;

        final StorageReference filePath = ProductImgRef.child(ImageUri.getLastPathSegment() + ProductKey + ".jpg");

        final UploadTask uploadTask = filePath.putFile(ImageUri);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String message = e.toString();
                Toast.makeText(AddNewProductActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();

                loadingBar.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(AddNewProductActivity.this, "Product uploaded successfully...", Toast.LENGTH_SHORT).show();

                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if(!task.isSuccessful()) {
                            throw task.getException();
                        }

                        ImgUrl = filePath.getDownloadUrl().toString();
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            ImgUrl = task.getResult().toString();

                            Toast.makeText(AddNewProductActivity.this, "Product Image Saved successfully", Toast.LENGTH_SHORT).show();

                            SaveProductInfoToDatabase();
                        }
                    }
                });
            }
        });
    }

    private void SaveProductInfoToDatabase() {
        HashMap<String, Object> productMap = new HashMap<>();
        productMap.put("pid", ProductKey);
        productMap.put("date", uploadDate);
        productMap.put("time", uploadTime);
        productMap.put("description", Description);
        productMap.put("image", ImgUrl);
        productMap.put("category", Categories);
        productMap.put("name", Name);
        productMap.put("price", Price);

        ProductRef.child(ProductKey).updateChildren(productMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Intent intent = new Intent(AddNewProductActivity.this, CategoryActivity.class);
                        startActivity(intent);

                        loadingBar.dismiss();
                        Toast.makeText(AddNewProductActivity.this, "Product uploaded successfully...", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        loadingBar.dismiss();
                        String message = task.getException().toString();
                        Toast.makeText(AddNewProductActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    private void OpenGallery()
    {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GalleryPic);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GalleryPic && resultCode == RESULT_OK && data!=null)
        {
            ImageUri = data.getData();
            ProductImage.setImageURI(ImageUri);
        }
    }
}
