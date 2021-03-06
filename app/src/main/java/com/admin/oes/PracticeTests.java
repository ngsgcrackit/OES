package com.admin.oes;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.format.DateFormat;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PracticeTests extends AppCompatActivity {
    private ProgressBar progressBar;
    private TextView quest_tv,timer;
    int answered=0;
    int MAX_STEP, selectedId, position = 0;
    int current_step = 1,corect = 0,Wrong=0;
    ArrayList<String> que = new ArrayList<String>();
    ArrayList<String> A = new ArrayList<String>();
    ArrayList<String> B = new ArrayList<String>();
    ArrayList<String> C = new ArrayList<String>();
    ArrayList<String> D = new ArrayList<String>();
    ArrayList<String> ans = new ArrayList<String>();
    ArrayList<String> key = new ArrayList<String>();
   // ArrayList<String> marksa = new ArrayList<String>();
    ArrayList<String> dbans = new ArrayList<String>();
    private RadioGroup radioquestionGroup;
    private RadioButton radioans;
    RadioButton Ar,Br,Cr,Dr;
    String keyi,sub;
    FirebaseAuth firebaseAuth;
    private QuestionModel questionModel;
    private ArrayList<QuestionModel> myList;
    private SharedPreferences sharedPreferences;
    CountDownTimer countDownTimer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice_tests);
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.black));
        timer = findViewById(R.id.counttowntimer);

        getSupportActionBar().setSubtitle("");
        sharedPreferences = getApplicationContext().getSharedPreferences("sp", 0);

        firebaseAuth = FirebaseAuth.getInstance();
        Intent intent = getIntent();
        keyi = intent.getStringExtra("td");
        sub=intent.getStringExtra("sub_name");
        setTitle(keyi);
        //String parent = dataSnapshot.getKey();


        Ar = findViewById(R.id.a);
        Br = findViewById(R.id.b);
        Cr = findViewById(R.id.c);
        Dr = findViewById(R.id.d);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Subjects").child(sub).child(keyi).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // get total available quest
                String parent = dataSnapshot.getKey();
                MAX_STEP = (int) dataSnapshot.getChildrenCount();
                Toast.makeText(PracticeTests.this, MAX_STEP+"", Toast.LENGTH_SHORT).show();
                for (int i = 1; i <= MAX_STEP; i++) {
                    dbans.add("");
                }

                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    if (childDataSnapshot.hasChild("QUESTION") &&
                            childDataSnapshot.hasChild("OPT A") &&
                            childDataSnapshot.hasChild("OPT B") &&
                            childDataSnapshot.hasChild("OPT C") &&
                            childDataSnapshot.hasChild("OPT D") &&
                            childDataSnapshot.hasChild("CORRECT ANS")) {
                        que.add(childDataSnapshot.child("QUESTION").getValue().toString());
                        A.add(childDataSnapshot.child("OPT A").getValue().toString());
                        B.add(childDataSnapshot.child("OPT B").getValue().toString());
                        C.add(childDataSnapshot.child("OPT C").getValue().toString());
                        D.add(childDataSnapshot.child("OPT D").getValue().toString());
                        ans.add(childDataSnapshot.child("CORRECT ANS").getValue().toString());
                        //                    marksa.add(childDataSnapshot.child("marks").getValue().toString());
                        key.add(childDataSnapshot.getKey());
                    }
                    else
                    {
                        Toast.makeText(PracticeTests.this, "Data is Improper", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
                steppedprogress();
                starttest(0);
                timer();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    public void timer() {
        countDownTimer = new CountDownTimer(MAX_STEP * 60000, 1000) {
            public void onTick(long millisUntilFinished) {
                String text = String.format(Locale.getDefault(), "%02d min: %02d sec",
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60,
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60);
                timer.setText(text);
            }

            public void onFinish() {
                Date d = new Date();
                CharSequence s = DateFormat.format("MMMM d, yyyy HH:mm:ss", d.getTime());
                SharedPreferences sharedPreferences;
                sharedPreferences = getApplicationContext().getSharedPreferences("sp", 0);
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users/" + firebaseAuth.getUid() + "/Exams/" + keyi);
                databaseReference.child("Name").setValue(sharedPreferences.getString("name", "0"));
                databaseReference.child("TotalQ").setValue(MAX_STEP);
                databaseReference.child("Correctans").setValue(corect);
                databaseReference.child("wrongans").setValue(Wrong);
                databaseReference.child("UnAnswered").setValue(MAX_STEP-corect-Wrong);
                databaseReference.child("Time").setValue(s);
                databaseReference.child("Teacher").setValue("Temp.....");
                databaseReference.child("ID").setValue("Temp.....");
                Log.d("Subbmitted", "kduhasuyfj");
                myList = new ArrayList<>();
                QuestionsModel model = new QuestionsModel();
                for (int j = 0; j < dbans.size(); j++) {
                    questionModel = new QuestionModel();
                    questionModel.setAnswer(dbans.get(j));
                    questionModel.setCorrectAnswer(ans.get(j));
                    questionModel.setQuestionNumber(que.get(j));
                    myList.add(questionModel);
                }
                Log.i("xyz", String.valueOf(corect));
                model.setQuestions(myList);
                databaseReference.child(keyi).setValue(model);
                Log.d("correct", String.valueOf(corect));
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("LastTakenTest",keyi);
                editor.putString("lastScore",String.valueOf(corect));
                editor.putString("lastwrong",String.valueOf(Wrong));
                editor.putString("UNAnswered",String.valueOf(MAX_STEP-answered));

                editor.apply();
                    /*databaseReference.child("Ques").child(String.valueOf(j)).setValue(que.get(j));
                    databaseReference.child("CorrectAnswer").child(String.valueOf(j)).setValue(ans.get(j));
                    databaseReference.child("Answer").child(String.valueOf(j)).setValue(dbans.get(j));*/

                Toast.makeText(PracticeTests.this, "Submitted..", Toast.LENGTH_SHORT).show();
                finish();
                //  Toast.makeText(Test.this, "Katham!!", Toast.LENGTH_SHORT).show();
            }
        }.start();
    }

    public void starttest(int pos) {

        if (pos <= MAX_STEP - 1) {
            quest_tv.setText(que.get(pos));
            Ar.setText(A.get(pos));
            Br.setText(B.get(pos));
            Cr.setText(C.get(pos));
            Dr.setText(D.get(pos));
        } else {
        }
    }

    public void setans(int pro) {
        radioquestionGroup = findViewById(R.id.radioGroup);
        selectedId = radioquestionGroup.getCheckedRadioButtonId();
        radioans = (RadioButton) findViewById(selectedId);
        if (selectedId == -1) {
            dbans.set(pro, "Ntg selected");
        } else {
            answered++;
            if (radioans.getText().equals(ans.get(pro))) {
                dbans.set(pro, radioans.getText().toString());
                corect++;
            } else {
                dbans.set(pro, radioans.getText().toString());
                Wrong++;
            }
        }
        radioquestionGroup.clearCheck();
    }

    private void steppedprogress() {
        quest_tv = (TextView) findViewById(R.id.question);
        quest_tv.setMovementMethod(new ScrollingMovementMethod());
        progressBar = (ProgressBar) findViewById(R.id.progress);
        progressBar.setMax(MAX_STEP);
        progressBar.setProgress(current_step);
        progressBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);

        (findViewById(R.id.lyt_back)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (current_step != 1) {
                    backStep(current_step);
                } else {
                    Toast.makeText(PracticeTests.this, "First question", Toast.LENGTH_SHORT).show();
                }
            }
        });

        (findViewById(R.id.lyt_next)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextStep(current_step);
            }
        });

        String str_progress = String.format(getString(R.string.step_of), current_step, MAX_STEP);
        quest_tv.setText(str_progress);
    }

    private void nextStep(int progress) {
        if (progress <= MAX_STEP) {
            progress++;
            current_step = progress;
            ViewAnimation.fadeOutIn(quest_tv);
        }
        TextView tv = findViewById(R.id.next_test);

        if (tv.getText().equals("Submit")) {
//            Wrong = MAX_STEP-1 - corect;
            showtestexitDialog();
        }

        if (current_step >= MAX_STEP) {
            tv.setText("Submit");
        } else {
            tv.setText("Next");
        }

        //TODO:Guess it's being called 2 times at last

        if (current_step >= MAX_STEP + 1) {
            current_step--;
            setans(position);
            //  Toast.makeText(this, corect+"", Toast.LENGTH_SHORT).show();
        } else {

            position++;
        }
        progressBar.setProgress(current_step);
        setans(position - 1);
        starttest(position);
    }

    private void backStep(int progress) {
        if (progress > 1) {
            progress--;
            current_step = progress;
            ViewAnimation.fadeOutIn(quest_tv);
            position--;
        }

        progressBar.setProgress(current_step);
        if (position >= 0) {
            setans(position - 1);
            starttest(position);
        }

    }

    //Exit Dialog
    private void showtestexitDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.exittest);
        dialog.setCancelable(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        (dialog.findViewById(R.id.bt_submit)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date d = new Date();
                CharSequence s = DateFormat.format("MMMM d, yyyy HH:mm:ss", d.getTime());
                SharedPreferences sharedPreferences;
                sharedPreferences = getApplicationContext().getSharedPreferences("sp", 0);
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users/" + firebaseAuth.getUid() + "/Exams/" + keyi);
                databaseReference.child("Name").setValue(sharedPreferences.getString("name", "0"));
                databaseReference.child("TotalQ").setValue(MAX_STEP);
                databaseReference.child("Correctans").setValue(corect);
                databaseReference.child("wrongans").setValue(Wrong);
                databaseReference.child("UnAnswered").setValue(MAX_STEP-corect-Wrong);
                databaseReference.child("Time").setValue(s);
                databaseReference.child("Teacher").setValue("Temp.....");
                databaseReference.child("ID").setValue("Temp.....");
                Log.d("Subbmitted", "kduhasuyfj");
                myList = new ArrayList<>();
                QuestionsModel model = new QuestionsModel();
                for (int j = 0; j < dbans.size(); j++) {
                    questionModel = new QuestionModel();
                    questionModel.setAnswer(dbans.get(j));
                    questionModel.setCorrectAnswer(ans.get(j));
                    questionModel.setQuestionNumber(que.get(j));
                    myList.add(questionModel);
                }
                Log.i("xyz", String.valueOf(corect));
                model.setQuestions(myList);
                databaseReference.child(keyi).setValue(model);
                Log.d("correct", String.valueOf(corect));
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("LastTakenTest",keyi);
                editor.putString("lastScore",String.valueOf(corect));
                editor.putString("lastwrong",String.valueOf(Wrong));
                editor.putString("UNAnswered",String.valueOf(MAX_STEP-answered));

                editor.apply();
                    /*databaseReference.child("Ques").child(String.valueOf(j)).setValue(que.get(j));
                    databaseReference.child("CorrectAnswer").child(String.valueOf(j)).setValue(ans.get(j));
                    databaseReference.child("Answer").child(String.valueOf(j)).setValue(dbans.get(j));*/

                Toast.makeText(PracticeTests.this, "Submitted..", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                finish();
            }
        });

        (dialog.findViewById(R.id.bt_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(PracticeTests.this, "Back clicked", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);

    }

    public void Quit(View view) {
        showtestexitDialog();
    }
}
