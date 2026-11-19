package com.example.softgames;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Caso de Uso CU-S3-02: Configurar ajustes de audio y preferencias de juego.
 * Permite al usuario ajustar el volumen de música de fondo, volumen de efectos de sonido,
 * silenciar/activar el audio general, activar/desactivar la vibración táctil, restablecer
 * los valores por defecto (80%) y cambiar la contraseña de la cuenta, guardando todo
 * persistentemente en SharedPreferences y usuarios_registrados.json.
 */
public class AjustesActivity extends AppCompatActivity {

    public static final String PREFS_AJUSTES = "AjustesJuego";
    public static final String KEY_SONIDO_ACTIVO = "sonidoActivo";
    public static final String KEY_VOLUMEN_MUSICA = "volumenMusica";
    public static final String KEY_VOLUMEN_EFECTOS = "volumenEfectos";
    public static final String KEY_VIBRACION_ACTIVA = "vibracionActiva";
    public static final int VOLUMEN_PREDETERMINADO = 80;

    private boolean sonidoActivo = true;
    private int volumenMusica = VOLUMEN_PREDETERMINADO;
    private int volumenEfectos = VOLUMEN_PREDETERMINADO;
    private boolean vibracionActiva = true;

    private TextView tvEstadoAudioAjustes;
    private TextView btnToggleSonidoGeneral;
    private TextView tvValorVolumenMusica;
    private SeekBar sbVolumenMusica;
    private TextView tvValorVolumenEfectos;
    private SeekBar sbVolumenEfectos;
    private TextView btnToggleVibracion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajustes);

        View btnVolver = findViewById(R.id.btnVolver);
        Button btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        Button btnRestablecerAjustes = findViewById(R.id.btnRestablecerAjustes);
        Button btnCambiarPassword = findViewById(R.id.btnCambiarPassword);

        tvEstadoAudioAjustes = findViewById(R.id.tvEstadoAudioAjustes);
        btnToggleSonidoGeneral = findViewById(R.id.btnToggleSonidoGeneral);
        tvValorVolumenMusica = findViewById(R.id.tvValorVolumenMusica);
        sbVolumenMusica = findViewById(R.id.sbVolumenMusica);
        tvValorVolumenEfectos = findViewById(R.id.tvValorVolumenEfectos);
        sbVolumenEfectos = findViewById(R.id.sbVolumenEfectos);
        btnToggleVibracion = findViewById(R.id.btnToggleVibracion);

        cargarPreferenciasGuardadas();
        actualizarInterfazAjustes();

        if (btnVolver != null) {
            btnVolver.setOnClickListener(v -> finish());
        }

        if (btnToggleSonidoGeneral != null) {
            btnToggleSonidoGeneral.setOnClickListener(v -> {
                sonidoActivo = !sonidoActivo;
                guardarPreferencias();
                actualizarInterfazAjustes();
                if (!sonidoActivo) {
                    Toast.makeText(this, "Audio silenciado", Toast.LENGTH_SHORT).show();
                } else {
                    reproducirRetroalimentacion(this, v, true);
                }
            });
        }

        if (sbVolumenMusica != null) {
            sbVolumenMusica.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    volumenMusica = progress;
                    if (fromUser && progress > 0 && !sonidoActivo) {
                        sonidoActivo = true;
                    } else if (fromUser && volumenMusica == 0 && volumenEfectos == 0) {
                        sonidoActivo = false;
                    }
                    actualizarInterfazAjustes();
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    guardarPreferencias();
                }
            });
        }

        if (sbVolumenEfectos != null) {
            sbVolumenEfectos.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    volumenEfectos = progress;
                    if (fromUser && progress > 0 && !sonidoActivo) {
                        sonidoActivo = true;
                    } else if (fromUser && volumenMusica == 0 && volumenEfectos == 0) {
                        sonidoActivo = false;
                    }
                    actualizarInterfazAjustes();
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    guardarPreferencias();
                    reproducirRetroalimentacion(AjustesActivity.this, seekBar, true);
                }
            });
        }

        if (btnToggleVibracion != null) {
            btnToggleVibracion.setOnClickListener(v -> {
                vibracionActiva = !vibracionActiva;
                guardarPreferencias();
                actualizarInterfazAjustes();
                if (vibracionActiva) {
                    v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                }
            });
        }

        if (btnRestablecerAjustes != null) {
            btnRestablecerAjustes.setOnClickListener(v -> {
                sonidoActivo = true;
                volumenMusica = VOLUMEN_PREDETERMINADO;
                volumenEfectos = VOLUMEN_PREDETERMINADO;
                vibracionActiva = true;
                guardarPreferencias();
                actualizarInterfazAjustes();
                Toast.makeText(this, "Ajustes restablecidos al 80% por defecto", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnCambiarPassword != null) {
            btnCambiarPassword.setOnClickListener(v -> mostrarDialogoCambiarPassword());
        }

        if (btnCerrarSesion != null) {
            btnCerrarSesion.setOnClickListener(v -> cerrarSesion());
        }
    }

    private void mostrarDialogoCambiarPassword() {
        SharedPreferences prefsSesion = getSharedPreferences("SesionJuego", MODE_PRIVATE);
        final String usuarioActual = prefsSesion.getString("usuarioActual", "default");

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        float densidad = getResources().getDisplayMetrics().density;
        int padH = Math.round(22 * densidad);
        int padV = Math.round(24 * densidad);

        LinearLayout tarjeta = new LinearLayout(this);
        tarjeta.setOrientation(LinearLayout.VERTICAL);
        tarjeta.setGravity(Gravity.CENTER_HORIZONTAL);
        tarjeta.setBackgroundResource(R.drawable.bg_card_ajustes);
        tarjeta.setPadding(padH, padV, padH, padV);

        TextView tvTitulo = new TextView(this);
        tvTitulo.setText("Cambiar contraseña");
        tvTitulo.setTextColor(0xFF142232);
        tvTitulo.setTextSize(20f);
        tvTitulo.setTypeface(null, Typeface.BOLD);
        tvTitulo.setGravity(Gravity.CENTER);
        tarjeta.addView(tvTitulo);

        TextView tvSub = new TextView(this);
        tvSub.setText("Cuenta: " + usuarioActual);
        tvSub.setTextColor(0xFF415A77);
        tvSub.setTextSize(13f);
        tvSub.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams paramsSub = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        paramsSub.topMargin = Math.round(4 * densidad);
        paramsSub.bottomMargin = Math.round(16 * densidad);
        tvSub.setLayoutParams(paramsSub);
        tarjeta.addView(tvSub);

        int altoInput = Math.round(46 * densidad);
        int padInputH = Math.round(14 * densidad);

        final EditText etPassActual = new EditText(this);
        LinearLayout.LayoutParams paramsIn1 = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, altoInput);
        paramsIn1.bottomMargin = Math.round(10 * densidad);
        etPassActual.setLayoutParams(paramsIn1);
        etPassActual.setHint("Contraseña actual");
        etPassActual.setHintTextColor(0xFF7A6F5D);
        etPassActual.setTextColor(0xFF142232);
        etPassActual.setTextSize(14.5f);
        etPassActual.setSingleLine(true);
        etPassActual.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        etPassActual.setBackgroundResource(R.drawable.bg_input_preguntas);
        etPassActual.setPadding(padInputH, 0, padInputH, 0);
        tarjeta.addView(etPassActual);

        final EditText etPassNueva = new EditText(this);
        LinearLayout.LayoutParams paramsIn2 = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, altoInput);
        paramsIn2.bottomMargin = Math.round(10 * densidad);
        etPassNueva.setLayoutParams(paramsIn2);
        etPassNueva.setHint("Nueva contraseña");
        etPassNueva.setHintTextColor(0xFF7A6F5D);
        etPassNueva.setTextColor(0xFF142232);
        etPassNueva.setTextSize(14.5f);
        etPassNueva.setSingleLine(true);
        etPassNueva.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        etPassNueva.setBackgroundResource(R.drawable.bg_input_preguntas);
        etPassNueva.setPadding(padInputH, 0, padInputH, 0);
        tarjeta.addView(etPassNueva);

        final EditText etPassConfirmar = new EditText(this);
        LinearLayout.LayoutParams paramsIn3 = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, altoInput);
        paramsIn3.bottomMargin = Math.round(18 * densidad);
        etPassConfirmar.setLayoutParams(paramsIn3);
        etPassConfirmar.setHint("Confirmar nueva contraseña");
        etPassConfirmar.setHintTextColor(0xFF7A6F5D);
        etPassConfirmar.setTextColor(0xFF142232);
        etPassConfirmar.setTextSize(14.5f);
        etPassConfirmar.setSingleLine(true);
        etPassConfirmar.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        etPassConfirmar.setBackgroundResource(R.drawable.bg_input_preguntas);
        etPassConfirmar.setPadding(padInputH, 0, padInputH, 0);
        tarjeta.addView(etPassConfirmar);

        LinearLayout filaBotones = new LinearLayout(this);
        filaBotones.setOrientation(LinearLayout.HORIZONTAL);
        filaBotones.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        int altoBoton = Math.round(48 * densidad);
        int margenMitad = Math.round(6 * densidad);

        AppCompatButton btnCancelar = new AppCompatButton(this);
        LinearLayout.LayoutParams paramsCancelar = new LinearLayout.LayoutParams(0, altoBoton, 1.0f);
        paramsCancelar.rightMargin = margenMitad;
        btnCancelar.setLayoutParams(paramsCancelar);
        btnCancelar.setText("CANCELAR");
        btnCancelar.setTextColor(0xFF142232);
        btnCancelar.setTextSize(13.5f);
        btnCancelar.setTypeface(null, Typeface.BOLD);
        btnCancelar.setBackgroundResource(R.drawable.bg_btn_ajustes);
        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        AppCompatButton btnGuardarPass = new AppCompatButton(this);
        LinearLayout.LayoutParams paramsGuardar = new LinearLayout.LayoutParams(0, altoBoton, 1.0f);
        paramsGuardar.leftMargin = margenMitad;
        btnGuardarPass.setLayoutParams(paramsGuardar);
        btnGuardarPass.setText("GUARDAR");
        btnGuardarPass.setTextColor(0xFFFFFFFF);
        btnGuardarPass.setTextSize(13.5f);
        btnGuardarPass.setTypeface(null, Typeface.BOLD);
        btnGuardarPass.setBackgroundResource(R.drawable.bg_btn_continuar);
        btnGuardarPass.setOnClickListener(v -> {
            String passActual = etPassActual.getText().toString().trim();
            String passNueva = etPassNueva.getText().toString().trim();
            String passConfirmar = etPassConfirmar.getText().toString().trim();

            if (passActual.isEmpty() || passNueva.isEmpty() || passConfirmar.isEmpty()) {
                Toast.makeText(this, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!passNueva.equals(passConfirmar)) {
                etPassConfirmar.setError("Las contraseñas no coinciden");
                Toast.makeText(this, "La nueva contraseña y su confirmación no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }
            if (passNueva.equals(passActual)) {
                etPassNueva.setError("Ingresa una contraseña distinta a la actual");
                Toast.makeText(this, "La nueva contraseña debe ser distinta a la actual", Toast.LENGTH_SHORT).show();
                return;
            }

            if (actualizarPasswordCuenta(this, usuarioActual, passActual, passNueva)) {
                Toast.makeText(this, "Contraseña actualizada correctamente", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                etPassActual.setError("Contraseña actual incorrecta");
                Toast.makeText(this, "La contraseña actual ingresada es incorrecta", Toast.LENGTH_SHORT).show();
            }
        });

        filaBotones.addView(btnCancelar);
        filaBotones.addView(btnGuardarPass);
        tarjeta.addView(filaBotones);

        dialog.setContentView(tarjeta);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int anchoPantalla = getResources().getDisplayMetrics().widthPixels;
            dialog.getWindow().setLayout((int) (anchoPantalla * 0.88f), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        dialog.show();
    }

    public static boolean actualizarPasswordCuenta(Context context, String usuario, String passActual, String passNueva) {
        try {
            JSONArray cuentas = RegistroActivity.cargarCuentas(context);
            for (int i = 0; i < cuentas.length(); i++) {
                JSONObject cuenta = cuentas.optJSONObject(i);
                if (cuenta != null) {
                    String regUsuario = cuenta.optString("usuario", "");
                    String regCorreo = cuenta.optString("correo", "");
                    if (regUsuario.equalsIgnoreCase(usuario) || regCorreo.equalsIgnoreCase(usuario)) {
                        String passwordGuardada = cuenta.optString("password", "");
                        if (!passwordGuardada.equals(passActual)) {
                            return false;
                        }
                        cuenta.put("password", passNueva);
                        return RegistroActivity.guardarCuentas(context, cuentas);
                    }
                }
            }
            // Si era un perfil local previo sin registro en usuarios_registrados.json, lo registra con la nueva contraseña
            JSONObject nuevaEntrada = new JSONObject();
            nuevaEntrada.put("correo", usuario + "@softgames.local");
            nuevaEntrada.put("usuario", usuario);
            nuevaEntrada.put("password", passNueva);
            cuentas.put(nuevaEntrada);
            return RegistroActivity.guardarCuentas(context, cuentas);
        } catch (Exception e) {
            return false;
        }
    }

    private void cargarPreferenciasGuardadas() {
        SharedPreferences prefs = getSharedPreferences(PREFS_AJUSTES, MODE_PRIVATE);
        sonidoActivo = prefs.getBoolean(KEY_SONIDO_ACTIVO, true);
        volumenMusica = prefs.getInt(KEY_VOLUMEN_MUSICA, VOLUMEN_PREDETERMINADO);
        volumenEfectos = prefs.getInt(KEY_VOLUMEN_EFECTOS, VOLUMEN_PREDETERMINADO);
        vibracionActiva = prefs.getBoolean(KEY_VIBRACION_ACTIVA, true);
    }

    private void guardarPreferencias() {
        SharedPreferences prefs = getSharedPreferences(PREFS_AJUSTES, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_SONIDO_ACTIVO, sonidoActivo);
        editor.putInt(KEY_VOLUMEN_MUSICA, volumenMusica);
        editor.putInt(KEY_VOLUMEN_EFECTOS, volumenEfectos);
        editor.putBoolean(KEY_VIBRACION_ACTIVA, vibracionActiva);
        editor.apply();
    }

    private void actualizarInterfazAjustes() {
        int musicaEfectiva = sonidoActivo ? volumenMusica : 0;
        int efectosEfectivos = sonidoActivo ? volumenEfectos : 0;

        if (tvValorVolumenMusica != null) {
            tvValorVolumenMusica.setText(musicaEfectiva + "%");
            tvValorVolumenMusica.setTextColor(musicaEfectiva > 0 ? 0xFF708274 : 0xFF7A6F5D);
        }
        if (sbVolumenMusica != null && sbVolumenMusica.getProgress() != volumenMusica) {
            sbVolumenMusica.setProgress(volumenMusica);
        }
        if (sbVolumenMusica != null) {
            sbVolumenMusica.setAlpha(sonidoActivo ? 1.0f : 0.5f);
        }

        if (tvValorVolumenEfectos != null) {
            tvValorVolumenEfectos.setText(efectosEfectivos + "%");
            tvValorVolumenEfectos.setTextColor(efectosEfectivos > 0 ? 0xFF708274 : 0xFF7A6F5D);
        }
        if (sbVolumenEfectos != null && sbVolumenEfectos.getProgress() != volumenEfectos) {
            sbVolumenEfectos.setProgress(volumenEfectos);
        }
        if (sbVolumenEfectos != null) {
            sbVolumenEfectos.setAlpha(sonidoActivo ? 1.0f : 0.5f);
        }

        if (btnToggleSonidoGeneral != null) {
            if (sonidoActivo && (volumenMusica > 0 || volumenEfectos > 0)) {
                btnToggleSonidoGeneral.setText("✓ ACTIVADO");
                btnToggleSonidoGeneral.setBackgroundResource(R.drawable.bg_chip_selected);
                btnToggleSonidoGeneral.setTextColor(0xFFFFFFFF);
            } else {
                btnToggleSonidoGeneral.setText("SILENCIADO");
                btnToggleSonidoGeneral.setBackgroundResource(R.drawable.bg_btn_ajustes);
                btnToggleSonidoGeneral.setTextColor(0xFF142232);
            }
        }

        if (btnToggleVibracion != null) {
            if (vibracionActiva) {
                btnToggleVibracion.setText("✓ ACTIVADA");
                btnToggleVibracion.setBackgroundResource(R.drawable.bg_chip_selected);
                btnToggleVibracion.setTextColor(0xFFFFFFFF);
            } else {
                btnToggleVibracion.setText("DESACTIVADA");
                btnToggleVibracion.setBackgroundResource(R.drawable.bg_btn_ajustes);
                btnToggleVibracion.setTextColor(0xFF142232);
            }
        }

        if (tvEstadoAudioAjustes != null) {
            if (!sonidoActivo || (volumenMusica == 0 && volumenEfectos == 0)) {
                tvEstadoAudioAjustes.setText("Modo silencio activo • El audio de los minijuegos está desactivado");
            } else {
                tvEstadoAudioAjustes.setText("Música: " + volumenMusica + "%  •  Efectos: " + volumenEfectos
                        + "%  •  Vibración: " + (vibracionActiva ? "Sí" : "No"));
            }
        }
    }

    public static void reproducirRetroalimentacion(Context context, View vista, boolean esCorrecto) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_AJUSTES, MODE_PRIVATE);
        boolean sonido = prefs.getBoolean(KEY_SONIDO_ACTIVO, true);
        int volEfectos = prefs.getInt(KEY_VOLUMEN_EFECTOS, VOLUMEN_PREDETERMINADO);
        boolean vibracion = prefs.getBoolean(KEY_VIBRACION_ACTIVA, true);

        if (vibracion && vista != null) {
            vista.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        }

        if (sonido && volEfectos > 0) {
            try {
                final ToneGenerator toneGen = new ToneGenerator(AudioManager.STREAM_MUSIC, Math.min(100, volEfectos));
                int tipoTono = esCorrecto ? ToneGenerator.TONE_PROP_ACK : ToneGenerator.TONE_PROP_NACK;
                toneGen.startTone(tipoTono, 120);
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    try {
                        toneGen.release();
                    } catch (Exception ignored) {
                    }
                }, 200);
            } catch (Exception ignored) {
                // En entornos de prueba sin hardware de audio se ignora de forma segura
            }
        }
    }

    private void cerrarSesion() {
        SharedPreferences prefs = getSharedPreferences("SesionJuego", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("slotActivo");
        editor.remove("usuarioActual");
        editor.apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
