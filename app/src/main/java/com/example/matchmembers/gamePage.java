package com.example.matchmembers;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.StringTokenizer;

public class gamePage extends AppCompatActivity implements View.OnClickListener {
    ImageView picture;
    Button buttonA;
    Button buttonB;
    Button buttonC;
    Button buttonD;
    Button endButton;
    TextView timer;
    char correct_button;

    TextView score;
    TextView progress;
    int current_score = 0;
    int current_progress = 0;

    HashSet<String> names_to_learn = new HashSet<>();
    int total_names = 0;
    String current_picture_name = "";

    CountDownTimer currentTimer;
    long milliLeft = 0;
    static final char TIME_UP_KEY = '0';

    static final int UPDATE_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_page);

        picture = findViewById(R.id.picture);
        buttonA = findViewById(R.id.buttonA);
        buttonB = findViewById(R.id.buttonB);
        buttonC = findViewById(R.id.buttonC);
        buttonD = findViewById(R.id.buttonD);
        endButton = findViewById(R.id.endGame);
        timer = findViewById(R.id.timer);
        score = findViewById(R.id.score);
        progress = findViewById(R.id.progress);

        Log.d("yeet", "test");
        getNames();
        Log.d("yeet2", "test");

        buttonA.setOnClickListener(this);
        buttonB.setOnClickListener(this);
        buttonC.setOnClickListener(this);
        buttonD.setOnClickListener(this);
        endButton.setOnClickListener(this);
        picture.setOnClickListener(this);
        nextQuestion();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.picture:
                timerPause();
                addContact();
                timerResume();
                break;
            case R.id.buttonA:
                processChoice('A');
                break;
            case R.id.buttonB:
                processChoice('B');
                break;
            case R.id.buttonC:
                processChoice('C');
                break;
            case R.id.buttonD:
                processChoice('D');
                break;
            case R.id.endGame:
                endGame();
        }
    }

    private void endGame() {
        timerPause();

        // example borrowed from https://stackoverflow.com/questions/2115758/how-do-i-display-an-alert-dialog-on-android
        new AlertDialog.Builder(gamePage.this)
                .setTitle("Exit Game")
                .setMessage("Are you sure you want to exit the game?")

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // exit out of game
                        Intent intent = new Intent(gamePage.this, MainActivity.class);
                        intent.putExtra("Score", current_score);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        timerResume();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }

    // 1 = A, 2 = B, 3 = C, 4 = D
    // order, names both arrays of length 4
    private void setButtons(int[] order, String[] names) {
        // store correct answer
        correct_button = (char) ('A' + order[0] - 1);

        // update image to correct picture
        Context context = picture.getContext();
        current_picture_name = names[0];
        int id = context.getResources().getIdentifier(getPicturePath(names[0]), "drawable", context.getPackageName());
        picture.setImageResource(id);

        for (int i=0; i<4; i++) {
            switch (order[i]) {
                case 1:
                    buttonA.setText(names[i]);
                    break;
                case 2:
                    buttonB.setText(names[i]);
                    break;
                case 3:
                    buttonC.setText(names[i]);
                    break;
                case 4:
                    buttonD.setText(names[i]);
                    break;
            }
        }
    }

    private void addContact() {
        ArrayList <ContentProviderOperation> ops = new ArrayList<>();
        ops.add(ContentProviderOperation.newInsert(
                ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        ops.add(ContentProviderOperation.newInsert(
                ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(
                        ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                        current_picture_name).build());

        // Asking the Contact provider to create a new contact
        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            Toast.makeText(this, "Added Contact", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to Add Contact", Toast.LENGTH_SHORT).show();
        }
    }

    private void processChoice(char button_id) {
        // update progress
        currentTimer.cancel();

        current_progress++;
        progress.setText(String.format("Progress: %d%s", (int) (100 * current_progress / total_names), "%"));

        if (correct_button == button_id) {
            // update score
            current_score++;
        } else if (button_id == TIME_UP_KEY) { // if wrong answer, display a Toast
            Toast.makeText(gamePage.this, "Time Ran Out", Toast.LENGTH_SHORT).show();
        } else { // if wrong answer, display a Toast
            Toast.makeText(gamePage.this, "Wrong Answer", Toast.LENGTH_SHORT).show();
        }
        score.setText(String.format("Score: %d / %d", current_score, current_progress));

        nextQuestion();
    }

    private void nextQuestion() {
        setButtons(getRandomButtonOrder(), getRandomNames());
        timerStart(5000); // set a 5 second timer
    }

    private void timerStart(long timeLeft) {
        currentTimer = new CountDownTimer(timeLeft, 1000) {

            @Override
            public void onTick(long milliTillFinish) {
                milliLeft = milliTillFinish;
                timer.setText(String.format("%d",milliTillFinish / 1000));
            }

            @Override
            public void onFinish() {
                processChoice(TIME_UP_KEY); // any char that's not a,b,c,d (no choice was made, which is by default wrong)
            }
        }.start();
    }

    private void timerPause() {
        currentTimer.cancel();
    }

    private void timerResume() {
        timerStart(milliLeft);
    }

    // open names.txt, add all names as Strings to Set object names_to_learn
    private void getNames() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getApplicationContext().getAssets().open("names.txt")))) {
        // try (BufferedReader br = new BufferedReader(new FileReader(new File("names.txt")))) {
            StringTokenizer st = new StringTokenizer(br.readLine(), ",");
            Log.d("yeet3", "test");
            while (st.hasMoreTokens()) {
                String s = st.nextToken();
                names_to_learn.add(s);
                Log.d("name", s);
                total_names++;
            }
            Log.d("yeet", "ya" + names_to_learn.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String[] getRandomNames() {
        return new String[]{drawRandomName(true), drawRandomName(false),
                drawRandomName(false), drawRandomName(false)};
    }

    // draws random name from names.txt
    // Code borrowed from https://stackoverflow.com/questions/124671/picking-a-random-element-from-a-set
    private String drawRandomName(boolean isCorrectAnswer) {
        int size = names_to_learn.size();
        Log.d("size", size+" ");
        int item = new Random().nextInt(size); // In real life, the Random object should be rather more shared than this
        int i = 0;
        String name = "";
        for (String s : names_to_learn) {
            if (i == item) {
                name = s;
                break;
            }
            i++;
        }

        if (isCorrectAnswer) {
            names_to_learn.remove(name); // only remove the correct name from the reamining list of names to iterate through
        }
        return name;
    }

    // converts formal name (ie. "John Smith") to path of image johnsmith.jpg
    private String getPicturePath(String name) {
        return name.replaceAll("\\s+","").toLowerCase();
    }

    // Implementing Fisher–Yates shuffle
    // Code borrowed from https://stackoverflow.com/questions/1519736/random-shuffling-of-an-array
    private int[] getRandomButtonOrder() {
        int[] ar = {1,2,3,4};
        // If running on Java 6 or older, use `new Random()` on RHS here
        Random rnd = new Random();
        for (int i = ar.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            int a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
        return ar;
    }

}
