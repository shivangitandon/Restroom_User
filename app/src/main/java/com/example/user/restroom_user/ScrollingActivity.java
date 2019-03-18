package com.example.user.restroom_user;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.mongodb.internal.connection.Time;
import com.mongodb.lang.NonNull;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.mongodb.stitch.core.auth.providers.anonymous.AnonymousCredential;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteInsertOneResult;

import org.bson.Document;

import java.sql.Timestamp;
import java.text.DateFormat;


public class ScrollingActivity extends AppCompatActivity {

    TextView location;
    EditText email;
    EditText msg;
    SeekBar odour;
    RatingBar cleanliness;
    RatingBar rating;
    RadioGroup soap;
    RadioGroup water;
    RadioButton soapvalue;
    RadioButton watervalue;
    Button submit;
    RemoteMongoCollection feedbackcollection;
    RemoteMongoCollection restroomcollection;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        location=(TextView)findViewById(R.id.textview1);
        email=(EditText)findViewById(R.id.edittext2);
        msg=(EditText)findViewById(R.id.edittext1);
        odour=(SeekBar)findViewById(R.id.seekBar);
        cleanliness=(RatingBar)findViewById(R.id.ratingBar1);
        rating=(RatingBar)findViewById(R.id.ratingBar2);
        soap=(RadioGroup)findViewById(R.id.radiogroup1);
        water=(RadioGroup)findViewById(R.id.radiogroup2);
        submit=(Button)findViewById(R.id.button1);

        int restroom_number=5;
        Stitch.initializeDefaultAppClient(
                getResources().getString(R.string.my_app_id)
        );

        StitchAppClient stitchAppClient = Stitch.getDefaultAppClient();

        Stitch.getDefaultAppClient().getAuth().loginWithCredential(new AnonymousCredential()).addOnCompleteListener(new OnCompleteListener<StitchUser>() {
            @Override
            public void onComplete(@NonNull final Task<StitchUser> task) {
                if (task.isSuccessful()) {
                    Log.d("stitch", "logged in anonymously");
                } else {
                    Log.e("stitch", "failed to log in anonymously", task.getException());
                }
            }
        }
        );
        RemoteMongoClient mongoClient = stitchAppClient.getServiceClient(
                RemoteMongoClient.factory,
                "mongodb-atlas"
        );

        feedbackcollection= mongoClient.getDatabase("Restroom_Management").getCollection("user_feedback");
        restroomcollection= mongoClient.getDatabase("Restroom_Management").getCollection("restrooms");

        submit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                Toast.makeText(getApplicationContext(),"Uploading feedback",Toast.LENGTH_SHORT).show();
                String restroom_location=location.getText().toString();

                String emailid=email.getText().toString();

                int odour_level=((odour.getProgress()+1)/2);

                int cleanliness_level=(int)cleanliness.getRating()+1;

                int selectedid1=soap.getCheckedRadioButtonId();
                soapvalue=(RadioButton)findViewById(selectedid1);
                boolean is_soap;
                if(soapvalue.getText().toString().equals("Yes"))
                {
                    is_soap=true;
                }
                else
                {
                    is_soap=false;
                }

                int selectedid2=water.getCheckedRadioButtonId();
                watervalue=(RadioButton)findViewById(selectedid2);
                boolean is_water;
                if(watervalue.getText().toString().equals("Yes"))
                {
                    is_water=true;
                }
                else
                {
                    is_water=false;
                }

                int overall_rating=(int)rating.getRating()+1;

                String feedback_msg=msg.getText().toString();

                //Timestamp timestamp=new Timestamp(System.currentTimeMillis());

                Document newItem = new Document()
                        .append("number",restroom_number)
                        .append("location", restroom_location)
                        .append("emailid",emailid)
                        .append("odour_level", odour_level)
                        .append("cleanliness_level", cleanliness_level)
                        .append("is_soap", is_soap)
                        .append("is_water", is_water)
                        .append("overall_rating", overall_rating)
                        .append("feedback_msg", feedback_msg);

                final Task <RemoteInsertOneResult> insertTask = feedbackcollection.insertOne(newItem);
                insertTask.addOnCompleteListener(new OnCompleteListener <RemoteInsertOneResult> () {
                    @Override
                    public void onComplete(@NonNull Task <RemoteInsertOneResult> task) {
                        if (task.isSuccessful()) {
                            //Toast.makeText(getApplicationContext(),"Feedback recorded",Toast.LENGTH_SHORT).show();
                            Intent intent=new Intent(ScrollingActivity.this, ExitActivity.class);
                            startActivity(intent);
                            finish();
                           // Log.d("app", String.format("successfully inserted item with id %s",
                             //       task.getResult().getInsertedId()));
                        } else {
                            Toast.makeText(getApplicationContext(),"Feedback Upload fail",Toast.LENGTH_SHORT).show();
                            //Log.e("app", "failed to insert document with: ", task.getException());
                        }
                    }
                });
            }
        });

    }

}
