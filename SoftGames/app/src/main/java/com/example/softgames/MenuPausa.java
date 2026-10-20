package com.example.softgames;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

public class MenuPausa extends Dialog {
    private List<View.OnClickListener> acciones;

    public MenuPausa(Context context, List<View.OnClickListener> acciones) {
        super(context, android.R.style.Theme_NoTitleBar_Fullscreen);
        this.acciones = acciones;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pause_layout);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                getWindow().setStatusBarColor(0xFFF4EEDC);
            }
        }

        // Si el usuario cancela con el botón de retroceso del teléfono, reanudar partida
        setOnCancelListener(dialog -> {
            if (acciones != null && acciones.size() > 0) {
                acciones.get(0).onClick(null);
            }
        });

        // 1. Reanudar partida
        View.OnClickListener listenerReanudar = v -> {
            if (acciones != null && acciones.size() > 0) {
                acciones.get(0).onClick(v);
            }
            dismiss();
        };

        View btnVolver = findViewById(R.id.btnVolverPausa);
        if (btnVolver != null) btnVolver.setOnClickListener(listenerReanudar);

        View cardReanudar = findViewById(R.id.cardReanudar);
        if (cardReanudar != null) cardReanudar.setOnClickListener(listenerReanudar);

        // 2. Ajustes
        View.OnClickListener listenerAjustes = v -> {
            if (acciones != null && acciones.size() > 2) {
                acciones.get(2).onClick(v);
            }
        };

        View btnAjustesPausa = findViewById(R.id.btnAjustesPausa);
        if (btnAjustesPausa != null) btnAjustesPausa.setOnClickListener(listenerAjustes);

        View cardAjustes = findViewById(R.id.cardAjustes);
        if (cardAjustes != null) cardAjustes.setOnClickListener(listenerAjustes);

        // 3. Salir
        View.OnClickListener listenerSalir = v -> {
            if (acciones != null && acciones.size() > 1) {
                acciones.get(1).onClick(v);
            }
            dismiss();
        };

        View cardSalir = findViewById(R.id.cardSalir);
        if (cardSalir != null) cardSalir.setOnClickListener(listenerSalir);
    }
}
