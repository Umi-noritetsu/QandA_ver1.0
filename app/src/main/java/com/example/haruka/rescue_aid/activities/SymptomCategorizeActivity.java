package com.example.haruka.rescue_aid.activities;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.haruka.rescue_aid.R;
import com.example.haruka.rescue_aid.recognition_list.ListSymptom;

import java.util.ArrayList;

/**
 * Created by Tomoya on 9/13/2017 AD.
 */

public class SymptomCategorizeActivity extends ReadAloudTestActivity {

    ImageButton BtnToIll, BtnToInjury;
    Intent interviewIntent;
    private ArrayList<String>[] dictionary;

    //final String scenarioIll = "scenario.csv";
    //final String scenarioIll = "scenario_17091501.csv";
    //final String scenarioIll = Utils.SCENARIOS_ILL; //"scenario_17091701.csv";
    //final String scenarioInjury = Utils.SCENARIOS_INJURY; //"text4.csv";
    final int scenarioIdIll = 0;
    final int scenarioIdInjury = 1;


    SpeechRecognizer sr;

    class SpeechListener implements RecognitionListener {

        @Override
        public void onBeginningOfSpeech() {
            // TODO Auto-generated method stub

        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onEndOfSpeech() {
            // TODO Auto-generated method stub

        }

        @Override
        public void onError(int error) {
            if(error == 9) {
                //get Permission
                ActivityCompat.requestPermissions(SymptomCategorizeActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 0);
            }else if (error != 7){
                Toast.makeText(getApplicationContext(), "エラー " + error, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onReadyForSpeech(Bundle params) {
            Toast.makeText(getApplicationContext(), "認識開始", Toast.LENGTH_SHORT).show();
        }

        private void voiceAnswer(ArrayList<String> candidates){
            int yes = 0;
            Log.d("voice answer", candidates.get(0));
            for(yes = 0; yes < 2; yes++){
                for(int index = 0; index < dictionary[yes].size(); index++){
                    if(dictionary[yes].get(index).equals(candidates.get(0))){
                        Toast.makeText(getApplicationContext(), (yes == 0) ?"Yes":"No" , Toast.LENGTH_SHORT).show();
                        if(yes == 0){
                            BtnToIll.callOnClick();
                        }else{
                            BtnToInjury.callOnClick();
                        }
                        break;
                    }
                }
            }

        }

        @Override
        public void onResults(Bundle results) {
            ArrayList<String> candidates = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            voiceAnswer(candidates);
        }

        @Override
        public void onRmsChanged(float rmsdB) {
            // TODO Auto-generated method stub

        }

    }


    private void setSpeechRecognizer(){
        Log.i("progress", "speech recognizer started to be set");

        sr = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
        sr.setRecognitionListener(new SpeechListener());
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        sr.startListening(intent);
    }

    private void askSymptom(){
        speechText("急病ですか、怪我ですか");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptom_categorize);

        interviewIntent = new Intent(this, InterviewActivity.class);

        BtnToIll = (ImageButton)findViewById(R.id.btn_to_ill);
        BtnToIll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO go to ill interview
                //interviewIntent.putExtra("SCENARIO", scenarioIll);
                interviewIntent.putExtra("SCENARIO_ID", scenarioIdIll);
                startActivity(interviewIntent);
            }
        });
        BtnToInjury = (ImageButton)findViewById(R.id.btn_to_injury);
        BtnToInjury.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO go to injury interview
                //interviewIntent.putExtra("SCENARIO", scenarioInjury);
                interviewIntent.putExtra("SCENARIO_ID", scenarioIdInjury);
                startActivity(interviewIntent);
            }
        });

        askSymptom();
        dictionary = ListSymptom.getDictionary();

    }


    protected void setTtsListener(){
        // android version more than 15th
        if (Build.VERSION.SDK_INT >= 15)
        {
            int listenerResult = tts.setOnUtteranceProgressListener(new UtteranceProgressListener()
            {

                public void onDone(String utteranceId) {
                    // TODO Auto-generated method stub
                    SymptomCategorizeActivity.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            setSpeechRecognizer();
                        }
                    });
                }

                @Override
                public void onError(String utteranceId)
                {
                    Log.d(TAG,"progress on Error " + utteranceId);
                }

                @Override
                public void onStart(String utteranceId)
                {
                    Log.d(TAG,"progress on Start " + utteranceId);
                }

            });
            if (listenerResult != TextToSpeech.SUCCESS)
            {
                Log.e(TAG, "failed to add utterance progress listener");
            }
        }
        else {
            Log.e(TAG, "Build VERSION is less than API 15");
        }

    }

    @Override
    protected void onRestart(){
        super.onRestart();

        askSymptom();
    }
}
