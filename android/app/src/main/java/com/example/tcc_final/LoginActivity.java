package com.example.tcc_final;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import api.ApiClient;
import api.UsuarioService;
import database.MODEL.Usuario;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextInputEditText editTextEmail = findViewById(R.id.editTextEmail);
        TextInputEditText editTextSenha = findViewById(R.id.editTextSenha);

        Button btnEntrar = findViewById(R.id.btnEntrar);
        btnEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextEmail.getText().toString().trim();
                String senha = editTextSenha.getText().toString();

                if (email.isEmpty() || senha.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Preencha e-mail e senha", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Cria o objeto do usuário com as credenciais digitadas
                Usuario loginUsuario = new Usuario(email, senha);

                // Chama o Retrofit para enviar os dados de login à API
                UsuarioService service = ApiClient.getClient().create(UsuarioService.class);
                Call<String> call = service.fazerLogin(loginUsuario);

                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Login efetuado com sucesso!", Toast.LENGTH_SHORT).show();

                            // Captura o e-mail digitado e salva no SharedPreferences para o check-in do mascote
                            String emailLogado = editTextEmail.getText().toString().trim();
                            SharedPreferences preferences = getSharedPreferences("SessaoUsuario", MODE_PRIVATE);
                            preferences.edit().putString("email_ativo", emailLogado).apply();

                            // Redireciona para a tela principal (MainActivity com as abas)
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "E-mail ou senha incorretos", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Toast.makeText(LoginActivity.this, "Erro de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        Button btnCadastro = findViewById(R.id.btnCadastro);
        btnCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, CadastroActivity.class);
                startActivity(intent);
            }
        });
    }
}