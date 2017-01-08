package com.example.antonio.pr1;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class ConfigActivity extends AppCompatActivity {

    Button contacto;
    Button accion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        contacto =(Button)findViewById(R.id.AddCont);

        contacto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cont= new Intent(ConfigActivity.this, ContactosActivity.class);
                startActivity(cont);

            }
        });


        accion =(Button)findViewById(R.id.AddAcc);
        accion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent acc= new Intent(ConfigActivity.this, AccionActivity.class);
                startActivity(acc);
            }
        });




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

}
