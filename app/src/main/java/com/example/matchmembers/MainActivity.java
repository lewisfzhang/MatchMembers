package com.example.matchmembers;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button start;
    TextView title;
    TextView lastScore;
    public static final int UPDATE_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        start = findViewById(R.id.start_button);
        title = findViewById(R.id.title);
        lastScore = findViewById(R.id.previousScore);

        start.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_button:
                Intent intent = new Intent(MainActivity.this, gamePage.class);
                startActivityForResult(intent, UPDATE_CODE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UPDATE_CODE && resultCode == RESULT_OK) {
            int score = data.getIntExtra("Score", 0);
            lastScore.setText(String.format("Last Score: %d", score));
        }
    }
}
