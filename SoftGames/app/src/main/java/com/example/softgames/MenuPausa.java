package com.example.softgames;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class MenuPausa extends AppCompatActivity {
    List<View.OnClickListener> acciones;
    public MenuPausa(List<View.OnClickListener> acciones){
        this.acciones = acciones;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pause_layout);

        Button reanudar = findViewById(R.id.btnUno);
        Button salir = findViewById(R.id.btnDos);

        reanudar.setOnClickListener(acciones.get(0));
        salir.setOnClickListener(acciones.get(1));
    }
}
