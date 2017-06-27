package com.example.antonio.pr1;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Locale;

/**
 * Created by antonio on 20/12/16.
 */

public class NFCtools {


    //
    public NdefMessage write(String text, Tag tag,boolean c) throws IOException, FormatException {
        //Creamos un array de elementos NdefRecord. Este Objeto representa un registro del mensaje NDEF
        //Para crear el objeto NdefRecord usamos el método createRecord(String s)

        try{


            if (c) {
                NdefRecord[] records = {createVcardContact("contacto",null,text,null)};
                NdefMessage message = new NdefMessage(records);
                NdefRecord.createApplicationRecord("com.example.antonio.pr1");
                return message;

            }else{
                NdefRecord[] records = {createVcardText(text)};
                NdefMessage message = new NdefMessage(records);
                NdefRecord.createApplicationRecord("com.example.antonio.pr1");

                return message;

            }

            //NdefMessage encapsula un mensaje Ndef(NFC Data Exchange Format). Estos mensajes están
            //compuestos por varios registros encapsulados por la clase NdefRecord

            //Obtenemos una instancia de Ndef del Tag

        }catch (UnsupportedEncodingException e){
            Log.e("create text record",e.getMessage());
            return null;

        }

    }







    public NdefRecord createVcardContact(String name, String org, String tel, String email)
            throws UnsupportedEncodingException {

        String payloadStr = "BEGIN:VCARD" +"\n"+
                "VERSION:2.1" +"\n" +
                "N:;" + name + "\n" +
                "ORG:"+org+"\n"+
                "TEL:"+tel+"\n"+
                "EMAIL:"+email+"\n" +"END:VCARD";
        byte[] uriField = payloadStr.getBytes(Charset.forName("US-ASCII"));
        byte[] payload = new byte[uriField.length + 1];
        System.arraycopy(uriField, 0, payload, 1, uriField.length);
        NdefRecord nfcRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                "text/vcard".getBytes(),
                new byte[0],
                payload);

        return nfcRecord;

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




    public boolean formatTag(Tag tag,NdefMessage ndefMessage){//escribe el mdemessage en el tag


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