package com.example.justideas_admin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class CategoryActivity extends AppCompatActivity {

    private Button menC, womanC, childC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        menC = (Button) findViewById(R.id.mensCat);
        womanC = (Button) findViewById(R.id.womansCat);
        childC = (Button) findViewById(R.id.childCat);

        menC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(CategoryActivity.this, AddNewProductActivity.class);
                intent.putExtra("category", "Men");
                startActivity(intent);
            }
        });

        womanC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CategoryActivity.this, AddNewProductActivity.class);
                intent.putExtra("category", "Women");
                startActivity(intent);
            }
        });

        childC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CategoryActivity.this, AddNewProductActivity.class);
                intent.putExtra("category", "Children");
                startActivity(intent);
            }
        });
    }
}
