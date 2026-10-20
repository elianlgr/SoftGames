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

public class ConfigurarMaratonActivity extends AppCompatActivity {

    private boolean modoGeneralActivo = true;
    private boolean catLogicaSeleccionada = false;
    private boolean catProgramacionSeleccionada = false;
    private boolean catAlgoritmosSeleccionada = false;

    private LinearLayout cardModoGeneralMaraton;
    private TextView tvTituloModoGeneral;
    private TextView tvSubtituloModoGeneral;
    private TextView tvCheckModoGeneral;

    private LinearLayout itemCatMaratonLogica;
    private TextView tvTituloCatLogica;
    private TextView tvSubCatLogica;
    private TextView tvCheckCatLogica;

    private LinearLayout itemCatMaratonProgramacion;
    private TextView tvTituloCatProgramacion;
    private TextView tvSubCatProgramacion;
    private TextView tvCheckCatProgramacion;

    private LinearLayout itemCatMaratonAlgoritmos;
    private TextView tvTituloCatAlgoritmos;
    private TextView tvSubCatAlgoritmos;
    private TextView tvCheckCatAlgoritmos;

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

        itemCatMaratonLogica = findViewById(R.id.itemCatMaratonLogica);
        tvTituloCatLogica = findViewById(R.id.tvTituloCatLogica);
        tvSubCatLogica = findViewById(R.id.tvSubCatLogica);
        tvCheckCatLogica = findViewById(R.id.tvCheckCatLogica);

        itemCatMaratonProgramacion = findViewById(R.id.itemCatMaratonProgramacion);
        tvTituloCatProgramacion = findViewById(R.id.tvTituloCatProgramacion);
        tvSubCatProgramacion = findViewById(R.id.tvSubCatProgramacion);
        tvCheckCatProgramacion = findViewById(R.id.tvCheckCatProgramacion);

        itemCatMaratonAlgoritmos = findViewById(R.id.itemCatMaratonAlgoritmos);
        tvTituloCatAlgoritmos = findViewById(R.id.tvTituloCatAlgoritmos);
        tvSubCatAlgoritmos = findViewById(R.id.tvSubCatAlgoritmos);
        tvCheckCatAlgoritmos = findViewById(R.id.tvCheckCatAlgoritmos);

        Button btnEmpezarMaraton = findViewById(R.id.btnEmpezarMaraton);

        cardModoGeneralMaraton.setOnClickListener(v -> {
            modoGeneralActivo = !modoGeneralActivo;
            if (modoGeneralActivo) {
                catLogicaSeleccionada = false;
                catProgramacionSeleccionada = false;
                catAlgoritmosSeleccionada = false;
            }
            actualizarEstadoVisual();
        });

        itemCatMaratonLogica.setOnClickListener(v -> {
            catLogicaSeleccionada = !catLogicaSeleccionada;
            if (catLogicaSeleccionada) {
                modoGeneralActivo = false;
            }
            actualizarEstadoVisual();
        });

        itemCatMaratonProgramacion.setOnClickListener(v -> {
            catProgramacionSeleccionada = !catProgramacionSeleccionada;
            if (catProgramacionSeleccionada) {
                modoGeneralActivo = false;
            }
            actualizarEstadoVisual();
        });

        itemCatMaratonAlgoritmos.setOnClickListener(v -> {
            catAlgoritmosSeleccionada = !catAlgoritmosSeleccionada;
            if (catAlgoritmosSeleccionada) {
                modoGeneralActivo = false;
            }
            actualizarEstadoVisual();
        });

        btnEmpezarMaraton.setOnClickListener(v -> iniciarMaratonConfigurado());

        actualizarEstadoVisual();
    }

    private void actualizarEstadoVisual() {
        // Estado visual de Modo General
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

        aplicarEstiloItemCategoria(itemCatMaratonLogica, tvTituloCatLogica, tvSubCatLogica, tvCheckCatLogica, catLogicaSeleccionada);
        aplicarEstiloItemCategoria(itemCatMaratonProgramacion, tvTituloCatProgramacion, tvSubCatProgramacion, tvCheckCatProgramacion, catProgramacionSeleccionada);
        aplicarEstiloItemCategoria(itemCatMaratonAlgoritmos, tvTituloCatAlgoritmos, tvSubCatAlgoritmos, tvCheckCatAlgoritmos, catAlgoritmosSeleccionada);
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
        if (catLogicaSeleccionada) {
            categoriasElegidas.add("Lógica");
        }
        if (catProgramacionSeleccionada) {
            categoriasElegidas.add("Programación");
        }
        if (catAlgoritmosSeleccionada) {
            categoriasElegidas.add("Análisis y diseño de algoritmos");
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
