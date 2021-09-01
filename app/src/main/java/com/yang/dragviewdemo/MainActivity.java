package com.yang.dragviewdemo;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.yang.dragview.DragLinearLayout;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DragLinearLayout dragLinearLayout = findViewById(R.id.dragLinearLayout);
        dragLinearLayout.setDragOrientation(DragLinearLayout.DRAG_ORIENTATION_VERTICAL);
    }

    public void clickContainer(View view) {
        Toast.makeText(this, "clickContainer", Toast.LENGTH_SHORT).show();
    }
}