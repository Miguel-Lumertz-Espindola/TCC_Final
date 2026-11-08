package com.example.Database_TCC.service;
import com.example.Database_TCC.model.*;
import com.example.Database_TCC.repository.*;
import com.example.Database_TCC.dto.*;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.lang.reflect.Proxy;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class CheckinServiceTest {
    @SuppressWarnings("unchecked")private static <T>T proxy(Class<T> tipo,java.lang.reflect.InvocationHandler h){return (T)Proxy.newProxyInstance(tipo.getClassLoader(),new Class<?>[]{tipo},h);}
    static class Fixture {
        final Usuario usuario=new Usuario("teste@example.test","teste");
        StatusGamificacao atual;
        final List<HistoricoCheckin> registros=new ArrayList<>();
        final List<UsuarioConquista> medalhas=new ArrayList<>();
        final Map<String,Conquista> catalogo=new HashMap<>();
        final UsuarioRepository usuarios=proxy(UsuarioRepository.class,(obj,method,args)->{
            if(method.getName().equals("findByEmail")||method.getName().equals("buscarParaAtualizarGamificacao"))return usuario.getEmail().equals(args[0])?Optional.of(usuario):Optional.empty();
            throw new UnsupportedOperationException(method.getName());
        });
        final StatusGamificacaoRepository status=proxy(StatusGamificacaoRepository.class,(obj,method,args)->{
            switch(method.getName()){case "findByUsuario":return Optional.ofNullable(atual);case "save":atual=(StatusGamificacao)args[0];return atual;default:throw new UnsupportedOperationException(method.getName());}
        });
        final HistoricoCheckinRepository historico=proxy(HistoricoCheckinRepository.class,(obj,method,args)->{
            switch(method.getName()){
                case "save":{HistoricoCheckin h=(HistoricoCheckin)args[0];h.setId((long)registros.size()+1);registros.add(h);return h;}
                case "findByUsuarioIdAndDataHoraRegistroGreaterThanEqualAndDataHoraRegistroLessThan":return registros.stream().filter(h->!h.getDataHoraRegistro().isBefore((LocalDateTime)args[1])&&h.getDataHoraRegistro().isBefore((LocalDateTime)args[2])).toList();
                case "datasComPontos":return registros.stream().filter(h->h.getPontosGanhos()>0&&h.getDataHoraRegistro().isBefore((LocalDateTime)args[1])).map(HistoricoCheckin::getDataHoraRegistro).toList();
                case "existsByUsuario":return !registros.isEmpty();
                case "countByUsuarioAndTipoRefeicaoIgnoreCaseAndStatusPrecisaoStartingWithIgnoreCase":return registros.stream().filter(h->h.getTipoRefeicao().equalsIgnoreCase((String)args[1])&&h.getStatusPrecisao().toLowerCase(Locale.ROOT).startsWith(((String)args[2]).toLowerCase(Locale.ROOT))).count();
                case "countByUsuarioAndStatusPrecisaoStartingWithIgnoreCase":return registros.stream().filter(h->h.getStatusPrecisao().toLowerCase(Locale.ROOT).startsWith(((String)args[1]).toLowerCase(Locale.ROOT))).count();
                default:throw new UnsupportedOperationException(method.getName());
            }
        });
        final ConquistaRepository conquistas=proxy(ConquistaRepository.class,(obj,method,args)->{if(method.getName().equals("findByNome"))return catalogo.get(args[0]);throw new UnsupportedOperationException(method.getName());});
        final UsuarioConquistaRepository inventario=proxy(UsuarioConquistaRepository.class,(obj,method,args)->{
            if(method.getName().equals("findByUsuario"))return new ArrayList<>(medalhas);
            if(method.getName().equals("save")){medalhas.add((UsuarioConquista)args[0]);return args[0];}throw new UnsupportedOperationException(method.getName());
        });
        Fixture(){usuario.setId(1L);long id=1;for(String nome:List.of("Primeiro Passo","Chama Viva","Mestre do Almoço","Relógio Suíço")){Conquista c=new Conquista(nome,"Descrição","icone",0);c.setId(id++);catalogo.put(nome,c);}}
        CheckinService em(String horario){Clock clock=Clock.fixed(LocalDateTime.parse(horario).atZone(ZoneId.of("America/Sao_Paulo")).toInstant(),ZoneId.of("America/Sao_Paulo"));return new CheckinService(usuarios,status,historico,new GamificacaoService(conquistas,inventario,status,historico),clock);}
        Set<String> nomes(){Set<String>s=new HashSet<>();for(var m:medalhas)s.add(m.getConquista().getNome());return s;}
    }
    @Test void respostaAtualizaPontosERefeicoesDoDia(){
        var f=new Fixture();var r=f.em("2026-10-21T07:30:00").registrar(f.usuario.getEmail(),"Cafe_da_Manha");
        assertEquals(30,r.status().pontuacaoTotal());assertEquals(1,r.status().ofensivaDias());assertEquals(List.of("Cafe_da_Manha"),r.status().refeicoesRegistradasHoje());assertFalse(r.repetido());
    }
    @Test void repetirMesmaRefeicaoNaoGeraNovosPontosNemDias(){
        var f=new Fixture();var s=f.em("2026-10-21T07:30:00");s.registrar(f.usuario.getEmail(),"Cafe_da_Manha");
        var r=s.registrar(f.usuario.getEmail(),"Café da manhã");assertTrue(r.repetido());assertEquals(30,r.status().pontuacaoTotal());assertEquals(1,r.status().ofensivaDias());assertEquals(1,f.registros.size());
    }
    @Test void tresRefeicoesNoMesmoDiaContamUmDia(){
        var f=new Fixture();f.em("2026-10-21T07:30:00").registrar(f.usuario.getEmail(),"Cafe_da_Manha");f.em("2026-10-21T12:00:00").registrar(f.usuario.getEmail(),"Almoco");
        var r=f.em("2026-10-21T19:00:00").registrar(f.usuario.getEmail(),"Jantar");assertEquals(90,r.status().pontuacaoTotal());assertEquals(1,r.status().ofensivaDias());assertEquals(3,r.status().refeicoesRegistradasHoje().size());
    }
    @Test void consultasNaoAlteramOEstadoDoJogador(){
        var f=new Fixture();var s=f.em("2026-10-21T07:30:00");var original=s.registrar(f.usuario.getEmail(),"Cafe_da_Manha");
        assertEquals(original.status(),s.consultar(f.usuario.getEmail()));assertEquals(original.status(),s.consultar(f.usuario.getEmail()));assertEquals(1,f.registros.size());
    }
    @Test void proximoDiaLiberaRefeicaoEIncrementaOfensivaUmaVez(){
        var f=new Fixture();f.em("2026-10-20T07:30:00").registrar(f.usuario.getEmail(),"Cafe_da_Manha");var s=f.em("2026-10-21T07:30:00");
        assertTrue(s.consultar(f.usuario.getEmail()).refeicoesRegistradasHoje().isEmpty());var r=s.registrar(f.usuario.getEmail(),"Cafe_da_Manha");assertEquals(2,r.status().ofensivaDias());assertEquals(60,r.status().pontuacaoTotal());
    }
    @Test void intervaloSemRegistroReiniciaOfensiva(){
        var f=new Fixture();f.em("2026-10-19T07:30:00").registrar(f.usuario.getEmail(),"Cafe_da_Manha");var s=f.em("2026-10-21T07:30:00");
        assertEquals(0,s.consultar(f.usuario.getEmail()).ofensivaDias());assertEquals(1,s.registrar(f.usuario.getEmail(),"Cafe_da_Manha").status().ofensivaDias());
    }
    @Test void almocoSemPontosNaoIncrementaDiaMasContaPrimeiroRegistro(){
        var f=new Fixture();var r=f.em("2026-10-21T18:00:00").registrar(f.usuario.getEmail(),"Almoco");assertEquals(0,r.status().pontuacaoTotal());assertEquals(0,r.status().ofensivaDias());assertTrue(f.nomes().contains("Primeiro Passo"));
    }
    @Test void limitesDosHorariosDeAlmoco(){
        String[] horas={"00:00:00","11:30:00","11:59:59.999999999","12:00:00","12:59:59.999999999","13:00:00","13:29:59.999999999","13:30:00","14:00:00","23:59:59"};int[] pontos={0,0,0,30,30,15,15,0,0,0};
        for(int i=0;i<horas.length;i++){var f=new Fixture();assertEquals(pontos[i],f.em("2026-10-21T"+horas[i]).registrar(f.usuario.getEmail(),"Almoco").status().pontuacaoTotal());}
    }
    @Test void quatroConquistasSaoDesbloqueadasSemDuplicar(){
        var f=new Fixture();
        for(int i=1;i<=7;i++)f.em(String.format("2026-10-%02dT12:00:00",i)).registrar(f.usuario.getEmail(),"Almoco");
        assertEquals(Set.of("Primeiro Passo","Chama Viva","Mestre do Almoço"),f.nomes());
        for(int i=8;i<=10;i++)f.em(String.format("2026-10-%02dT12:00:00",i)).registrar(f.usuario.getEmail(),"Almoco");
        assertTrue(f.nomes().contains("Relógio Suíço"));assertEquals(4,f.medalhas.size());
        f.em("2026-10-10T12:00:00").registrar(f.usuario.getEmail(),"Almoco");assertEquals(4,f.medalhas.size());
    }
    @Test void registrosAntigosDoMesmoDiaNaoInflamOfensiva(){
        LocalDate hoje=LocalDate.of(2026,10,21);assertEquals(2,CheckinService.ofensiva(List.of(hoje.atTime(8,0),hoje.atTime(12,0),hoje.minusDays(1).atTime(8,0)),hoje));
    }
    @Test void naoAceitaTipoInvalidoOuUsuarioInexistente(){
        var f=new Fixture();var s=f.em("2026-10-21T07:30:00");assertEquals(400,assertThrows(ResponseStatusException.class,()->s.registrar(f.usuario.getEmail(),"qualquer" )).getStatusCode().value());
        assertEquals(404,assertThrows(ResponseStatusException.class,()->s.registrar("ausente@example.test","Almoco")).getStatusCode().value());assertTrue(f.registros.isEmpty());
    }

    @Test void bonusParcialContaComoDiaValidoMasNaoComoPrecisaoExata(){
        var f=new Fixture();var r=f.em("2026-10-21T13:20:00").registrar(f.usuario.getEmail(),"Almoco");
        assertEquals(15,r.status().pontuacaoTotal());assertEquals(1,r.status().ofensivaDias());
        assertFalse(f.registros.getFirst().getStatusPrecisao().startsWith("Exato"));
    }
    @Test void diaSemCheckinComPontosZeraSequenciaEProximoValidoComecaEmUm(){
        var f=new Fixture();f.em("2026-10-19T12:00:00").registrar(f.usuario.getEmail(),"Almoco");
        // No dia 20, o usuario acessou e registrou fora da janela, sem pontos.
        f.em("2026-10-20T14:00:00").registrar(f.usuario.getEmail(),"Almoco");
        var retorno=f.em("2026-10-21T13:20:00");assertEquals(0,retorno.consultar(f.usuario.getEmail()).ofensivaDias());
        var r=retorno.registrar(f.usuario.getEmail(),"Almoco");assertEquals(1,r.status().ofensivaDias());assertEquals(45,r.status().pontuacaoTotal());
    }
    @Test void novoDiaNaoZeraAntesDeTerminarPrazoParaRegistrar(){
        var f=new Fixture();f.em("2026-10-20T12:00:00").registrar(f.usuario.getEmail(),"Almoco");
        assertEquals(1,f.em("2026-10-21T00:00:00").consultar(f.usuario.getEmail()).ofensivaDias());
        assertEquals(0,f.em("2026-10-22T00:00:00").consultar(f.usuario.getEmail()).ofensivaDias());
    }
    @Test void cafeExatoContaParaRelogioSuicoMasNaoParaMestreDoAlmoco(){
        var f=new Fixture();for(int i=1;i<=10;i++)f.em(String.format("2026-10-%02dT07:30:00",i)).registrar(f.usuario.getEmail(),"Cafe_da_Manha");
        assertFalse(f.nomes().contains("Mestre do Almoço"));assertTrue(f.nomes().contains("Relógio Suíço"));
    }
    @Test void precisaoExataAntigaDeCinquentaPontosContinuaValendoParaConquistas(){
        var f=new Fixture();
        for(int i=1;i<=9;i++){
            HistoricoCheckin h=new HistoricoCheckin(f.usuario,"Almoco",50,"Exato (Boost Máximo!)");h.setDataHoraRegistro(LocalDate.of(2026,10,i).atTime(12,0));f.registros.add(h);
        }
        f.em("2026-10-10T12:00:00").registrar(f.usuario.getEmail(),"Almoco");
        assertTrue(f.nomes().contains("Mestre do Almoço"));assertTrue(f.nomes().contains("Relógio Suíço"));
    }

    @Test void limitesDosHorariosDeCafeEJantar(){
        String[][] horas={
            {"00:00:00","06:59:59.999999999","07:00:00","07:59:59.999999999","08:00:00","08:29:59.999999999","08:30:00","12:00:00","23:59:59"},
            {"00:00:00","18:59:59.999999999","19:00:00","19:59:59.999999999","20:00:00","20:29:59.999999999","20:30:00","21:00:00","23:59:59"}
        };
        int[] pontos={0,0,30,30,15,15,0,0,0};String[] tipos={"Cafe_da_Manha","Jantar"};
        for(int t=0;t<tipos.length;t++)for(int i=0;i<pontos.length;i++){
            var f=new Fixture();var r=f.em("2026-11-04T"+horas[t][i]).registrar(f.usuario.getEmail(),tipos[t]);
            assertEquals(pontos[i],r.status().pontuacaoTotal(),tipos[t]+" "+horas[t][i]);
            assertEquals(pontos[i]>0?1:0,r.status().ofensivaDias());
        }
    }
    private CheckinService utc(Fixture f,String instante){
        return new CheckinService(f.usuarios,f.status,f.historico,new GamificacaoService(f.conquistas,f.inventario,f.status,f.historico),Clock.fixed(Instant.parse(instante),ZoneOffset.UTC));
    }
    @Test void apiEmUtcAindaUsaMeioDiaDeSaoPaulo(){
        var f=new Fixture();var r=utc(f,"2026-11-04T15:30:00Z").registrar(f.usuario.getEmail(),"Almoco");
        assertEquals(30,r.status().pontuacaoTotal());
        assertEquals(LocalDateTime.parse("2026-11-04T12:30:00"),f.registros.getFirst().getDataHoraRegistro());
        assertTrue(r.mensagem().contains("12:30 -03:00"));
    }
    @Test void meioDiaEmUtcNaoDaPontosQuandoSaoNoveDaManhaNoBrasil(){
        var f=new Fixture();var r=utc(f,"2026-11-04T12:30:00Z").registrar(f.usuario.getEmail(),"Almoco");
        assertEquals(0,r.status().pontuacaoTotal());assertEquals(0,r.status().ofensivaDias());
        assertTrue(r.mensagem().contains("09:30 -03:00"));
    }
    @Test void diaELiberacaoDosBotoesTambemSeguemSaoPaulo(){
        var f=new Fixture();f.em("2026-11-04T12:30:00").registrar(f.usuario.getEmail(),"Almoco");
        var antes=utc(f,"2026-11-05T02:59:59Z").consultar(f.usuario.getEmail());
        assertEquals(LocalDate.of(2026,11,4),antes.dataReferencia());
        assertTrue(antes.refeicoesRegistradasHoje().contains("Almoco"));
        var depois=utc(f,"2026-11-05T03:00:00Z").consultar(f.usuario.getEmail());
        assertEquals(LocalDate.of(2026,11,5),depois.dataReferencia());assertTrue(depois.refeicoesRegistradasHoje().isEmpty());
    }
    @Test void outrosNaoPodeSerUsadoParaGanharTrintaPontosSemHorario(){
        var f=new Fixture();var r=f.em("2026-11-04T17:00:00").registrar(f.usuario.getEmail(),"Outros");
        assertEquals(0,r.status().pontuacaoTotal());assertEquals(0,r.status().ofensivaDias());
    }
    @Test void horariosConsultadosPeloAppSaoOsMesmosDaPontuacao(){
        var f=new Fixture();var horarios=new com.example.Database_TCC.gamificacao.HorariosCheckin();
        horarios.setAlmoco(new com.example.Database_TCC.gamificacao.HorariosCheckin.Janela(LocalTime.of(14,0),LocalTime.of(15,0),LocalTime.of(15,30)));
        horarios.afterPropertiesSet();
        var s=new CheckinService(f.usuarios,f.status,f.historico,new GamificacaoService(f.conquistas,f.inventario,f.status,f.historico),horarios,
                Clock.fixed(Instant.parse("2026-11-04T17:30:00Z"),ZoneOffset.UTC));
        var consulta=s.consultarHorarios();assertEquals("America/Sao_Paulo",consulta.fusoHorario());
        assertEquals("14:30",consulta.horaAtual());assertEquals(3,consulta.refeicoes().size());
        var almoco=consulta.refeicoes().stream().filter(r->r.tipo().equals("Almoco")).findFirst().orElseThrow();
        assertEquals("14:00",almoco.inicio());assertEquals("15:00",almoco.fimExato());assertEquals("15:30",almoco.fimParcial());
        assertEquals(30,s.registrar(f.usuario.getEmail(),"Almoco").status().pontuacaoTotal());
    }
    @Test void horariosInvertidosEFusoInvalidoSaoRejeitados(){
        var h=new com.example.Database_TCC.gamificacao.HorariosCheckin();
        h.setCafe(new com.example.Database_TCC.gamificacao.HorariosCheckin.Janela(LocalTime.of(8,0),LocalTime.of(7,0),LocalTime.of(8,30)));
        assertThrows(IllegalStateException.class,h::afterPropertiesSet);
        var fuso=new com.example.Database_TCC.gamificacao.HorariosCheckin();fuso.setFusoHorario("FUSO_INEXISTENTE");
        assertThrows(DateTimeException.class,fuso::afterPropertiesSet);
    }
    @Test void repetirCafeParcialOuSemPontosNaoAlteraRegistro(){
        for(String horario:List.of("08:15:00","17:00:00")){
            var f=new Fixture();var s=f.em("2026-11-04T"+horario);var original=s.registrar(f.usuario.getEmail(),"Cafe_da_Manha");
            var repetido=s.registrar(f.usuario.getEmail(),"Cafe_da_Manha");
            assertTrue(repetido.repetido());assertEquals(original.status(),repetido.status());assertEquals(1,f.registros.size());
        }
    }
    @Test void registrosAntigosDeCafeSemPrecisaoNaoDesbloqueiamRelogioRetroativamente(){
        var f=new Fixture();
        for(int i=1;i<=10;i++){
            var h=new HistoricoCheckin(f.usuario,"Cafe_da_Manha",30,"Registrado");h.setDataHoraRegistro(LocalDate.of(2026,10,i).atTime(17,0));f.registros.add(h);
        }
        f.em("2026-11-04T07:30:00").registrar(f.usuario.getEmail(),"Cafe_da_Manha");
        assertFalse(f.nomes().contains("Relógio Suíço"));
    }
}
