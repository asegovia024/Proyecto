package com.example.antonio.pr1;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class ContactosActivity extends AppCompatActivity implements View.OnClickListener {

    Button agenda;
    Button escribircontacto;
    EditText numero;
    int PICK_CONTACT;

    Context context;
    NfcAdapter nfcAdapter;


    NFCtools n;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contactos);
        context= this;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//atras

         nfcAdapter = NfcAdapter.getDefaultAdapter(this);// adaptador nfc

        if (nfcAdapter!=null && nfcAdapter.isEnabled()){

        }else{
            Toast.makeText(this, "Error de Nfc, no disponible", Toast.LENGTH_LONG).show();//cambiar por activacion o por la opcion de activacion del nfc
        }


       /* escribircontacto = (Button) findViewById(R.id.escribircontacto);
        escribircontacto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


            }
        });*/



        agenda = (Button) findViewById(R.id.agenda);
        agenda.setOnClickListener(this);

        numero = (EditText) findViewById(R.id.numero);
        // numero.setOnClickListener(this);


        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 1); // permiso para leer los contactos



    }



    @Override
    protected void  onNewIntent(Intent intent){
        super.onNewIntent(intent);


        if(intent.hasExtra(NfcAdapter.EXTRA_TAG)) {
            Toast.makeText(this, "Escribiendo...", Toast.LENGTH_LONG).show();

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            String nn;
            nn= String.valueOf(numero.getText());


            NdefMessage ndefMessage = null;
            try {
                ndefMessage = write(nn,tag);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (FormatException e) {
                e.printStackTrace();
            }
            WriteNdef2(tag,ndefMessage);



        }

    }



    @Override
    protected void onResume() {

        super.onResume();
        enableForegroundDispachSistem();

    }

    @Override
    protected void onPause() {

        super.onPause();
        disableForegroundDispachSistem();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: //hago un case por si en un futuro agrego mas opciones
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.agenda:


                Animation zoom2;
                zoom2= AnimationUtils.loadAnimation(this, R.animator.animacion);
                agenda.startAnimation(zoom2);

                //reqCode=1;//codificamos con un 1 para el caso de el acceso de la agenda
                Intent intenta = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);

                startActivityForResult(intenta, PICK_CONTACT); //abre los contactos
                break;

          /*  case R.id.escribircontacto://no entra
                //debugear por aqui
                Toast.makeText(this, "añadir pulsado " , Toast.LENGTH_LONG).show();

                //abrir otra pantalla (?)

                break;*/

        }


    }


    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode) {
            case 0:
                if (resultCode == Activity.RESULT_OK) {
                    Uri contactData = data.getData();
                    Cursor c = managedQuery(contactData, null, null, null, null);
                    if (c.moveToFirst()) {
                        String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

                        String hasPhone =
                                c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                        if (hasPhone.equalsIgnoreCase("1")) {
                            Cursor phones = getContentResolver().query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                                    null, null);
                            phones.moveToFirst();
                            String phn_no = phones.getString(phones.getColumnIndex("data1"));
                            String name = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.DISPLAY_NAME));
                            Toast.makeText(this, "contact info : " + phn_no + "\n" + name, Toast.LENGTH_LONG).show();

                            numero.setText(phn_no);
                        }
                    }
                }
                break;

        }

    }


    @Nullable
    private NdefMessage write(String text, Tag tag) throws IOException, FormatException {
        //Creamos un array de elementos NdefRecord. Este Objeto representa un registro del mensaje NDEF
        //Para crear el objeto NdefRecord usamos el método createRecord(String s)

        try{


        NdefRecord[] records = {createVcardRecord("contacto",null,text,null)};

        //NdefMessage encapsula un mensaje Ndef(NFC Data Exchange Format). Estos mensajes están
        //compuestos por varios registros encapsulados por la clase NdefRecord
        NdefMessage message = new NdefMessage(records);



        //Obtenemos una instancia de Ndef del Tag
        /*
        Ndef ndef = Ndef.get(tag);
        ndef.connect();
        ndef.writeNdefMessage(message);
        ndef.close();
        */
        return message;
        }catch (UnsupportedEncodingException e){
            Log.e("create text record",e.getMessage());
            return null;

        }


    }

    public NdefRecord createVcardRecord(String name, String org, String tel, String email)
            throws UnsupportedEncodingException {

      /*  String payloadStr = "BEGIN:VCARD" +"\n"+
                "VERSION:2.1" +"\n" +
                "N:;" + name + "\n" +
                "ORG:"+org+"\n"+
                "TEL:"+tel+"\n"+
                "EMAIL:"+email+"\n" +"END:VCARD";*/
        String payloadStr = tel;
        byte[] uriField = payloadStr.getBytes(Charset.forName("US-ASCII"));
        byte[] payload = new byte[uriField.length + 1];
        System.arraycopy(uriField, 0, payload, 1, uriField.length);
        NdefRecord nfcRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                "text/vcard".getBytes(),
                new byte[0],
                payload);

        return nfcRecord;
    }

    private void formatTag(Tag tag,NdefMessage ndefMessage){//escribe el mdemessage en el tag


        try{
            NdefFormatable ndefFormatable = NdefFormatable.get(tag);


            if(ndefFormatable== null) {
                Toast.makeText(this, "tag no formateable", Toast.LENGTH_LONG).show();
                return;
            }
            ndefFormatable.connect();
            ndefFormatable.format(ndefMessage);
            ndefFormatable.close();
            Toast.makeText(this, "Mensaje escrito", Toast.LENGTH_LONG).show();


        }catch (Exception e){
            Log.e("formatTag", e.getMessage());

        }


    }


    private void WriteNdef2(Tag tag, NdefMessage ndefMessage){
        try{
            if (tag== null){
                Toast.makeText(this, "el tag es nulo", Toast.LENGTH_LONG).show();

                return;
            }
            Ndef ndef =Ndef.get(tag);
            if(ndef == null){
                   n.formatTag(tag,ndefMessage);
                //formatTag(tag, ndefMessage);
            }else {
                ndef.connect();
                if(!ndef.isWritable()){
                    Toast.makeText(this, "Imposible de escribir", Toast.LENGTH_LONG).show();
                    ndef.close();
                    return;
                }
                ndef.writeNdefMessage(ndefMessage);
                ndef.close();
                Toast.makeText(this, "Mensaje escrito", Toast.LENGTH_LONG).show();


            }

        }catch (Exception e){
            Log.e("WriteNdefMessage", e.getMessage());

        }
    }




    private void enableForegroundDispachSistem (){
        Intent intent = new Intent(this,ContactosActivity.class).addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        PendingIntent pendingIntent =  PendingIntent.getActivity(this, 0 ,intent,0);
        IntentFilter[] intentFilters =new IntentFilter[]{};
        nfcAdapter.enableForegroundDispatch(this, pendingIntent,intentFilters,null);


    }


    private void disableForegroundDispachSistem (){
        nfcAdapter.disableForegroundDispatch(this);

    }





}
