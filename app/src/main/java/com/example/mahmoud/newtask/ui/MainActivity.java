package com.example.mahmoud.newtask.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.mahmoud.newtask.R;
import com.example.mahmoud.newtask.adapter.MessageAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import static com.example.mahmoud.newtask.data.MessageContract.CONTENT_URI;
import static com.example.mahmoud.newtask.data.MessageContract.MessageEntry.COLUMN_MESSAGE;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    Button listen, send, listDevices, showMessages;
    ListView listview;
    RecyclerView rvMessages;
    LinearLayoutManager linearLayoutManager;
    TextView msg_box, status;
    EditText writeMsg;
    SendRecive sendRecive;
    MessageAdapter messageAdapter;

    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice[] btArray;
    ArrayList<String> messages;

    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECEIVED = 5;

    int REQUEST_ENABLE_BLUETOOTH = 1;

    private static final String APP_NAME = "BTChat";
    private static final UUID MY_UUID = UUID.fromString("899ffc5a-0ea9-11e9-ab14-d663bd873d93");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get inst
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        findViewByIds();


        if (!bluetoothAdapter.isEnabled()) {

            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
        }


        implementListener();

    }

    private void implementListener() {

        listDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Set<BluetoothDevice> bt = bluetoothAdapter.getBondedDevices();
                String[] strings = new String[bt.size()];
                btArray = new BluetoothDevice[bt.size()];
                int index = 0;

                try {

                    if (bt.size() > 0) {
                        for (BluetoothDevice device : bt) {
                            if (!device.getName().equalsIgnoreCase(null)) {
                                btArray[index] = device;
                                strings[index] = device.getName();
                                index++;
                            }
                        }

                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, strings);
                        listview.setAdapter(arrayAdapter);

                    }

                } catch (Exception e) {

                }


            }
        });


        listen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServerClass serverClass = new ServerClass();
                serverClass.start();
            }
        });


        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                ClientClass clientClass = new ClientClass(btArray[position]);
                clientClass.start();
                status.setText("Connecting");
            }
        });


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String string = String.valueOf(writeMsg.getText());

                sendRecive.write(string.getBytes());


            }
        });



    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Log.e("ssss", "msg" + msg.toString() + "");
            switch (msg.what) {
                case STATE_LISTENING:
                    status.setText("Listening");
                    break;
                case STATE_CONNECTING:
                    status.setText("CONNECTING");
                    break;
                case STATE_CONNECTED:
                    status.setText("CONNECTED");
                    break;
                case STATE_CONNECTION_FAILED:
                    status.setText("CONNECTION FAILED");
                    break;
                case STATE_MESSAGE_RECEIVED:
                    // we will write later

                    String obj = (String) msg.obj;
                    Log.e("ssss", "readBuffer " + obj + "");
                    String tempMsg = obj;
                    msg_box.setText(tempMsg);


                    // to insert in database
                    insert(tempMsg);


                    break;
            }
            return true;
        }
    });

    private void insert(String tempMsg) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_MESSAGE, tempMsg);
        getContentResolver().insert(CONTENT_URI, values);
        Log.e(TAG, "insert: " + tempMsg);
        getMessages();
    }

    private void getMessages() {
        messages = new ArrayList<String>();
        Cursor cursor = getContentResolver().query(CONTENT_URI,
                null,
                null,
                null,
                null);

        messageAdapter = new MessageAdapter(getApplicationContext(), cursor);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        rvMessages.setLayoutManager(linearLayoutManager);
        rvMessages.setAdapter(messageAdapter);


//        cursor.moveToFirst();
//        for (int position = 0; position < cursor.getCount(); position++) {
//            if (!cursor.moveToPosition(position))
//                return;
//
//            String tmpMsg = cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE));
//            messages.add(tmpMsg);
//            Log.e(TAG, "Cursor" + tmpMsg);
//        }
//
//
//        arrayAdapterMessages= new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, messages);
//        listview.setAdapter(arrayAdapterMessages);


    }

    private void findViewByIds() {

        listDevices = findViewById(R.id.btn_list_device);
        listen = findViewById(R.id.btn_listen);
        send = findViewById(R.id.btn_send);
        listview = findViewById(R.id.list);
        rvMessages = findViewById(R.id.rv_messages);
        writeMsg = findViewById(R.id.write_msg);
        status = findViewById(R.id.tv_status);
        msg_box = findViewById(R.id.tv_message);
    }


    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class ServerClass extends Thread {
        // The local server socket
        private BluetoothServerSocket serverSocket;

        public ServerClass() {
            BluetoothServerSocket tmp = null;

            try {
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID);
                Log.d(TAG, "AcceptThread: Setting up Server using: " + MY_UUID);

            } catch (IOException e) {
                e.printStackTrace();
            }
            serverSocket = tmp;
        }


        public void run() {
            Log.d(TAG, "run: AcceptThread Running.");
            BluetoothSocket socket = null;
            while (socket == null) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    Log.d(TAG, "run: RFCOM server socket start.....");

                    Message message = Message.obtain();
                    message.what = STATE_CONNECTING;
                    handler.sendMessage(message);
                    socket = serverSocket.accept();
                    Log.d(TAG, "run: RFCOM server socket accepted connection.");
                } catch (IOException e) {
                    Log.e(TAG, "AcceptThread: IOException: " + e.getMessage());
                    e.printStackTrace();
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);
                }

                if (socket != null) {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTED;
                    handler.sendMessage(message);

                    // write something for wite and recieve

                    sendRecive = new SendRecive(socket);
                    sendRecive.start();
                    break;
                }
                Log.i(TAG, "END mAcceptThread ");

            }
        }


    }


    private class ClientClass extends Thread {

        private BluetoothDevice device;
        private BluetoothSocket socket;


        public ClientClass(BluetoothDevice device1) {
            device = device1;
            BluetoothSocket tmp = null;
            try {
                Log.d(TAG, "ConnectThread: Trying to create InsecureRfcommSocket using UUID: "
                        + MY_UUID);
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "ConnectThread: Could not create InsecureRfcommSocket " + e.getMessage());
            }

            socket = tmp;
        }


        public void run() {
            try {
                Log.d(TAG, "run: ConnectThread connected.");
                socket.connect();
                Message message = Message.obtain();
                message.what = STATE_CONNECTED;
                handler.sendMessage(message);

                sendRecive = new SendRecive(socket);
                sendRecive.start();

            } catch (IOException e) {
                e.printStackTrace();
                Message message = Message.obtain();
                message.what = STATE_CONNECTION_FAILED;
                handler.sendMessage(message);

            }
        }


    }


    private class SendRecive extends Thread {

        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public SendRecive(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting.");

            bluetoothSocket = socket;
            InputStream tempIn = null;
            OutputStream tempOut = null;

            try {
                tempIn = socket.getInputStream();
                tempOut = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream = tempIn;
            outputStream = tempOut;
        }


        public void run() {
            byte[] buffer = new byte[1024]; // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                // Read from the InputStream
                try {
                    // Read from the InputStream.
                    bytes = inputStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "InputStream: " + incomingMessage);
                    // Send the obtained bytes to the UI activity.
                    handler.obtainMessage(STATE_MESSAGE_RECEIVED, incomingMessage).sendToTarget();
//                    handler.obtainMessage(STATE_MESSAGE_RECEIVED, bytes).sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "write: Error reading Input Stream. " + e.getMessage());
                    break;
                }


            }
        }

        //Call this from the main activity to send data to the remote device
        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to outputstream: " + text);

            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "write: Error writing to output stream. " + e.getMessage());
            }
        }


    }


}
