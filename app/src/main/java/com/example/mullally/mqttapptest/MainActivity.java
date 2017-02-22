package com.example.mullally.mqttapptest;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;


public class MainActivity extends AppCompatActivity {

    static String MQTTHOST = "tcp://m20.cloudmqtt.com:19742";
    static String USERNAME = "conor";
    static String PASSWORD = "test";
    String topicStr ="dev/test";
    String message ="";
    boolean connected = false;


    MqttConnectOptions options;
    MqttAndroidClient client;
    TextView subText;
    Vibrator vibrator;
    Ringtone myRingtone;
    EditText etMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        subText =(TextView)findViewById(R.id.subText);
        vibrator=(Vibrator)getSystemService(VIBRATOR_SERVICE);
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        myRingtone = RingtoneManager.getRingtone(getApplicationContext(),uri);
        etMessage = (EditText)findViewById(R.id.etMessage);


        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), MQTTHOST, clientId);
        options = new MqttConnectOptions();
        options.setUserName(USERNAME);
        options.setPassword(PASSWORD.toCharArray());

        /*try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Toast.makeText(MainActivity.this,"connected",Toast.LENGTH_LONG).show();
                    Subscribe();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Toast.makeText(MainActivity.this,"connection failed",Toast.LENGTH_LONG).show();

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }*/

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                subText.setText(new String(message.getPayload()));
                vibrator.vibrate(500);
                myRingtone.play();
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

    public void Publish(View v){
        if(connected==true) {
            String topic = topicStr;
            message = etMessage.getText().toString();
            try {

                client.publish(topic, message.getBytes(), 0, false);
                etMessage.setText("");
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        else{
            Toast.makeText(MainActivity.this,"Unable to publish, try connecting",Toast.LENGTH_LONG).show();
        }
    }

    private void Subscribe(){
        try{
            client.subscribe(topicStr,0);
        }
        catch (MqttException e) {
                e.printStackTrace();
        }
    }

    public void Connect(View v){
        if(connected==false){
        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Toast.makeText(MainActivity.this,"connected",Toast.LENGTH_LONG).show();
                    Subscribe();
                    connected = true;

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Toast.makeText(MainActivity.this,"connection failed",Toast.LENGTH_LONG).show();

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
      }
        else{
            Toast.makeText(MainActivity.this,"already connected",Toast.LENGTH_LONG).show();
        }
    }

    public void Disconnect(View v) {
        if (connected == true) {
            try {
                IMqttToken token = client.disconnect();

                token.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        // We are connected
                        Toast.makeText(MainActivity.this, "disconnected", Toast.LENGTH_LONG).show();
                        connected = false;
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        // Something went wrong e.g. connection timeout or firewall problems
                        Toast.makeText(MainActivity.this, "disconnect failed", Toast.LENGTH_LONG).show();

                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        else{
            Toast.makeText(MainActivity.this,"already disconnected",Toast.LENGTH_LONG).show();
        }
    }
}
