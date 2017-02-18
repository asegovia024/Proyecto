package com.example.antonio.pr1;

import android.Manifest;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Pair;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {


    NfcAdapter nfcAdapter;
    EditText tagContent;
    EditText tagContent2;

    int Accion;
    String Mensaje;
    String Contacto;

    Context context;
    boolean Lacc = false; //comprobamos que la accion se ha leido
    boolean Lcon = false; //comprobamos que el contacto se ha leido

    boolean simReady;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        context = this;

        nfcAdapter = NfcAdapter.getDefaultAdapter(context);
        tagContent = (EditText) findViewById(R.id.tagContent);
        tagContent2 = (EditText) findViewById(R.id.TagContent2);


        // informacion de la simm

        TelephonyManager telephonyManager = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));
        simReady = telephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY;


        //boton rosa que hay que activar para ponerle que sea el de configuracion
        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/


        if (nfcAdapter != null && nfcAdapter.isEnabled()) {

        } else {
            Toast.makeText(this, "Error de Nfc, no disponible", Toast.LENGTH_LONG).show();//cambiar por activacion o por la opcion de activacion del nfc
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        disableForegroundDispachSistem();

    }

    @Override
    protected void onResume() {
        super.onResume();
        enableForegroundDispachSistem();

    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.hasExtra(nfcAdapter.EXTRA_TAG)) {
            Parcelable[] parcelables = intent.getParcelableArrayExtra(nfcAdapter.EXTRA_NDEF_MESSAGES);

            if (parcelables != null && parcelables.length > 0) {
                readtag((NdefMessage) parcelables[0]);
            } else {
                Toast.makeText(this, "No mensaje", Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        // int id = item.getItemId();

        switch (item.getItemId()) {
            case R.id.Config:
                //lanzar configuracion
                Intent config = new Intent(MainActivity.this, ConfigActivity.class);
                startActivity(config);//abrimos el menu de configuracion

                break;

            case R.id.Info:
                // lanzar info
                Intent info = new Intent(MainActivity.this, InfoActivity.class);// nos vamos a infoactivity
                startActivity(info);

                break;
        }

        return super.onOptionsItemSelected(item);
    }


    protected void readtag(NdefMessage ndefMessage) {

        NdefRecord[] ndefRecords = ndefMessage.getRecords();
        if (ndefRecords != null && ndefRecords.length > 0) {
            NdefRecord ndefRecord = ndefRecords[0];

            String texto = getTextFromTag(ndefRecord);

            comprobar(texto);


            if (Accion == 2 && Contacto != null) {

                try {
                    String url = "tel:" + Contacto;

                    // if (url.startsWith("tel:")){

                    //si funciona con action dial
                    // Intent intent = new Intent(Intent.ACTION_DIAL,Uri.parse(url));
                    Intent intent = new Intent(Intent.ACTION_CALL);

                    if (simReady) {
                        // intent.putExtra("com.android.phone.extra.slot", 0); //For sim 1
                        intent.putExtra("simSlot", 0); //For sim 1

                    } else {
                        intent.putExtra("com.android.phone.extra.slot", 1); //For sim 2

                    }
                    intent.setData(Uri.parse(url));
                    // startActivity(intent);


                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    startActivity(intent);


                } catch (Exception e){
                    Toast.makeText(this,"No se ha podido realizar la llamada", Toast.LENGTH_LONG).show();

                }
             //}
            }else if (Accion == 1 && Contacto != null) {
                Mensaje = texto.substring(2);

                SmsManager sms =SmsManager.getDefault();
                sms.sendTextMessage(Contacto,null,Mensaje,null,null);


              /*  Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + Contacto));
                intent.putExtra("sms_body", Mensaje);
                startActivity(intent);*/


            }else if (Accion == 3 && Contacto != null) {
                Toast.makeText(this,"Seccion en desarollo", Toast.LENGTH_LONG).show();

            }


        }else{
            Toast.makeText(this,"Mensaje no encontrado", Toast.LENGTH_LONG).show();
        }

    }



    public String getTextFromTag(NdefRecord ndefRecord){
        String content=null;
        try {
        byte[] payload =ndefRecord.getPayload();
            String textencoding= ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
            int languasesize =payload[0] & 0063;
            content = new String(payload, languasesize + 1,
                    payload.length - languasesize - 1, textencoding);


        } catch (UnsupportedEncodingException e) {
            Log.e("getTextFormTag",e.getMessage(),e);
        }


        return content;
    }




    protected void enableForegroundDispachSistem(){

        Intent  intent = new Intent(this ,MainActivity.class).addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        PendingIntent pendingIntent =PendingIntent.getActivity(this,0,intent,0);
        IntentFilter[] intentFilters = new  IntentFilter[]{};
        nfcAdapter.enableForegroundDispatch(this,pendingIntent,intentFilters,null);

    }
    protected  void disableForegroundDispachSistem(){

        nfcAdapter.disableForegroundDispatch(this);
    }


    //--------------------------------------------Funciones de comprobación -------------------------------------------------------



    protected void comprobar (String cadena){ //comprobacion sobre que tipo de tag estamos leyendo y si es valido
        if (cadena.length() >= 12 &&  isNumeric(cadena) ){
            Lcon =true;
            Contacto= cadena;
             Toast.makeText(this, Contacto+ "Guardado", Toast.LENGTH_LONG).show();

        }else if (cadena.indexOf("#") ==1) {
            Accion= Integer.parseInt(cadena.substring(0, 1));
            Toast.makeText(this,"Accion "+ Accion+ " Guardada", Toast.LENGTH_LONG).show();

            Lacc = true;

        }else{
            Toast.makeText(this,"Mensaje no reconocido", Toast.LENGTH_LONG).show();
        }
    }


    protected   boolean isNumeric(String cadena){ //comprobamos que una cadena puede ser numerico
        if (cadena == null  || cadena.isEmpty()){
            return false;
        }
        int i=0;
        if (cadena.charAt(0)== '-' ){
            if (cadena.length() > 1){
                i++;
            }else{
                return false;
            }
        }
        for(;i < cadena.length(); i++){
            //ingnorar espacios en blanco
            if(cadena.charAt(i)==' '){
            i++;
            }
            if (!Character.isDigit(cadena.charAt(i))  ){
                return false;
            }
        }

        return true;
    }



}
