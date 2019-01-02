package com.fazlagida.burak.smartscaleusb;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;


public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.fazlagida.burak.smartscaleusb.MESSAGE";
    private static final String TAG = "LOG: " ;
    private static int row_index;

    private double previous_cumulative_scale = 0;
    private String buffer = "";
    private Context context = null;

    private HashMap<String, Integer> items_scales_object;
    protected String[] itemList;

    private UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {
        @Override
        public void onReceivedData(byte[] bytes) {
            try {
                String dataUTF8 =  new String( bytes, "UTF-8" );
                buffer += dataUTF8;
                int index;
                while ((index = buffer.indexOf('\n')) != -1) {
                    final String dataStr = buffer.substring(0, index + 1).trim();
                    buffer = buffer.length() == index ? "" : buffer.substring(index + 1);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onSerialDataReceived( dataStr );
                        }
                    });
                }
            } catch (UnsupportedEncodingException e) {
                Log.e( TAG, "Error receiving USB data", e );
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Default calls
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get connected Devices by the serial USB connection
        UsbManager usbManager = getSystemService( UsbManager.class );
        Map<String, UsbDevice> connectedDevices = usbManager.getDeviceList();
        for( UsbDevice device : connectedDevices.values() ) {
            // Log.i( TAG, "Device Found!" + device.getDeviceName() );
            startSerialConnection( usbManager, device );
        }

        // Table Layout
        itemList = new String[]{"elma","armut","kiwi"};
        context = getApplicationContext();
        final TableLayout tableLayout = (TableLayout) findViewById(R.id.items);

        Button saveButton = (Button) findViewById( R.id.save_button );
        saveButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText input = new EditText( context );
                input.setInputType( InputType.TYPE_CLASS_TEXT );

                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setView( input );
                alertDialog.setTitle("Add Item");
                alertDialog.setMessage("Please enter the item name scaled into the text field below");

                // Button Accept
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Create a new table row.
                                TableRow tableRow = new TableRow(context);
                                if ( row_index % 2 == 0 ) {
                                    tableRow.setBackgroundColor( Color.parseColor("#F3F3F3") );
                                } else {
                                    tableRow.setBackgroundColor( Color.parseColor("#FFFFFF" ) );
                                }

                                // Set new table row layout parameters.
                                TableRow.LayoutParams param = new TableRow.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        1
                                );
                                tableRow.setLayoutParams( param );

                                // Add a TextView in the first column.
                                TextView item_name = new TextView( context );
                                item_name.setText( "Product: " + input.getText() );
                                item_name.setLayoutParams( param );
                                item_name.setTextSize( 20 );
                                item_name.setPadding( 10, 0, 10, 0 );
                                tableRow.addView( item_name, 0 );

                                // Add a TextView in the second column.
                                TextView item_scale = new TextView(context);
                                TextView scale = ( TextView ) findViewById( R.id.textWeight );
                                double scale_of_item = Float.parseFloat( scale.getText().toString() ) - previous_cumulative_scale;
                                previous_cumulative_scale = Float.parseFloat( scale.getText().toString() );
                                item_scale.setText( "Scale: " + scale_of_item + " KG" );
                                item_scale.setLayoutParams( param );
                                item_scale.setGravity( Gravity.RIGHT );
                                item_scale.setTextSize( 20 );
                                item_scale.setPadding( 10, 0, 10, 0 );
                                tableRow.addView( item_scale, 1 );

                                tableLayout.addView( tableRow );
                                row_index++;
                            }
                        });

                // Button Cancel
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        });
    }

    private void onSerialDataReceived(String data) {
        // Add whatever you want here
        TextView editText = ( TextView ) findViewById( R.id.textWeight );
        editText.setText( data );
        // Log.i(TAG, "Serial data received: " + data);
    }

    void startSerialConnection(UsbManager usbManager, UsbDevice device) {
        UsbDeviceConnection connection = usbManager.openDevice( device );
        UsbSerialDevice serial = UsbSerialDevice.createUsbSerialDevice( device, connection );
        if (serial != null && serial.open()) {
            serial.setBaudRate(9600);
            serial.setDataBits(UsbSerialInterface.DATA_BITS_8);
            serial.setStopBits(UsbSerialInterface.STOP_BITS_1);
            serial.setParity(UsbSerialInterface.PARITY_NONE);
            serial.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
            serial.read(mCallback);
        }
    }

    /** Called when the user taps the Send button */
    public void sendMessage(View view) {
        Intent intent = new Intent( this, DisplayMessageActivity.class );
        TextView editText = ( TextView ) findViewById( R.id.textWeight );
        String message = editText.getText().toString();
        intent.putExtra( EXTRA_MESSAGE, message );
        startActivity( intent );
    }

}
