package com.example.justideas;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.renderscript.Sampler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.justideas.Prevalent.Prevalent;
import com.example.justideas.model.Basket;
import com.example.justideas.model.Products;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.angmarch.views.NiceSpinner;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class ProductDetailsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private ImageView pImage;
    private TextView pTitle, pPrice, pDescription, pCategory, backBtn;
    private EditText pQuantity;
    private Button addToBasketBtn;
    private String pID = "", pColor = "", pSize = "", ImgUrl;
    private DatabaseReference productsRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        pID = getIntent().getStringExtra("pid");

        pImage = (ImageView) findViewById(R.id.productImg_dtl);
        pTitle = (TextView) findViewById(R.id.productName_dtl);
        pPrice = (TextView) findViewById(R.id.productPrice_dtl);
        pDescription = (TextView) findViewById(R.id.productDetailsContent);
        pQuantity = (EditText) findViewById(R.id.productQuantity);
        pCategory = (TextView) findViewById(R.id.productCategory_dtl);
        addToBasketBtn = (Button) findViewById(R.id.basketBtn);
        backBtn = (TextView) findViewById(R.id.back_settings_btn);

        productsRef = FirebaseDatabase.getInstance().getReference().child("Products");



        getProductDetails(pID);
        /*

        Create and populate the spinners for size and color adjustment of products

         */
        NiceSpinner productSize = (NiceSpinner) findViewById(R.id.size_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterSize = ArrayAdapter.createFromResource(this,
                R.array.sizes_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterSize.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        productSize.setAdapter(adapterSize);

        NiceSpinner productColor = (NiceSpinner) findViewById(R.id.color_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterColor = ArrayAdapter.createFromResource(this,
                R.array.colors_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterColor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        productColor.setAdapter(adapterColor);

        pSize = productSize.getItemAtPosition(0).toString();
        pColor = productColor.getItemAtPosition(0).toString();

        productSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                pSize = parent.getItemAtPosition(position - 1).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        productColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                pColor = parent.getItemAtPosition(position - 1).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        addToBasketBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToBasket();
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
    }

    private void addToBasket() {

        String saveCurrentTime, saveCurrentDate;
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat  currentDate = new SimpleDateFormat("mm, dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        final DatabaseReference BasketRef;
        BasketRef = FirebaseDatabase.getInstance().getReference().child("Basket Items");

        final HashMap<String, Object> basketMap = new HashMap<>();
        basketMap.put("pid", pID);
        basketMap.put("name", pTitle.getText().toString());
        basketMap.put("image", ImgUrl);
        basketMap.put("price", pPrice.getText().toString());
        basketMap.put("category", pCategory.getText().toString());
        basketMap.put("date", saveCurrentDate);
        basketMap.put("time", saveCurrentTime);
        basketMap.put("quantity", pQuantity.getText().toString());
        basketMap.put("color", pColor);
        basketMap.put("size", pSize);
        BasketRef.child("User View")
                .child(Prevalent.currentOnlineUser.getPhone())
                .child("Products")
                .child(pID)
                .updateChildren(basketMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            BasketRef.child("Admin View")
                                    .child(Prevalent.currentOnlineUser.getPhone())
                                    .child("Products")
                                    .child(pID)
                                    .updateChildren(basketMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(ProductDetailsActivity.this, "Product Added Successfully", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void getProductDetails(String pID)
    {

        productsRef.child(pID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    Products products = dataSnapshot.getValue(Products.class);

                    pTitle.setText(products.getName());
                    pPrice.setText(products.getPrice());
                    pDescription.setText(products.getDescription());
                    pCategory.setText(products.getCategory());
                    Picasso.get().load(products.getImage()).into(pImage);

                    ImgUrl = products.getImage();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
