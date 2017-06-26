package com.example.antonio.pr1;

import android.Manifest;
import android.app.Activity;
import android.net.Uri;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Locale;

import static android.R.attr.button;
import static android.R.attr.track;

public class AccionActivity extends AppCompatActivity implements View.OnClickListener{

    Button sms, call, w;
    EditText ttexto;

    NFCtools n;

    String op, text;

    Context context;
    NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accion);
        context=this;



        nfcAdapter =NfcAdapter.getDefaultAdapter(context);


        if (nfcAdapter!=null && nfcAdapter.isEnabled()){

        }else{
            Toast.makeText(this, "Error de Nfc, no disponible", Toast.LENGTH_LONG).show();//cambiar por activacion o por la opcion de activacion del nfc
        }



        sms =(Button)findViewById(R.id.sms);
        sms.setOnClickListener(this);

        call =(Button)findViewById(R.id.call);
        call.setOnClickListener(this);

      /*  w =(Button)findViewById(R.id.wapp);
        w.setOnClickListener(this);*/

        ttexto =(EditText)findViewById(R.id.editText);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //boton atras



    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if(intent.hasExtra(NfcAdapter.EXTRA_TAG)) {
            Toast.makeText(this, "intent nfc", Toast.LENGTH_LONG).show();

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            NFCtools n = new NFCtools();


            String nn;
            text= String.valueOf(ttexto.getText());
            nn=op +"#"+ text ;


            NdefMessage ndefMessage = null;


            try {
             //   ndefMessage = n.write(nn, tag, false);
                ndefMessage=write(nn,tag,false);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (FormatException e) {
                e.printStackTrace();
            }



            if(n.WriteNdef(tag, ndefMessage)){
                Toast.makeText(this, "Mensaje escrito", Toast.LENGTH_LONG).show();

            }else{
                Toast.makeText(this, "el tag es nulo o imposible de escribir", Toast.LENGTH_LONG).show();
            }


        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: //hago un case por si en un futuro agrego mas opciones
                //Log.i("ActionBar", "Atrás!");
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.sms:
                op="1";
                // do your code
                break;

            case R.id.call:
                op="2";
                // do your code
                break;

          /*  case R.id.wapp:
                op="3";
                // do your code
                break;*/


        }

    }



    @Override
    protected void onResume() {

        super.onResume();
       // enableForegroundDispachSistem();// candando rojo
        enalbeFGDS();//alternativa

    }

    @Override
    protected void onPause() {
        super.onPause();
       // disableForegroundDispachSystem();
        disableFGDS();

    }

    protected  void enalbeFGDS(){
        if (nfcAdapter==null){
            nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        }
        Intent intent =new Intent(this,AccionActivity.class).addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        PendingIntent pendingIntent =PendingIntent.getActivity(this,0,intent,0);
        IntentFilter[] intentFilters =new IntentFilter[]{};
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters,null);
    }

    protected void disableFGDS(){
        if (nfcAdapter==null){
            nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        }
        nfcAdapter.disableForegroundDispatch(this);}










    //------------------------------------------------0---------------------------------------------




    public NdefMessage write(String text, Tag tag,boolean c) throws IOException, FormatException {
        //Creamos un array de elementos NdefRecord. Este Objeto representa un registro del mensaje NDEF
        //Para crear el objeto NdefRecord usamos el método createRecord(String s)

        try{


           /* if (c) {
                NdefRecord[] records = {createVcardContact("contacto",null,text,null)};
                NdefMessage message = new NdefMessage(records);
                return message;

            }else{*/
                NdefRecord[] records = {createVcardText(text)};
                NdefMessage message = new NdefMessage(records);
                return message;

           // }

            //NdefMessage encapsula un mensaje Ndef(NFC Data Exchange Format). Estos mensajes están
            //compuestos por varios registros encapsulados por la clase NdefRecord

            //Obtenemos una instancia de Ndef del Tag

        }catch (UnsupportedEncodingException e){
            Log.e("create text record",e.getMessage());
            return null;

        }

    }










    public NdefRecord createVcardText(String text)
            throws UnsupportedEncodingException {

        byte[] uriField = text.getBytes(Charset.forName("US-ASCII"));
        byte[] payload = new byte[uriField.length + 1];
        System.arraycopy(uriField, 0, payload, 1, uriField.length);
        NdefRecord nfcRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                "text/vcard".getBytes(),
                new byte[0],
                payload);


        return nfcRecord;

    }




    private boolean formatTag(Tag tag,NdefMessage ndefMessage){//escribe el mdemessage en el tag


        try{
            NdefFormatable ndefFormatable = NdefFormatable.get(tag);


            if(ndefFormatable== null) {
                //Toast.makeText(this, "tag no formateable", Toast.LENGTH_LONG).show();
                return false; //tag no formateable
            }
            ndefFormatable.connect();
            ndefFormatable.format(ndefMessage);
            ndefFormatable.close();


        }catch (Exception e){
            Log.e("formatTag", e.getMessage());

        }
        return true; //mensaje escrito
        //Toast.makeText(this, "Mensaje escrito", Toast.LENGTH_LONG).show();

    }



    public boolean WriteNdef(Tag tag, NdefMessage ndefMessage){
        try{
            if (tag== null){
                //Toast.makeText(this, "el tag es nulo", Toast.LENGTH_LONG).show();

                return false;
            }
            Ndef ndef =Ndef.get(tag);
            if(ndef == null){
             //   n.formatTag(tag,ndefMessage);
                formatTag(tag, ndefMessage);
            }else {
                ndef.connect();
                if(!ndef.isWritable()){
                    // Toast.makeText(this, "Imposible de escribir", Toast.LENGTH_LONG).show();
                    ndef.close();
                    return false;
                }
                ndef.writeNdefMessage(ndefMessage); //se llama a si mismo ?¿?¿?
                ndef.close();
                // Toast.makeText(this, "Mensaje escrito", Toast.LENGTH_LONG).show();


            }

        }catch (Exception e){
            Log.e("WriteNdefMessage", e.getMessage());

        }
        return true;

    }


















}
