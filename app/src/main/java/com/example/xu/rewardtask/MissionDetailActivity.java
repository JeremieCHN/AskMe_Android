package com.example.xu.rewardtask;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class MissionDetailActivity extends AppCompatActivity {
    private String TAG = "MissionDetailActivity";
    private String missionPublisher;
    private String missionName;
    private TextView PublisherTimeTV;
    private TextView tip;
    private ListView commentLV;
    private EditText commentEdit;
    private TextView IsCompletedTip;
    private TextView moneyTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission_detail);

        initView();
        new getMissionDetailAsyncTask().execute();
    }

    void initView() {
        Intent intent = getIntent();
        if (intent.getStringExtra("Date") != null) {
            Date date = new Date(intent.getStringExtra("Date"));
            DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm");
            PublisherTimeTV.setText(missionPublisher + "发布于" + dateFormat.format(date));
        }
    }
    class getMissionDetailAsyncTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            HttpURLConnection connection = null;

            try {
                URL url = new URL("http://" + CurrentUser.IP + "/AndroidServer/missionServlet?mission=" +
                        URLEncoder.encode(missionName, "UTF-8") + "&publisher=" +
                        URLEncoder.encode(missionPublisher, "UTF-8"));

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                StringBuffer buffer = new StringBuffer();
                InputStream is = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                reader.close();

                JSONObject jsonObject = new JSONObject(buffer.toString());
                if (jsonObject.getString("Status").equals("Fail")) {
                    return "Fail";
                }

                return buffer.toString();

            } catch (UnknownHostException e) {
                e.printStackTrace();
                return "InternetGG";
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                return "InternetGG";
            } catch (ConnectException e) {
                e.printStackTrace();
                return "InternetGG";
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG, e.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null)
                    connection.disconnect();
            }
            return "Fail";
        }

        @Override
        protected void onPostExecute(String s) {
            if (s.equals("Fail")) {
                Toast.makeText(MissionDetailActivity.this, "获取失败，请稍后再试", Toast.LENGTH_SHORT).show();
            } else if (s.equals("InternetGG")) {
                Toast.makeText(MissionDetailActivity.this, "网络错误，请稍后再试", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    Log.e(TAG, s);
                    JSONObject jsonObject = new JSONObject(s);

                    String dateStr = jsonObject.getString("Date");
                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = format.parse(dateStr.substring(0, 19));

                    DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm");
                    PublisherTimeTV.setText(missionPublisher + "发布于" + dateFormat.format(date));

                    TextView contentTV = (TextView) findViewById(R.id.MissionDetail_Content);
                    contentTV.setText(URLDecoder.decode(jsonObject.getString("Content"), "UTF-8"));

                    moneyTV = (TextView) findViewById(R.id.MissionDetail_Money);
                    moneyTV.setText(jsonObject.getString("Gold"));

                    if (jsonObject.getString("IsComplete").equals(Mission.COMPLETED)) {
                        IsCompletedTip.setText("此任务的赏金已经被领取");
                    } else {
                        IsCompletedTip.setText("此任务的赏金尚未被领取");
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide2right);
    }
}
