package com.example.jtsdesktop.json_listview;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MqttCallback {

    ListView lstview;
    Button settings;
    private static String TAG = "MQTT_android";
    String payload = "the payload";
    static MqttAndroidClient client;
    MqttConnectOptions options = new MqttConnectOptions();
    private ProgressDialog dialog_progress ;
    AlertDialog.Builder builderLoading;
    String topic,server_ip;
    String serverip="cld003.jts-prod.in";
    static String serverpopS,topicpopS;
    Object value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lstview=(ListView)findViewById(R.id.lstid);
        settings=(Button)findViewById(R.id.settings);

        dialog_progress = new ProgressDialog(MainActivity.this);
        builderLoading = new AlertDialog.Builder(MainActivity.this);

        //subscribe_scada();

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settings_popup();
            }
        });


    }
    public void subscribe_scada() {
        Log.d("Enetered ", "in sub func ");
        //Bundle b = getIntent().getExtras();
        dialog_progress.setMessage("connecting ...");
        dialog_progress.show();

        String clientId = MqttClient.generateClientId();
        //topic = "jts/dtd/response";
        //String server_ip = "tcp://jtha.in:1883";
        topic=topicpopS;
        server_ip="tcp://"+serverip+":1883";

       /* if (topicpopS!=null){
            topic=topicpopS;
            server_ip=serverpopS;

        }else {
            topic="jts/RFID/v_0_0_1/Data/2s";
            server_ip = "tcp://"+serverip+":1883";

        }*/

        Log.d("Enetered ", "subscribeScada");
        client = new MqttAndroidClient(this.getApplicationContext(), server_ip,
                clientId);

        Log.d("Enetered ", "subscribeScada1");
        try {
            options.setUserName("esp");
            options.setPassword("ptlesp01".toCharArray());
            IMqttToken token = client.connect(options);
            Log.d("Enetered ", "subscribeScada2");
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    //t.cancel();
                    Log.d("Enetered ", "subscribeScada3");
                    client.setCallback(MainActivity.this);
                    int qos = 2;
                    try {
                        IMqttToken subToken = client.subscribe(topic, qos);
                        Log.d("Enetered ", "subscribeScada4");
                        subToken.setActionCallback(new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                // successfully subscribed
                                //tv.setText("Successfully subscribed to: " + topic);
                                Log.d("success", "came here");
                                dialog_progress.dismiss();
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken,
                                                  Throwable exception) {
                                // The subscription could not be performed, maybe the user was not
                                // authorized to subscribe on the specified topic e.g. using wildcards
                                // Toast.makeText(MainActivity.this, "Couldn't subscribe to: " + topic, Toast.LENGTH_SHORT).show();
                                Log.d("failure", "came here");
                                //tv.setText("Couldn't subscribe to: " + topic);

                            }
                        });
                        Log.d(TAG, "here we are");
                    } catch (MqttException e) {
                        e.printStackTrace();
                        Log.d("error", "!");
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        Log.d("error", "2");
                    }
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "onFailure");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
            Log.d(TAG, "onFailure");
        }
    }
    @Override
    public void connectionLost(Throwable cause) {

    }
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

        Log.d("messege arriv", "login"+message);

        JSONObject json = null;  //your response
        try {
            json = new JSONObject(String.valueOf(message));
        } catch (JSONException e) {
            e.printStackTrace();
        }


        String req = json.getString("req");
        Log.d("login_accesstokenS::", "jsonresponse errcode:::" + req);

        if (req.contentEquals("add_data")) {
            // Toast.makeText(getApplicationContext(), "Response=successfully ", Toast.LENGTH_LONG).show();
            //client.unsubscribe(topic);
            // client.disconnect();
            dialog_progress.dismiss();
            JSONObject object = json.getJSONObject("Data");

            Iterator<String> iter = object.keys();
            ArrayList<Object> list = new ArrayList<Object>();

            while (iter.hasNext()) {

                String key = iter.next();
                Log.d("key", "value:"+key);

                String value1 = key+" :  "+(String) object.get(key);
                list.add(value1);

                Log.d("value", "value:"+value1);

            }


            final ArrayAdapter adapter = new ArrayAdapter(this,
                    R.layout.list_back, R.id.textView , list);

            lstview.setAdapter(adapter);


        } else {
            // Toast.makeText(getApplicationContext(), "Not added the notes", Toast.LENGTH_LONG).show();
            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Reasponse=Failed to login");

            builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface1, int i) {

                    dialogInterface1.dismiss();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }
    public void settings_popup() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(getApplication().LAYOUT_INFLATER_SERVICE);
        View dialogLayout = inflater.inflate(R.layout.popup_back,
                null);
        final AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.setView(dialogLayout, 0, 0, 0, 0);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        WindowManager.LayoutParams wlmp = dialog.getWindow()
                .getAttributes();
        wlmp.gravity = Gravity.CENTER;


      /*  final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //dialog.setTitle("please enter room names");
        dialog.setContentView(R.layout.update_popup_forecast);*/

        Button ok = (Button) dialogLayout.findViewById(R.id.okpopbtn);
        final EditText topicE = (EditText) dialogLayout.findViewById(R.id.topicid);
        final EditText serverE = (EditText) dialogLayout.findViewById(R.id.serverid);

        //textnotesT.setMovementMethod(new ScrollingMovementMethod());
        //textnotesT.setText(notes);

        builder.setView(dialogLayout);
        topicE.setText(topic);
        serverE.setText(serverip);


        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //delgroup(grpnm);
                topicpopS = topicE.getText().toString();
                serverpopS = serverE.getText().toString();
/*
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
*/

                 dialog.dismiss();
                subscribe_scada();


            }
        });

        dialog.show();
    }
}
