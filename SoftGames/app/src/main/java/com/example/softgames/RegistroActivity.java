package com.example.softgames;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class RegistroActivity extends AppCompatActivity {

    public static final String ARCHIVO_CUENTAS = "usuarios_registrados.json";

    private EditText etCorreoRegistro;
    private EditText etUsuarioRegistro;
    private EditText etPasswordRegistro;
    private Button btnRegistrarse;
    private Button btnVolverLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        View btnVolver = findViewById(R.id.btnVolverRegistro);
        etCorreoRegistro = findViewById(R.id.etCorreoRegistro);
        etUsuarioRegistro = findViewById(R.id.etUsuarioRegistro);
        etPasswordRegistro = findViewById(R.id.etPasswordRegistro);
        btnRegistrarse = findViewById(R.id.btnRegistrarse);
        btnVolverLogin = findViewById(R.id.btnVolverLogin);

        if (btnVolver != null) {
            btnVolver.setOnClickListener(v -> finish());
        }

        btnVolverLogin.setOnClickListener(v -> finish());

        btnRegistrarse.setOnClickListener(v -> registrarCuenta());
    }

    private void registrarCuenta() {
        String correo = etCorreoRegistro.getText().toString().trim();
        String usuario = etUsuarioRegistro.getText().toString().trim();
        String password = etPasswordRegistro.getText().toString().trim();

        if (correo.isEmpty() || usuario.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Toast.makeText(this, "Por favor, ingresa un correo electrónico válido", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONArray cuentas = cargarCuentas(this);

            // Verificar que el correo o el nombre de usuario no estén ya registrados
            for (int i = 0; i < cuentas.length(); i++) {
                JSONObject cuenta = cuentas.getJSONObject(i);
                if (cuenta.optString("correo", "").equalsIgnoreCase(correo)) {
                    Toast.makeText(this, "Este correo electrónico ya está registrado", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (cuenta.optString("usuario", "").equalsIgnoreCase(usuario)) {
                    Toast.makeText(this, "El nombre de usuario ya está en uso", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            JSONObject nuevaCuenta = new JSONObject();
            nuevaCuenta.put("correo", correo);
            nuevaCuenta.put("usuario", usuario);
            nuevaCuenta.put("password", password);
            cuentas.put(nuevaCuenta);

            if (guardarCuentas(this, cuentas)) {
                SharedPreferences prefs = getSharedPreferences("SesionJuego", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("usuarioActual", usuario);
                editor.remove("slotActivo");
                editor.apply();

                Toast.makeText(this, "¡Cuenta creada exitosamente! Bienvenido, " + usuario, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(RegistroActivity.this, InicioActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Error al guardar la cuenta", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("RegistroActivity", "Error al registrar cuenta", e);
            Toast.makeText(this, "Error al crear la cuenta", Toast.LENGTH_SHORT).show();
        }
    }

    public static JSONArray cargarCuentas(Context context) {
        try {
            File file = new File(context.getFilesDir(), ARCHIVO_CUENTAS);
            if (!file.exists()) {
                return new JSONArray();
            }
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();
            String jsonStr = new String(data, StandardCharsets.UTF_8);
            return new JSONArray(jsonStr);
        } catch (Exception e) {
            Log.e("RegistroActivity", "Error al leer cuentas registradas", e);
            return new JSONArray();
        }
    }

    public static boolean guardarCuentas(Context context, JSONArray cuentas) {
        try {
            File file = new File(context.getFilesDir(), ARCHIVO_CUENTAS);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(cuentas.toString().getBytes(StandardCharsets.UTF_8));
            fos.close();
            return true;
        } catch (Exception e) {
            Log.e("RegistroActivity", "Error al guardar cuentas registradas", e);
            return false;
        }
    }
}
