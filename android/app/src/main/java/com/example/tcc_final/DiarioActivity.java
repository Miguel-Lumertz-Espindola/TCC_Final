package com.example.tcc_final;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.*;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.example.tcc_final.dto.RefeicaoDTO;
import com.example.tcc_final.dto.MetasConsultaDTO;
import com.example.tcc_final.nutricao.*;
import api.ApiClient;
import api.DiarioService;
import api.MetasService;
import retrofit2.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class DiarioActivity extends AppCompatActivity {
    private final Calendar dia = Calendar.getInstance();
    private final String[] tipos = {"CAFE_DA_MANHA", "ALMOCO", "JANTAR", "LANCHE", "OUTROS"};
    private String[] nomesTipos;
    private DiarioService api;
    private String email;
    private Button data, proximo, repetir;
    private TextView status;
    private final List<RefeicaoDTO> refeicoes = new ArrayList<>();
    private RefeicoesAdapter adapter;
    private Call<List<RefeicaoDTO>> consulta;
    private Call<?> alteracao;
    private AlertDialog formulario;
    private Call<MetasConsultaDTO> consultaMetas;
    private MetasConsultaDTO metas;
    private boolean metasConsultadas,erroMetas,diarioCarregado;

    private String formatar(Calendar valor, String formato) {
        return new SimpleDateFormat(formato, Locale.getDefault()).format(valor.getTime());
    }
    private boolean encerrada() { return isFinishing() || isDestroyed(); }
    private boolean ocupada() {
        if (alteracao == null) return false;
        Toast.makeText(this, R.string.diario_busy, Toast.LENGTH_SHORT).show(); return true;
    }
    private void escolherData(Calendar valor, Runnable depois) {
        DatePickerDialog seletor = new DatePickerDialog(this, (v, ano, mes, diaMes) -> {
            valor.set(ano, mes, diaMes); depois.run();
        }, valor.get(Calendar.YEAR), valor.get(Calendar.MONTH), valor.get(Calendar.DAY_OF_MONTH));
        seletor.getDatePicker().setMaxDate(System.currentTimeMillis()); seletor.show();
    }
    @Override protected void onCreate(Bundle estado) {
        super.onCreate(estado); setContentView(R.layout.activity_diario);
        // Insets keep the toolbar and list clear of system bars on Android 15+.
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            androidx.core.graphics.Insets barras = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom); return insets;
        });
        MaterialToolbar toolbar = findViewById(R.id.toolbarDiario);
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        toolbar.setNavigationIconTint(getColor(R.color.ranking_accent));
        toolbar.setNavigationContentDescription(R.string.diario_back);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setOnMenuItemClickListener(item -> {
            if(item.getItemId()!=R.id.abrirMetas)return false;
            if(!ocupada())startActivity(new android.content.Intent(this,MetasActivity.class));
            return true;
        });
        email = getSharedPreferences("SessaoUsuario", MODE_PRIVATE).getString("email_ativo", "");
        if (email == null || email.isEmpty()) {
            Toast.makeText(this, R.string.diario_session, Toast.LENGTH_LONG).show(); finish(); return;
        }
        if (estado != null) dia.setTimeInMillis(estado.getLong("dia", dia.getTimeInMillis()));
        nomesTipos = getResources().getStringArray(R.array.diario_types);
        api = ApiClient.getClient().create(DiarioService.class);
        data = findViewById(R.id.dataDiario); proximo = findViewById(R.id.diaSeguinte);
        status = findViewById(R.id.statusDiario); repetir = findViewById(R.id.repetirDiario);
        RecyclerView lista = findViewById(R.id.listaDiario);
        lista.setLayoutManager(new LinearLayoutManager(this)); adapter = new RefeicoesAdapter(); lista.setAdapter(adapter);
        data.setOnClickListener(v -> escolherData(dia, this::carregar));
        findViewById(R.id.diaAnterior).setOnClickListener(v -> { dia.add(Calendar.DAY_OF_MONTH, -1); carregar(); });
        proximo.setOnClickListener(v -> { dia.add(Calendar.DAY_OF_MONTH, 1); carregar(); });
        findViewById(R.id.hojeDiario).setOnClickListener(v -> { dia.setTimeInMillis(System.currentTimeMillis()); carregar(); });
        findViewById(R.id.novaRefeicao).setOnClickListener(v -> { if (!ocupada()) abrirFormulario(null); });
        repetir.setOnClickListener(v -> carregar());
    }
    @Override protected void onResume() {
        super.onResume(); if(api!=null && !encerrada())carregar();
    }
    @Override protected void onSaveInstanceState(Bundle estado) {
        estado.putLong("dia", dia.getTimeInMillis()); super.onSaveInstanceState(estado);
    }
    private String mensagem(Response<?> resposta) {
        String padrao = getString(R.string.diario_server_error, resposta.code());
        try {
            if (resposta.errorBody() == null) return padrao;
            String texto = resposta.errorBody().string();
            if (texto.startsWith("{")) {
                String detalhe = new org.json.JSONObject(texto).optString("message", "");
                return detalhe.isEmpty() ? padrao : detalhe;
            }
            return texto.isEmpty() ? padrao : texto;
        } catch (Exception e) { return padrao; }
    }
    private void carregar() {
        if (consulta != null) consulta.cancel();
        diarioCarregado=false;metas=null;metasConsultadas=false;erroMetas=false;
        carregarMetas();
        data.setText(formatar(dia, "dd/MM/yyyy"));
        proximo.setEnabled(formatar(dia, "yyyy-MM-dd").compareTo(formatar(Calendar.getInstance(), "yyyy-MM-dd")) < 0);
        refeicoes.clear(); adapter.notifyDataSetChanged();
        findViewById(R.id.painelTotais).setVisibility(View.GONE);
        status.setVisibility(View.VISIBLE); status.setText(R.string.diario_loading); repetir.setVisibility(View.GONE);
        consulta = api.listar(email, formatar(dia, "yyyy-MM-dd"));
        consulta.enqueue(new Callback<List<RefeicaoDTO>>() {
            @Override public void onResponse(Call<List<RefeicaoDTO>> call, Response<List<RefeicaoDTO>> resposta) {
                if (encerrada() || call != consulta) return;
                if (!resposta.isSuccessful() || resposta.body() == null) { falhaLista(mensagem(resposta)); return; }
                refeicoes.addAll(resposta.body()); adapter.notifyDataSetChanged();
                diarioCarregado=true;
                atualizarTotais();
                status.setText(R.string.diario_empty); status.setVisibility(refeicoes.isEmpty() ? View.VISIBLE : View.GONE);
            }
            @Override public void onFailure(Call<List<RefeicaoDTO>> call, Throwable erro) {
                if (!encerrada() && !call.isCanceled() && call == consulta) falhaLista(getString(R.string.diario_connection));
            }
        });
    }
    private void carregarMetas() {
        if(consultaMetas!=null)consultaMetas.cancel();
        consultaMetas=ApiClient.getClient().create(MetasService.class).consultar(email,formatar(dia,"yyyy-MM-dd"));
        consultaMetas.enqueue(new Callback<MetasConsultaDTO>() {
            @Override public void onResponse(Call<MetasConsultaDTO> call,Response<MetasConsultaDTO> resposta) {
                if(encerrada() || call!=consultaMetas)return;
                metasConsultadas=true;erroMetas=!resposta.isSuccessful() || resposta.body()==null;
                metas=erroMetas?null:resposta.body();if(diarioCarregado)atualizarTotais();
            }
            @Override public void onFailure(Call<MetasConsultaDTO> call,Throwable erro) {
                if(encerrada() || call.isCanceled() || call!=consultaMetas)return;
                metasConsultadas=true;erroMetas=true;if(diarioCarregado)atualizarTotais();
            }
        });
    }
    private void atualizarTotais() {
        boolean temMetas=metas!=null && metas.meta!=null;
        findViewById(R.id.painelTotais).setVisibility(refeicoes.isEmpty() && !temMetas ? View.GONE : View.VISIBLE);
        ResumoNutricional resumo = new ResumoNutricional(refeicoes);
        ((TextView)findViewById(R.id.totaisNutrientes)).setText(temMetas
                ? TextoNutricional.comparar(this,resumo,metas.meta) : TextoNutricional.resumo(this,resumo));
        ((TextView)findViewById(R.id.metasDoDia)).setText(!metasConsultadas ? R.string.metas_loading
                : erroMetas ? R.string.metas_unavailable : temMetas ? R.string.metas_vigencia_help : R.string.metas_empty);
        boolean parcial = resumo.calorias.ausentes > 0 || resumo.proteinas.ausentes > 0
                || resumo.carboidratos.ausentes > 0 || resumo.gorduras.ausentes > 0;
        ((TextView)findViewById(R.id.ajudaTotais)).setText(parcial
                ? R.string.alimentos_total_help : R.string.alimentos_total_complete);
    }
    private void falhaLista(String texto) {
        status.setText(texto); status.setVisibility(View.VISIBLE); repetir.setVisibility(View.VISIBLE);
    }
    private Calendar horario(RefeicaoDTO refeicao) {
        Calendar resultado = (Calendar)dia.clone();
        Calendar agora = Calendar.getInstance();
        resultado.set(Calendar.HOUR_OF_DAY, agora.get(Calendar.HOUR_OF_DAY));
        resultado.set(Calendar.MINUTE, agora.get(Calendar.MINUTE)); resultado.set(Calendar.SECOND, 0); resultado.set(Calendar.MILLISECOND, 0);
        if (refeicao != null && refeicao.dataHoraConsumo != null) {
            try {
                java.util.Date valor = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.ROOT).parse(refeicao.dataHoraConsumo);
                if (valor != null) resultado.setTime(valor);
            } catch (java.text.ParseException ignored) { }
        }
        return resultado;
    }
    private String tipoNome(String tipo) {
        for (int i=0; i<tipos.length; i++) if (tipos[i].equals(tipo)) return nomesTipos[i];
        return nomesTipos[4];
    }
    private void abrirFormulario(RefeicaoDTO original) {
        View campos = getLayoutInflater().inflate(R.layout.dialog_refeicao, null);
        Calendar consumo = horario(original);
        EditorItensRefeicao editor = new EditorItensRefeicao(this, campos, original == null ? null : original.itens);
        Spinner tipo = campos.findViewById(R.id.tipoRefeicaoDiario);
        ArrayAdapter<String> opcoes = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, nomesTipos);
        opcoes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); tipo.setAdapter(opcoes);
        TextInputEditText descricao = campos.findViewById(R.id.consumoDiario);
        TextInputEditText observacao = campos.findViewById(R.id.observacaoDiario);
        if (original != null) {
            for (int i=0; i<tipos.length; i++) if (tipos[i].equals(original.tipoRefeicao)) tipo.setSelection(i);
            descricao.setText(original.descricao); observacao.setText(original.observacao);
        }
        Button dataConsumo = campos.findViewById(R.id.dataRefeicaoDiario);
        Button horaConsumo = campos.findViewById(R.id.horaRefeicaoDiario);
        Runnable atualizar = () -> {
            dataConsumo.setText(getString(R.string.diario_date, formatar(consumo, "dd/MM/yyyy")));
            horaConsumo.setText(getString(R.string.diario_time, formatar(consumo, "HH:mm")));
        };
        atualizar.run();
        dataConsumo.setOnClickListener(v -> escolherData(consumo, atualizar));
        horaConsumo.setOnClickListener(v -> new TimePickerDialog(this, (view, hora, minuto) -> {
            consumo.set(Calendar.HOUR_OF_DAY, hora); consumo.set(Calendar.MINUTE, minuto); atualizar.run();
        }, consumo.get(Calendar.HOUR_OF_DAY), consumo.get(Calendar.MINUTE), true).show());
        AlertDialog dialogo = new MaterialAlertDialogBuilder(this)
                .setTitle(original == null ? R.string.diario_new : R.string.diario_edit).setView(campos)
                .setNegativeButton(R.string.diario_cancel, null).setPositiveButton(R.string.diario_save, null).create();
        formulario = dialogo;
        dialogo.setOnDismissListener(v -> editor.fechar());
        dialogo.setOnShowListener(v -> dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(botao -> {
            String texto = descricao.getText() == null ? "" : descricao.getText().toString().trim();
            if (texto.isEmpty() && editor.obterItens().isEmpty()) { descricao.setError(getString(R.string.diario_description_required)); descricao.requestFocus(); return; }
            if (consumo.getTimeInMillis() > System.currentTimeMillis()) {
                Toast.makeText(this, R.string.diario_future, Toast.LENGTH_LONG).show(); return;
            }
            RefeicaoDTO dados = new RefeicaoDTO();
            dados.tipoRefeicao = tipos[tipo.getSelectedItemPosition()]; dados.descricao = texto;
            dados.itens = editor.obterItens();
            dados.observacao = observacao.getText() == null ? "" : observacao.getText().toString().trim();
            dados.dataHoraConsumo = formatar(consumo, "yyyy-MM-dd'T'HH:mm:ss");
            salvar(original, dados, consumo, dialogo);
        }));
        dialogo.show();
    }
    private void formularioOcupado(AlertDialog dialogo, boolean ocupado) {
        habilitarCampos(dialogo.findViewById(R.id.formularioRefeicao), !ocupado);
        dialogo.setCancelable(!ocupado); dialogo.setCanceledOnTouchOutside(!ocupado);
        dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!ocupado);
        dialogo.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(!ocupado);
        dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setText(ocupado ? R.string.diario_saving : R.string.diario_save);
    }
    private void habilitarCampos(View raiz, boolean habilitado) {
        if (raiz == null) return;
        raiz.setEnabled(habilitado);
        if (raiz instanceof ViewGroup) {
            ViewGroup grupo = (ViewGroup)raiz;
            for (int i=0; i<grupo.getChildCount(); i++) habilitarCampos(grupo.getChildAt(i), habilitado);
        }
    }
    private void salvar(RefeicaoDTO original, RefeicaoDTO dados, Calendar consumo, AlertDialog dialogo) {
        if (ocupada()) return;
        formularioOcupado(dialogo, true);
        Call<RefeicaoDTO> chamada = original == null ? api.registrar(email, dados) : api.editar(email, original.id, dados);
        alteracao = chamada;
        chamada.enqueue(new Callback<RefeicaoDTO>() {
            @Override public void onResponse(Call<RefeicaoDTO> call, Response<RefeicaoDTO> resposta) {
                if (encerrada()) return;
                alteracao = null; formularioOcupado(dialogo, false);
                if (resposta.isSuccessful()) {
                    dialogo.dismiss(); dia.setTimeInMillis(consumo.getTimeInMillis()); carregar();
                    Toast.makeText(DiarioActivity.this, R.string.diario_saved, Toast.LENGTH_SHORT).show();
                } else Toast.makeText(DiarioActivity.this, mensagem(resposta), Toast.LENGTH_LONG).show();
            }
            @Override public void onFailure(Call<RefeicaoDTO> call, Throwable erro) {
                if (encerrada() || call.isCanceled()) return;
                alteracao = null; formularioOcupado(dialogo, false);
                Toast.makeText(DiarioActivity.this, R.string.diario_connection, Toast.LENGTH_LONG).show();
            }
        });
    }
    private void confirmarExclusao(RefeicaoDTO refeicao) {
        if (ocupada()) return;
        new MaterialAlertDialogBuilder(this).setTitle(R.string.diario_delete_title)
                .setMessage(R.string.diario_delete_message).setNegativeButton(R.string.diario_cancel, null)
                .setPositiveButton(R.string.diario_delete, (dialogo, botao) -> excluir(refeicao)).show();
    }
    private void excluir(RefeicaoDTO refeicao) {
        if (ocupada()) return;
        Call<Void> chamada = api.excluir(email, refeicao.id); alteracao = chamada;
        chamada.enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> resposta) {
                if (encerrada()) return;
                alteracao = null;
                if (resposta.isSuccessful()) {
                    carregar(); Toast.makeText(DiarioActivity.this, R.string.diario_deleted, Toast.LENGTH_SHORT).show();
                } else Toast.makeText(DiarioActivity.this, mensagem(resposta), Toast.LENGTH_LONG).show();
            }
            @Override public void onFailure(Call<Void> call, Throwable erro) {
                if (encerrada() || call.isCanceled()) return;
                alteracao = null; Toast.makeText(DiarioActivity.this, R.string.diario_connection, Toast.LENGTH_LONG).show();
            }
        });
    }
    @Override protected void onDestroy() {
        if (consulta != null) consulta.cancel();
        if (consultaMetas != null) consultaMetas.cancel();
        if (alteracao != null) alteracao.cancel();
        if (formulario != null) formulario.dismiss();
        super.onDestroy();
    }
    private class RefeicoesAdapter extends RecyclerView.Adapter<RefeicaoHolder> {
        @Override public RefeicaoHolder onCreateViewHolder(ViewGroup parent, int tipo) {
            return new RefeicaoHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_refeicao, parent, false));
        }
        @Override public void onBindViewHolder(RefeicaoHolder h, int posicao) {
            RefeicaoDTO r = refeicoes.get(posicao);
            h.resumo.setText(getString(R.string.diario_summary, tipoNome(r.tipoRefeicao), formatar(horario(r), "HH:mm")));
            h.descricao.setText(r.descricao); h.observacao.setText(r.observacao);
            h.alimentos.setText(TextoNutricional.alimentos(DiarioActivity.this, r.itens));
            h.observacao.setVisibility(r.observacao == null || r.observacao.isEmpty() ? View.GONE : View.VISIBLE);
            h.editar.setOnClickListener(v -> { if (!ocupada()) abrirFormulario(r); });
            h.excluir.setOnClickListener(v -> confirmarExclusao(r));
        }
        @Override public int getItemCount() { return refeicoes.size(); }
    }
    private static class RefeicaoHolder extends RecyclerView.ViewHolder {
        TextView resumo, descricao, observacao, alimentos; View editar, excluir;
        RefeicaoHolder(View v) {
            super(v); alimentos=v.findViewById(R.id.alimentosRefeicao); resumo=v.findViewById(R.id.resumoRefeicao); descricao=v.findViewById(R.id.descricaoRefeicao);
            observacao=v.findViewById(R.id.observacaoRefeicao); editar=v.findViewById(R.id.editarRefeicao); excluir=v.findViewById(R.id.excluirRefeicao);
        }
    }
}
