package com.itensis.tictactoe.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.itensis.tictactoe.R;
import com.itensis.tictactoe.service.BTService;
import com.itensis.tictactoe.util.BTConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class BTClientActivity extends AppCompatActivity {

    private final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    private TextView statusText;
    private ListView devicesList;
    private final List<BluetoothDevice> foundDevices = new ArrayList<>();

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(!foundDevices.contains(device)){
                    foundDevices.add(device);
                    updateFoundDevicesList();
                }
            }
        }
    };


    private final Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(msg.what == BTConstants.MESSAGE_STATE_CHANGE){
                if(msg.arg1 == BTConstants.SERVICE_STATE_CONNECTED){
                    statusText.setText("Connected");
                }
            }
            if(msg.what == BTConstants.MESSAGE_DEVICE_NAME){
                statusText.setText("Connected to: " + msg.getData().getString(BTConstants.DEVICE_NAME));
            }

            if(msg.what == BTConstants.MESSAGE_TOAST){
                Toast.makeText(BTClientActivity.this, msg.getData().getString(BTConstants.TOAST_TEXT), Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_client);

        statusText = findViewById(R.id.clientStatusText);
        devicesList = findViewById(R.id.deviceListView);

        registerDeviceFoundReceiver();
        registerDeviceClicked();

        discoverDevices();
    }

    private void discoverDevices(){
        btAdapter.startDiscovery();
    }


    private void updateFoundDevicesList(){
        List<String> names = foundDevices
                .stream()
                .map(BluetoothDevice::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                BTClientActivity.this,
                android.R.layout.simple_expandable_list_item_1,
                names
        );
        devicesList.setAdapter(adapter);
    }

    private void registerDeviceFoundReceiver(){
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
    }

    private void registerDeviceClicked(){
        devicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice device = foundDevices.get(position);

                BTService btService = new BTService(handler);

                btService.openConnection(device);
            }
        });
    }

}