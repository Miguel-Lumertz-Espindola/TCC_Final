package database.MODEL;

import com.google.gson.annotations.SerializedName;

public class StatusGamificacao {

    @SerializedName("pontuacaoTotal")
    private int pontuacaoTotal;

    @SerializedName("ofensivaDias")
    private int ofensivaDias;

    @SerializedName("statusMascote")
    private String statusMascote;

    // Getters para lermos as informações na tela
    public int getPontuacaoTotal() { return pontuacaoTotal; }
    public int getOfensivaDias() { return ofensivaDias; }
    public String getStatusMascote() { return statusMascote; }

    private String dataReferencia;
    private java.util.List<String> refeicoesRegistradasHoje;
    public String getDataReferencia() { return dataReferencia; }
    public java.util.List<String> getRefeicoesRegistradasHoje() {
        return refeicoesRegistradasHoje==null?java.util.Collections.emptyList():refeicoesRegistradasHoje;
    }

    private String personagemSelecionado;
    private java.util.List<com.example.tcc_final.dto.PersonagemDTO> personagens;
    public String getPersonagemSelecionado(){return personagemSelecionado==null?"GUERREIRO":personagemSelecionado;}
    public java.util.List<com.example.tcc_final.dto.PersonagemDTO> getPersonagens(){
        return personagens==null?java.util.Collections.emptyList():personagens;
    }
    public com.example.tcc_final.dto.PersonagemDTO getPersonagem(String codigo){
        for(var p:getPersonagens())if(p!=null&&codigo.equals(p.codigo))return p;
        return null;
    }
}
