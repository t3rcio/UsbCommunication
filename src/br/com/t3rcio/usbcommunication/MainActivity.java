package br.com.t3rcio.usbcommunication;

import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import java.util.HashMap;
import java.util.List;
import java.io.IOException;

public class MainActivity extends Activity {

    private int ThreadTimeOut = 3000;

    private String TAG = "UsbCommunication: ";
    private HashMap<String, UsbDevice> usbDevices;
    private int TIMEOUT = 500;
    public byte[] buffer = new byte[4096];
    private UsbManager usbManager;
    public EditText dispositivos;
    private Button button;
    private UsbDevice device = null;
    Thread thread = null;
    
    private enum Operation {
        READ, WRITE;
    }
    

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        String text = "";

        dispositivos = (EditText) findViewById(br.com.t3rcio.usbcommunication.R.id.Text);
        dispositivos.canScrollVertically(1);
        button = (Button) findViewById(br.com.t3rcio.usbcommunication.R.id.SendButton);
        
        //Find all drivers
        this.usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        Log.v(this.TAG, "UsbManager criado...");
        dispositivos.setText("Texto a enviar para a placa");        
        
        button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                     //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                //MainActivity.this.operation(Operation.WRITE);
                MainActivity.this.write(Operation.WRITE);
            }
        });
        
        operation(Operation.READ);
    }
    
    public synchronized void operation(Operation operation){
        byte [] data;
        List<UsbSerialDriver> drivers = UsbSerialProber.getDefaultProber().findAllDrivers(this.usbManager);
        if (drivers.isEmpty()) {
            Log.v(TAG,"[ERROR] Não há drivers disponíveis");
            return;
        }
        //Open a connection
        UsbSerialDriver driver = drivers.get(0);
        final UsbDeviceConnection connection = this.usbManager.openDevice(driver.getDevice());

        if (connection == null) {
            Log.v(TAG,"[ERROR] Não há conexão disponível");
            return;
        }

        final UsbSerialPort sPort = driver.getPorts().get(0);
        try{
            sPort.open(connection);
            sPort.setParameters(57600, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            switch(operation){
                case READ:                
                    data = new byte[32];
                    char[] result = new char[32];
                    int bytes_lidos = 0;    
                    int counter = 0;
                    while(counter <= 9){
                        bytes_lidos = sPort.read(data, this.TIMEOUT);                    
                        for (int i = 0; i < data.length; i++) {
                            result[i] = (char) data[i];
                        }
                        if (bytes_lidos != 0) {                        
                            Log.v(TAG, "Bytes lidos: " + String.valueOf(bytes_lidos));
                            Log.v(TAG, "Dados recebidos: " + String.copyValueOf(result));
                        }
                        else{
                            sPort.close();
                            break;
                        }
                        counter++;
                    }                             
                    break;
                case WRITE:
                    data = this.dispositivos.getText().toString().getBytes();
                    int bytes_escritos = sPort.write(data, this.TIMEOUT);
                    if(bytes_escritos > 0){
                        Log.v(TAG,"Bytes escritos: "+bytes_escritos);
                        operation(MainActivity.Operation.READ);
                    }                    
                    else{
                        Log.v(TAG, "[ERROR] Problemas ao escrever os dados.");
                    }
                    sPort.close();
                    break;
                default:
                    break;
            }
        }
        catch(IOException e){
            Log.v(TAG,e.getMessage());
        }
    }
    
    public synchronized void write(Operation operation){
        byte [] data;
        List<UsbSerialDriver> drivers = UsbSerialProber.getDefaultProber().findAllDrivers(this.usbManager);
        if (drivers.isEmpty()) {
            Log.v(TAG,"[ERROR] Não há drivers disponíveis");
            return;
        }
        //Open a connection
        UsbSerialDriver driver = drivers.get(0);
        final UsbDeviceConnection connection = this.usbManager.openDevice(driver.getDevice());

        if (connection == null) {
            Log.v(TAG,"[ERROR] Não há conexão disponível");
            return;
        }

        final UsbSerialPort sPort = driver.getPorts().get(0);
        try{
            sPort.open(connection);
            sPort.setParameters(57600, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            switch(operation){
                case WRITE:
                    data = this.dispositivos.getText().toString().getBytes();
                    int bytes_escritos = sPort.write(data, this.TIMEOUT);
                    if(bytes_escritos > 0){
                        Log.v(TAG,"Bytes escritos: "+bytes_escritos);                        
                    }                    
                    else{
                        Log.v(TAG, "[ERROR] Problemas ao escrever os dados.");
                    }
                    sPort.close();
                    break;
                default:
                    break;
            }
        }
        catch(IOException e){
            Log.v(TAG,e.getMessage());
        }
    }   
    
}
