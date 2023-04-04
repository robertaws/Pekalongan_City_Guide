package com.binus.pekalongancityguide.Layout;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.binus.pekalongancityguide.R;

public class FoodDetails extends AppCompatActivity {
    ImageButton back;
    ImageView image,image1,image2,image3;
    TextView name,desc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_details);
        init();
        back.setOnClickListener(v -> onBackPressed());
        getData();
    }

    private void getData() {
        image.setBackgroundResource(this.getIntent().getIntExtra("imeg", R.drawable.food1));
        image1.setBackgroundResource(this.getIntent().getIntExtra("imeg1", R.drawable.food1_1));
        image2.setBackgroundResource(this.getIntent().getIntExtra("imeg2", R.drawable.food1_2));
        image3.setBackgroundResource(this.getIntent().getIntExtra("imeg3", R.drawable.food1_3));
        name.setText(this.getIntent().getStringExtra("name"));
        desc.setText("\t\t\t\t\t" + this.getIntent().getStringExtra("desc"));
    }
    private void init(){
        back = findViewById(R.id.backHome);
        image = findViewById(R.id.cul_img);
        image1 = findViewById(R.id.cul_img1);
        image2 = findViewById(R.id.cul_img2);
        image3 = findViewById(R.id.cul_img3);
        name = findViewById(R.id.cul_name);
        desc = findViewById(R.id.cul_desc);
    }
}