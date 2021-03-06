package com.admin.oes;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.developer.filepicker.controller.DialogSelectionListener;
import com.developer.filepicker.model.DialogConfigs;
import com.developer.filepicker.model.DialogProperties;
import com.developer.filepicker.view.FilePickerDialog;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class Upload extends AppCompatActivity {

    TextView info;
    int count = 0, keyc = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        info = findViewById(R.id.textup);

                DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.SINGLE_MODE;
        properties.root = new File(DialogConfigs.DEFAULT_DIR);
        properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        properties.offset = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = null;
        properties.show_hidden_files = false;
        FilePickerDialog dialog = new FilePickerDialog(Upload.this,properties);
        dialog.setTitle("Select a File");
        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                Log.d("TEST" , files[0]);
                set(files[0]);
            }
        });
        dialog.show();

    }

    public void set(String addr) {
        String json = null;
        try {
            FileInputStream fis = new FileInputStream(new File(addr ));  // 2nd line
            int size = fis.available();
            byte[] buffer = new byte[size];
            fis.read(buffer);
            fis.close();

            json = new String(buffer, StandardCharsets.UTF_8);

            JSONObject jsonObject = new JSONObject(json);
            Iterator<String> iter = jsonObject.keys();
            while(iter.hasNext()){
                String key = iter.next();
                JSONObject data = jsonObject.getJSONObject(key);
                keyc++;
                for (int i = 1; i < data.length() + 1 ; i++){
                    JSONObject data2 = data.getJSONObject(String.valueOf(i));

                    String QUESTION = data2.getString("QUESTION");
                    String OPT_A = data2.getString("OPT A");
                    String OPT_B = data2.getString("OPT B");
                    String OPT_C = data2.getString("OPT C");
                    String OPT_D = data2.getString("OPT D");
                    String CORRECT_ANS = data2.getString("CORRECT ANS");
                    String EXPLANATION = data2.getString("EXPLANATION");
                    count++;
                    info.setText("No of questions uploaded:  " + count + " From:  " + keyc + " Topics");

//To save data in Firebase Database
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Tests/" + key);
                    databaseReference.child("QUESTION").setValue(QUESTION);
                    databaseReference.child("OPT_A").setValue(OPT_A);
                    databaseReference.child("OPT_B").setValue(OPT_B);
                    databaseReference.child("OPT_C").setValue(OPT_C);
                    databaseReference.child("OPT_D").setValue(OPT_D);
                    databaseReference.child("CORRECT_ANS").setValue(CORRECT_ANS);
                    databaseReference.child("EXPLANATION").setValue(EXPLANATION);



                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
