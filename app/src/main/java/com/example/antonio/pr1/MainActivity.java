package com.example.antonio.pr1;

import android.app.PendingIntent;
import android.content.ClipData;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        nfcAdapter =NfcAdapter.getDefaultAdapter(this);
        tagContent =(EditText)findViewById(R.id.tagContent);

        //boton rosa que no tiene utilidad por ahora
        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu( menu);
        MenuInflater inflater =getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (item.getItemId()){
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.hasExtra(nfcAdapter.EXTRA_TAG)){

 //a evaluar!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            Parcelable[] parcelables =intent.getParcelableArrayExtra(nfcAdapter.EXTRA_NDEF_MESSAGES);

            if(parcelables!= null && parcelables.length > 0){
                readText((NdefMessage)parcelables[0]);
            }else{
                Toast.makeText(this, "Mensaje no encontrado", Toast.LENGTH_LONG).show();

            }


        }

    }



    public String getTextFromTag(NdefRecord ndefRecord){
        String content=null;
        try {
        byte[] payload =ndefRecord.getPayload();
            String textencoding= ((payload[0] & 128)==0)?"UTF-8":"UTF-16";
            int languasesize =payload[0] & 0063;
            content = new String(payload, languasesize+1,payload.length- languasesize-1,textencoding);



        } catch (UnsupportedEncodingException e) {
            Log.e("getTextFormTag",e.getMessage(),e);
        }


        return content;
    }



    private void readText(NdefMessage ndefmessage ) {
    NdefRecord[] ndefrecords = ndefmessage.getRecords();

        if(ndefrecords!= null && ndefrecords.length > 0){
            NdefRecord ndefrecord = ndefrecords[0];
            String textContent = getTextFromTag(ndefrecord);
            tagContent.setText(textContent);

        }else{
            Toast.makeText(this, "Mensaje grabado no encontrado", Toast.LENGTH_LONG).show();

        }



        }

    @Override
    protected void onPause() {
        super.onPause();

        enalbeFGDS();
    }

    @Override
    protected void onResume() {
        super.onResume();
        disableFGDS();


    }

    protected  void enalbeFGDS(){
        if (nfcAdapter==null){
            nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        }
        Intent intent =new Intent(this,MainActivity.class).addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        PendingIntent pendingIntent =PendingIntent.getActivity(this,0,intent,0);
        IntentFilter[] intentFilters =new IntentFilter[]{};
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters,null);
    }

    protected void disableFGDS(){
        if (nfcAdapter==null){
            nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        }
        nfcAdapter.disableForegroundDispatch(this);}




}
