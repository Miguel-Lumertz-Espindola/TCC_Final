package com.example.Database_TCC.gamificacao;

import com.example.Database_TCC.dto.HorariosCheckinDTO;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@ConfigurationProperties(prefix="gamificacao.checkin")
public class HorariosCheckin implements InitializingBean {
    private String fusoHorario="America/Sao_Paulo";
    private Janela cafe=new Janela(LocalTime.of(7,0),LocalTime.of(8,0),LocalTime.of(8,30));
    private Janela almoco=new Janela(LocalTime.of(12,0),LocalTime.of(13,0),LocalTime.of(13,30));
    private Janela jantar=new Janela(LocalTime.of(19,0),LocalTime.of(20,0),LocalTime.of(20,30));
    public record Resultado(int pontos, String precisao) { }

    public static class Janela {
        private LocalTime inicio,fimExato,fimParcial;
        public Janela() { }
        public Janela(LocalTime inicio,LocalTime fimExato,LocalTime fimParcial) {
            this.inicio=inicio;this.fimExato=fimExato;this.fimParcial=fimParcial;
        }
        public LocalTime getInicio(){return inicio;}
        public void setInicio(LocalTime valor){inicio=valor;}
        public LocalTime getFimExato(){return fimExato;}
        public void setFimExato(LocalTime valor){fimExato=valor;}
        public LocalTime getFimParcial(){return fimParcial;}
        public void setFimParcial(LocalTime valor){fimParcial=valor;}
        void validar(String nome){
            if(inicio==null||fimExato==null||fimParcial==null
                    || !inicio.isBefore(fimExato)||!fimExato.isBefore(fimParcial))
                throw new IllegalStateException("Horários de "+nome+": início deve ser anterior ao fim exato, que deve ser anterior ao fim parcial.");
        }
    }
    public String getFusoHorario(){return fusoHorario;}
    public void setFusoHorario(String valor){fusoHorario=valor;}
    public Janela getCafe(){return cafe;}
    public void setCafe(Janela valor){cafe=valor;}
    public Janela getAlmoco(){return almoco;}
    public void setAlmoco(Janela valor){almoco=valor;}
    public Janela getJantar(){return jantar;}
    public void setJantar(Janela valor){jantar=valor;}
    public ZoneId zona(){return ZoneId.of(fusoHorario);}
    @Override public void afterPropertiesSet(){
        zona();
        if(cafe==null||almoco==null||jantar==null)throw new IllegalStateException("Configure as três refeições do check-in.");
        cafe.validar("café da manhã");almoco.validar("almoço");jantar.validar("jantar");
    }
    public Resultado avaliar(String tipo,LocalTime hora){
        Janela janela=switch(tipo){case "Cafe_da_Manha"->cafe;case "Almoco"->almoco;case "Jantar"->jantar;default->null;};
        if(janela==null)return new Resultado(0,"Refeição sem faixa de pontuação");
        if(!hora.isBefore(janela.inicio)&&hora.isBefore(janela.fimExato))return new Resultado(30,"Exato");
        if(!hora.isBefore(janela.fimExato)&&hora.isBefore(janela.fimParcial))return new Resultado(15,"Parcialmente preciso");
        return new Resultado(0,"Fora do horário");
    }
    private HorariosCheckinDTO.FaixaDTO faixa(String tipo,String nome,Janela janela){
        DateTimeFormatter formato=DateTimeFormatter.ofPattern("HH:mm");
        return new HorariosCheckinDTO.FaixaDTO(tipo,nome,janela.inicio.format(formato),janela.fimExato.format(formato),janela.fimParcial.format(formato));
    }
    public HorariosCheckinDTO consultar(Clock relogio){
        return new HorariosCheckinDTO(fusoHorario,LocalTime.now(relogio.withZone(zona())).format(DateTimeFormatter.ofPattern("HH:mm")),
                List.of(faixa("Cafe_da_Manha","Café da manhã",cafe),faixa("Almoco","Almoço",almoco),faixa("Jantar","Jantar",jantar)));
    }
}
