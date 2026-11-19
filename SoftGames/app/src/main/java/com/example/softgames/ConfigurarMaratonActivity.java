package com.example.softgames;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuración del Modo Maratón con soporte para las 8 categorías curriculares (o Modo General).
 */
public class ConfigurarMaratonActivity extends AppCompatActivity {

    private static class OpcionCategoriaMaraton {
        final String nombre;
        final LinearLayout card;
        final TextView titulo;
        final TextView subtitulo;
        final TextView check;
        boolean seleccionada = false;

        OpcionCategoriaMaraton(String nombre, LinearLayout card, TextView titulo, TextView subtitulo, TextView check) {
            this.nombre = nombre;
            this.card = card;
            this.titulo = titulo;
            this.subtitulo = subtitulo;
            this.check = check;
        }
    }

    private boolean modoGeneralActivo = true;
    private LinearLayout cardModoGeneralMaraton;
    private TextView tvTituloModoGeneral;
    private TextView tvSubtituloModoGeneral;
    private TextView tvCheckModoGeneral;

    private final List<OpcionCategoriaMaraton> opcionesCategorias = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configurar_maraton);

        View btnVolver = findViewById(R.id.btnVolverConfigMaraton);
        if (btnVolver != null) {
            btnVolver.setOnClickListener(v -> finish());
        }

        cardModoGeneralMaraton = findViewById(R.id.cardModoGeneralMaraton);
        tvTituloModoGeneral = findViewById(R.id.tvTituloModoGeneral);
        tvSubtituloModoGeneral = findViewById(R.id.tvSubtituloModoGeneral);
        tvCheckModoGeneral = findViewById(R.id.tvCheckModoGeneral);

        registrarOpcionCategoria(
                "Programación",
                R.id.itemCatMaratonProgramacion,
                R.id.tvTituloCatProgramacion,
                R.id.tvSubCatProgramacion,
                R.id.tvCheckCatProgramacion
        );
        registrarOpcionCategoria(
                "Introducción a la ingeniería",
                R.id.itemCatMaratonIngenieria,
                R.id.tvTituloCatIngenieria,
                R.id.tvSubCatIngenieria,
                R.id.tvCheckCatIngenieria
        );
        registrarOpcionCategoria(
                "Lógica",
                R.id.itemCatMaratonLogica,
                R.id.tvTituloCatLogica,
                R.id.tvSubCatLogica,
                R.id.tvCheckCatLogica
        );
        registrarOpcionCategoria(
                "Matemáticas discretas",
                R.id.itemCatMaratonMatematicas,
                R.id.tvTituloCatMatematicas,
                R.id.tvSubCatMatematicas,
                R.id.tvCheckCatMatematicas
        );
        registrarOpcionCategoria(
                "Ingeniería de requerimientos",
                R.id.itemCatMaratonRequerimientos,
                R.id.tvTituloCatRequerimientos,
                R.id.tvSubCatRequerimientos,
                R.id.tvCheckCatRequerimientos
        );
        registrarOpcionCategoria(
                "Análisis y diseño orientado a objetos",
                R.id.itemCatMaratonAnalisisOO,
                R.id.tvTituloCatAnalisisOO,
                R.id.tvSubCatAnalisisOO,
                R.id.tvCheckCatAnalisisOO
        );
        registrarOpcionCategoria(
                "Programación orientada a objetos",
                R.id.itemCatMaratonPOO,
                R.id.tvTituloCatPOO,
                R.id.tvSubCatPOO,
                R.id.tvCheckCatPOO
        );
        registrarOpcionCategoria(
                "Análisis y diseño de algoritmos",
                R.id.itemCatMaratonAlgoritmos,
                R.id.tvTituloCatAlgoritmos,
                R.id.tvSubCatAlgoritmos,
                R.id.tvCheckCatAlgoritmos
        );

        Button btnEmpezarMaraton = findViewById(R.id.btnEmpezarMaraton);

        cardModoGeneralMaraton.setOnClickListener(v -> {
            modoGeneralActivo = !modoGeneralActivo;
            if (modoGeneralActivo) {
                for (OpcionCategoriaMaraton op : opcionesCategorias) {
                    op.seleccionada = false;
                }
            }
            actualizarEstadoVisual();
        });

        btnEmpezarMaraton.setOnClickListener(v -> iniciarMaratonConfigurado());

        actualizarEstadoVisual();
    }

    private void registrarOpcionCategoria(String nombre, int idCard, int idTitulo, int idSub, int idCheck) {
        LinearLayout card = findViewById(idCard);
        TextView titulo = findViewById(idTitulo);
        TextView sub = findViewById(idSub);
        TextView check = findViewById(idCheck);
        if (card == null || titulo == null || sub == null || check == null) {
            return;
        }
        OpcionCategoriaMaraton opcion = new OpcionCategoriaMaraton(nombre, card, titulo, sub, check);
        opcionesCategorias.add(opcion);

        card.setOnClickListener(v -> {
            opcion.seleccionada = !opcion.seleccionada;
            if (opcion.seleccionada) {
                modoGeneralActivo = false;
            }
            actualizarEstadoVisual();
        });
    }

    private void actualizarEstadoVisual() {
        if (modoGeneralActivo) {
            cardModoGeneralMaraton.setBackgroundResource(R.drawable.bg_chip_selected);
            tvTituloModoGeneral.setTextColor(0xFFFFFFFF);
            tvSubtituloModoGeneral.setTextColor(0xFFFAF5EC);
            tvCheckModoGeneral.setText("✓");
        } else {
            cardModoGeneralMaraton.setBackgroundResource(R.drawable.bg_categoria_pill);
            tvTituloModoGeneral.setTextColor(0xFF0D1B2A);
            tvSubtituloModoGeneral.setTextColor(0xFF415A77);
            tvCheckModoGeneral.setText("");
        }

        for (OpcionCategoriaMaraton op : opcionesCategorias) {
            aplicarEstiloItemCategoria(op.card, op.titulo, op.subtitulo, op.check, op.seleccionada);
        }
    }

    private void aplicarEstiloItemCategoria(LinearLayout card, TextView titulo, TextView subtitulo, TextView check, boolean activo) {
        if (activo) {
            card.setBackgroundResource(R.drawable.bg_chip_selected);
            titulo.setTextColor(0xFFFFFFFF);
            subtitulo.setTextColor(0xFFFAF5EC);
            check.setText("✓");
        } else {
            card.setBackgroundResource(R.drawable.bg_categoria_pill);
            titulo.setTextColor(0xFF0D1B2A);
            subtitulo.setTextColor(0xFF415A77);
            check.setText("");
        }
    }

    private void iniciarMaratonConfigurado() {
        ArrayList<String> categoriasElegidas = new ArrayList<>();
        for (OpcionCategoriaMaraton op : opcionesCategorias) {
            if (op.seleccionada) {
                categoriasElegidas.add(op.nombre);
            }
        }

        if (!modoGeneralActivo && categoriasElegidas.isEmpty()) {
            Toast.makeText(this,
                    "Selecciona el Modo General o marca al menos una categoría para comenzar",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, ModoMaratonActivity.class);
        intent.putExtra("modoGeneral", modoGeneralActivo);
        intent.putStringArrayListExtra("categoriasSeleccionadas", categoriasElegidas);
        startActivity(intent);
        finish();
    }
}
