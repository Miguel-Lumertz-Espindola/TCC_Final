package com.example.tcc_final;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import android.util.Patterns;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.example.tcc_final.dto.CadastroRequest;
import java.text.SimpleDateFormat;
import java.util.*;
import api.ApiClient;
import api.UsuarioService;
import retrofit2.*;

public class CadastroActivity extends AppCompatActivity {
    private int etapa;
    private boolean enviando;
    private Button continuar, anterior;
    private final String[] objetivos = {"MANUTENCAO", "EMAGRECIMENTO", "HIPERTROFIA"};
    private final String[] praticidades = {"RAPIDA", "ELABORADA"};
    private final String[] rigores = {"FLEXIVEL", "ESTRUTURADO"};
    private final String[] orcamentos = {"ECONOMICO", "FLEXIVEL"};
    private final String[] atividades = {"SEDENTARIO", "LEVE", "MODERADO", "INTENSO"};
    private String texto(int id) {
        TextInputEditText campo = findViewById(id);
        return campo.getText() == null ? "" : campo.getText().toString();
    }
    private boolean erro(int id, String mensagem) {
        TextInputEditText campo = findViewById(id);
        campo.setError(mensagem); campo.requestFocus(); return false;
    }
    private String nascimentoIso() {
        String valor = texto(R.id.nascimento).trim();
        if (!valor.matches("\\d{2}/\\d{2}/\\d{4}")) return null;
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy", Locale.ROOT);
        formato.setLenient(false);
        try {
            Date data = formato.parse(valor);
            Calendar hoje = Calendar.getInstance();
            hoje.set(Calendar.HOUR_OF_DAY, 0); hoje.set(Calendar.MINUTE, 0);
            hoje.set(Calendar.SECOND, 0); hoje.set(Calendar.MILLISECOND, 0);
            if (data == null || !data.before(hoje.getTime())) return null;
            return new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(data);
        } catch (java.text.ParseException e) { return null; }
    }
    private double numero(int id) {
        try { return Double.parseDouble(texto(id).trim().replace(',', '.')); }
        catch (NumberFormatException e) { return -1; }
    }
    private boolean validar() {
        if (etapa == 0) {
            if (texto(R.id.nome).trim().isEmpty()) return erro(R.id.nome, "Informe seu nome");
            if (!Patterns.EMAIL_ADDRESS.matcher(texto(R.id.email).trim()).matches()) return erro(R.id.email, "Informe um e-mail válido");
            if (texto(R.id.senha).length() < 8) return erro(R.id.senha, "Use pelo menos 8 caracteres");
            if (!texto(R.id.senha).equals(texto(R.id.confirmacao))) return erro(R.id.confirmacao, "As senhas não coincidem");
        } else if (etapa == 1) {
            if (nascimentoIso() == null) return erro(R.id.nascimento, "Informe uma data válida anterior a hoje");
            if (!(numero(R.id.altura) > 0)) return erro(R.id.altura, "Informe a altura em centímetros");
            if (!(numero(R.id.peso) > 0)) return erro(R.id.peso, "Informe o peso em quilogramas");
        } else {
            if (((Spinner)findViewById(R.id.objetivo)).getSelectedItemPosition() == 0
                    || ((Spinner)findViewById(R.id.atividade)).getSelectedItemPosition() == 0
                    || ((Spinner)findViewById(R.id.praticidade)).getSelectedItemPosition() == 0
                    || ((Spinner)findViewById(R.id.rigor)).getSelectedItemPosition() == 0
                    || ((Spinner)findViewById(R.id.orcamento)).getSelectedItemPosition() == 0) {
                Toast.makeText(this, getString(R.string.cadastro_perfil_preencher), Toast.LENGTH_SHORT).show(); return false;
            }
        }
        return true;
    }
    @Override protected void onCreate(Bundle estado) {
        super.onCreate(estado); setContentView(R.layout.activity_cadastro);
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v,insets)->{
            androidx.core.graphics.Insets barras=insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(barras.left,barras.top,barras.right,barras.bottom); return insets;
        });
        configurarSpinner(R.id.objetivo, new String[]{"Selecione", "Manutenção", "Emagrecimento", "Hipertrofia"});
        configurarSpinner(R.id.atividade, new String[]{"Selecione", "Sedentário", "Leve", "Moderado", "Intenso"});
        configurarSpinner(R.id.praticidade, getResources().getStringArray(R.array.cadastro_praticidades));
        configurarSpinner(R.id.rigor, getResources().getStringArray(R.array.cadastro_rigores));
        configurarSpinner(R.id.orcamento, getResources().getStringArray(R.array.cadastro_orcamentos));
        continuar = findViewById(R.id.continuar); anterior = findViewById(R.id.anterior);
        etapa = estado == null ? 0 : estado.getInt("etapa", 0);
        continuar.setOnClickListener(v -> { if (!enviando && validar()) { if (etapa < 2) { etapa++; atualizar(); } else enviar(); } });
        anterior.setOnClickListener(v -> { if (!enviando && etapa > 0) { etapa--; atualizar(); } });
        findViewById(R.id.voltarLogin).setOnClickListener(v -> { if (!enviando) finish(); });
        atualizar();
    }
    private void configurarSpinner(int id, String[] valores) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.cadastro_opcao_selecionada, valores);
        adapter.setDropDownViewResource(R.layout.cadastro_opcao_dropdown);
        ((Spinner)findViewById(id)).setAdapter(adapter);
    }
    private void atualizar() {
        int[] ids = {R.id.etapa0, R.id.etapa1, R.id.etapa2};
        String[] titulos = {"1 de 3 · Conta", "2 de 3 · Perfil físico", "3 de 3 · Objetivo e alimentação"};
        for (int i=0; i<3; i++) findViewById(ids[i]).setVisibility(i == etapa ? View.VISIBLE : View.GONE);
        ((TextView)findViewById(R.id.etapaTitulo)).setText(titulos[etapa]);
        anterior.setVisibility(etapa > 0 ? View.VISIBLE : View.GONE);
        anterior.setEnabled(!enviando); continuar.setEnabled(!enviando);
        continuar.setText(enviando ? "Salvando…" : etapa == 2 ? "Concluir cadastro" : "Continuar");
    }
    @Override protected void onSaveInstanceState(Bundle estado) {
        estado.putInt("etapa", etapa); super.onSaveInstanceState(estado);
    }
    private void enviar() {
        CadastroRequest dados = new CadastroRequest();
        dados.nome = texto(R.id.nome).trim(); dados.email = texto(R.id.email).trim(); dados.senha = texto(R.id.senha);
        dados.dataNascimento = nascimentoIso(); dados.alturaCm = numero(R.id.altura); dados.pesoInicialKg = numero(R.id.peso);
        dados.objetivo = objetivos[((Spinner)findViewById(R.id.objetivo)).getSelectedItemPosition()-1];
        dados.nivelAtividade = atividades[((Spinner)findViewById(R.id.atividade)).getSelectedItemPosition()-1];
        dados.praticidade = praticidades[((Spinner)findViewById(R.id.praticidade)).getSelectedItemPosition()-1];
        dados.rigor = rigores[((Spinner)findViewById(R.id.rigor)).getSelectedItemPosition()-1];
        dados.orcamento = orcamentos[((Spinner)findViewById(R.id.orcamento)).getSelectedItemPosition()-1];
        dados.tagsAlimentares = new ArrayList<>();
        int[] idsTags = {R.id.tagSemLactose, R.id.tagSemGluten, R.id.tagVegetariano, R.id.tagVegano};
        String[] codigosTags = {"SEM_LACTOSE", "SEM_GLUTEN", "VEGETARIANO", "VEGANO"};
        for (int i=0; i<idsTags.length; i++)
            if (((com.google.android.material.chip.Chip)findViewById(idsTags[i])).isChecked())
                dados.tagsAlimentares.add(codigosTags[i]);
        enviando = true; atualizar();
        ApiClient.getClient().create(UsuarioService.class).cadastrarUsuario(dados).enqueue(new Callback<String>() {
            @Override public void onResponse(Call<String> call, Response<String> response) {
                if (isFinishing() || isDestroyed()) return;
                enviando = false; atualizar();
                if (response.isSuccessful()) {
                    getSharedPreferences("SessaoUsuario", MODE_PRIVATE).edit().putString("email_ativo", dados.email).apply();
                    Intent intent = new Intent(CadastroActivity.this, DietaActivity.class).putExtra("aposCadastro", true);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent); finish();
                } else {
                    String mensagem = "Não foi possível concluir o cadastro (" + response.code() + ")";
                    try { if (response.errorBody() != null) mensagem = response.errorBody().string(); } catch (java.io.IOException ignored) { }
                    Toast.makeText(CadastroActivity.this, mensagem, Toast.LENGTH_LONG).show();
                }
            }
            @Override public void onFailure(Call<String> call, Throwable erro) {
                if (isFinishing() || isDestroyed()) return;
                enviando = false; atualizar();
                Toast.makeText(CadastroActivity.this, "Falha de conexão. Confira sua conexão e tente novamente.", Toast.LENGTH_LONG).show();
            }
        });
    }
}
