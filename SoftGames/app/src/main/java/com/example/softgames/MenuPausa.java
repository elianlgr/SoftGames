package com.example.softgames;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.List;

public class MenuPausa extends Dialog {
    private List<View.OnClickListener> acciones;

    public MenuPausa(Context context, List<View.OnClickListener> acciones) {
        super(context);
        this.acciones = acciones;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pause_layout);

        // Hace que el diálogo tenga el tamaño correcto o sea transparente si se desea
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT);

        Button reanudar = findViewById(R.id.btnUno);
        Button ajustes = findViewById(R.id.btnAjustes);
        Button salir = findViewById(R.id.btnDos);

        reanudar.setOnClickListener(v -> {
            if (acciones != null && acciones.size() > 0) {
                acciones.get(0).onClick(v);
            }
            dismiss();
        });

        ajustes.setOnClickListener(v -> {
            if (acciones != null && acciones.size() > 2) {
                acciones.get(2).onClick(v);
            }
            // No hacemos dismiss() aquí para que cuando vuelvan de Ajustes sigan en pausa
        });

        salir.setOnClickListener(v -> {
            if (acciones != null && acciones.size() > 1) {
                acciones.get(1).onClick(v);
            }
            // Cierra el menu
            dismiss();
        });
    }
}
