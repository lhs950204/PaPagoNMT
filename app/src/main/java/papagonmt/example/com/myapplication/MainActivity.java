package papagonmt.example.com.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    String clientId = "I5F_wto1DketaXOgZ7EK";//애플리케이션 클라이언트 아이디값";
    String clientSecret = "ftoG43wt2m";//애플리케이션 클라이언트 시크릿값";

    Button translate_Button,Speechinsert_Button;
    TextView result_TextView;
    EditText inputText;
    String resultText = "";

    //SpeechRecognizer
    Intent intent;
    SpeechRecognizer mRecognizer;
    private final int MY_PERMISSIONS_RECORD_AUDIO = 1;
    //==================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputText = (EditText) findViewById(R.id.inputText_Edit);
        translate_Button = (Button) findViewById(R.id.translate_Button);
        Speechinsert_Button = (Button) findViewById(R.id.Speechinsert_Button);
        result_TextView = (TextView) findViewById(R.id.result_TextView);


        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_RECORD_AUDIO
                );
            }
        }

        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");

        mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mRecognizer.setRecognitionListener(recognitionListener);

        Speechinsert_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecognizer.startListening(intent);
            }
        });

    }

    private RecognitionListener recognitionListener = new RecognitionListener() {

        // 음성인식 준비(음성인식 시작이 가능할때 호출)
        @Override
        public void onReadyForSpeech(Bundle bundle) {
            Log.e("onReadyForSpeech","onReadyForSpeech");
        }

        // 음성인식 시작(처음 시작할때 호출)
        @Override
        public void onBeginningOfSpeech() {
            Log.e("onBeginningOfSpeech","onBeginningOfSpeech");
        }

        // 음성인식 진행중(파라미터는 입력한 목소리의 데시벨을 의미)
        @Override
        public void onRmsChanged(float v) {
            Log.e("onRmsChanged","onRmsChanged");

        }

        //언제 호출되는지 정확하게 확인 불가
        @Override
        public void onBufferReceived(byte[] bytes) {
            Log.e("onBufferReceived","onBufferReceived");
        }


        //음성인식 종료 시점(중간에 말하다 멈추면 끝난거로 인식)
        @Override
        public void onEndOfSpeech() {
            Log.e("onEndOfSpeech","onEndOfSpeech");
        }

        @Override
        public void onError(int i) {
//            inputText.setText("너무 늦게 말하면 오류뜹니다");
//            Log.e("ReadyForSpeech","ReadyForSpeech");
        }

        @Override
        public void onResults(Bundle bundle) {
            String key = "";
            key = SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResult = bundle.getStringArrayList(key);

            String[] rs = new String[mResult.size()];
            mResult.toArray(rs);

            inputText.setText(rs[0]);

            TranslateTask translateTask = new TranslateTask();
            translateTask.execute(inputText.getText().toString());
        }

        @Override
        public void onPartialResults(Bundle bundle) {
            Log.e("onPartialResults","onPartialResults");

        }

        @Override
        public void onEvent(int i, Bundle bundle) {

        }
    };

    class TranslateTask extends AsyncTask<String, Void, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String sourceText = params[0];
            try {
                String text = URLEncoder.encode(sourceText, "UTF-8");
                String apiURL = "https://openapi.naver.com/v1/papago/n2mt";
                URL url = new URL(apiURL);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("X-Naver-Client-Id", clientId);
                con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
                // post request
                String postParams = "source=ko&target=en&text=" + text;
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(postParams);
                wr.flush();
                wr.close();
                int responseCode = con.getResponseCode();
                BufferedReader br;
                if (responseCode == 200) { // 정상 호출
                    br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                } else {  // 에러 발생
                    br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                }
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }
                br.close();
                System.out.println(response.toString());
                return response.toString();
            } catch (Exception e) {
                System.out.println(e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
//            Log.e("result", s.toString());
            Gson gson = new GsonBuilder().create();
            JsonParser parser = new JsonParser();
            JsonElement rootObj = parser.parse(s.toString())
//원하는 데이터 까지 찾아 들어간다.
                    .getAsJsonObject().get("message")
                    .getAsJsonObject().get("result");
//안드로이드 객체에 담기
            TranslatedItem items = gson.fromJson(rootObj.toString(), TranslatedItem.class);
            resultText = items.getTranslatedText();
//Log.d("result", resultText);
//번역결과를 텍스트뷰에 넣는다.
//            Log.e("dddd", "텍스트 설정");
            result_TextView.setText(items.getTranslatedText());
//            Log.e("dddd", "텍스트 설정2");

        }
    }

    public class TranslatedItem {
        String translatedText;

        public String getTranslatedText() {
            return translatedText;
        }
    }


}
