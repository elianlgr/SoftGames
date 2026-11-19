package com.example.softgames;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;

import java.util.List;

/**
 * Caso de Uso CU-S3-05: Pausar partida activa, ocultar reactivo y gestionar abandono seguro.
 * Muestra el menú de pausa en pantalla completa (ocultando el problema activo), permite
 * reanudar la partida, abrir Ajustes o salir al menú previa confirmación.
 */
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

        // 2. Ajustes (Flujo alternativo 4.1)
        View.OnClickListener listenerAjustes = v -> {
            if (acciones != null && acciones.size() > 2) {
                acciones.get(2).onClick(v);
            }
        };

        View btnAjustesPausa = findViewById(R.id.btnAjustesPausa);
        if (btnAjustesPausa != null) btnAjustesPausa.setOnClickListener(listenerAjustes);

        View cardAjustes = findViewById(R.id.cardAjustes);
        if (cardAjustes != null) cardAjustes.setOnClickListener(listenerAjustes);

        // 3. Salir al menú con confirmación (Flujo alternativo 4.2)
        View.OnClickListener listenerSalir = v -> mostrarDialogoConfirmarSalida(v);

        View cardSalir = findViewById(R.id.cardSalir);
        if (cardSalir != null) cardSalir.setOnClickListener(listenerSalir);
    }

    private void mostrarDialogoConfirmarSalida(View vistaOrigen) {
        Context ctx = getContext();
        Dialog confirmDialog = new Dialog(ctx);
        confirmDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        float densidad = ctx.getResources().getDisplayMetrics().density;
        int padH = Math.round(22 * densidad);
        int padV = Math.round(24 * densidad);

        LinearLayout tarjeta = new LinearLayout(ctx);
        tarjeta.setOrientation(LinearLayout.VERTICAL);
        tarjeta.setGravity(Gravity.CENTER_HORIZONTAL);
        tarjeta.setBackgroundResource(R.drawable.bg_card_ajustes);
        tarjeta.setPadding(padH, padV, padH, padV);

        TextView tvTitulo = new TextView(ctx);
        tvTitulo.setText("Salir al menú");
        tvTitulo.setTextColor(0xFF142232);
        tvTitulo.setTextSize(20f);
        tvTitulo.setTypeface(null, Typeface.BOLD);
        tvTitulo.setGravity(Gravity.CENTER);
        tarjeta.addView(tvTitulo);

        TextView tvMensaje = new TextView(ctx);
        tvMensaje.setText("¿Deseas abandonar la partida actual y regresar al menú?");
        tvMensaje.setTextColor(0xFF0D1B2A);
        tvMensaje.setTextSize(14f);
        tvMensaje.setGravity(Gravity.CENTER);
        tvMensaje.setBackgroundResource(R.drawable.bg_categoria_pill);
        int padMsgH = Math.round(16 * densidad);
        int padMsgV = Math.round(14 * densidad);
        tvMensaje.setPadding(padMsgH, padMsgV, padMsgH, padMsgV);
        LinearLayout.LayoutParams paramsMsg = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        paramsMsg.topMargin = Math.round(14 * densidad);
        paramsMsg.bottomMargin = Math.round(20 * densidad);
        tvMensaje.setLayoutParams(paramsMsg);
        tarjeta.addView(tvMensaje);

        LinearLayout filaBotones = new LinearLayout(ctx);
        filaBotones.setOrientation(LinearLayout.HORIZONTAL);
        filaBotones.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        int altoBoton = Math.round(48 * densidad);
        int margenMitad = Math.round(6 * densidad);

        AppCompatButton btnCancelar = new AppCompatButton(ctx);
        LinearLayout.LayoutParams paramsCancelar = new LinearLayout.LayoutParams(0, altoBoton, 1.0f);
        paramsCancelar.rightMargin = margenMitad;
        btnCancelar.setLayoutParams(paramsCancelar);
        btnCancelar.setText("CANCELAR");
        btnCancelar.setTextColor(0xFF142232);
        btnCancelar.setTextSize(13.5f);
        btnCancelar.setTypeface(null, Typeface.BOLD);
        btnCancelar.setBackgroundResource(R.drawable.bg_btn_ajustes);
        btnCancelar.setOnClickListener(v -> confirmDialog.dismiss());

        AppCompatButton btnConfirmarSalir = new AppCompatButton(ctx);
        LinearLayout.LayoutParams paramsSalir = new LinearLayout.LayoutParams(0, altoBoton, 1.0f);
        paramsSalir.leftMargin = margenMitad;
        btnConfirmarSalir.setLayoutParams(paramsSalir);
        btnConfirmarSalir.setText("SALIR");
        btnConfirmarSalir.setTextColor(0xFFFFFFFF);
        btnConfirmarSalir.setTextSize(13.5f);
        btnConfirmarSalir.setTypeface(null, Typeface.BOLD);
        btnConfirmarSalir.setBackgroundResource(R.drawable.bg_btn_cerrar_sesion);
        btnConfirmarSalir.setOnClickListener(v -> {
            confirmDialog.dismiss();
            if (acciones != null && acciones.size() > 1) {
                acciones.get(1).onClick(vistaOrigen);
            }
            dismiss();
        });

        filaBotones.addView(btnCancelar);
        filaBotones.addView(btnConfirmarSalir);
        tarjeta.addView(filaBotones);

        confirmDialog.setContentView(tarjeta);
        if (confirmDialog.getWindow() != null) {
            confirmDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int anchoPantalla = ctx.getResources().getDisplayMetrics().widthPixels;
            confirmDialog.getWindow().setLayout((int) (anchoPantalla * 0.88f), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        confirmDialog.show();
    }
}
