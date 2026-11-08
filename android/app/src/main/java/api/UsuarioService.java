package api;

import database.MODEL.Usuario;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;
import database.MODEL.StatusGamificacao;
import retrofit2.http.GET;
import com.example.tcc_final.dto.ConquistaDTO;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import com.example.tcc_final.dto.UsuarioRankingDTO;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface UsuarioService {

    // Rota para cadastrar no PostgreSQL
    @POST("api/usuarios/cadastrar")
    Call<String> cadastrarUsuario(@Body com.example.tcc_final.dto.CadastroRequest usuario);

    // Rota para validar o login no PostgreSQL
    @POST("api/usuarios/login")
    Call<String> fazerLogin(@Body Usuario usuario);

    // rota para enviar o check-in da refeição para o Spring Boot
    @POST("api/gamificacao/checkin")
    Call<String> realizarCheckin(
            @Query("email") String email,
            @Query("tipoRefeicao") String tipoRefeicao
    );

    //Atualizar a Interface do Retrofit
    @GET("api/gamificacao/status")
    Call<StatusGamificacao> buscarStatus(@Query("email") String email);
    @GET("api/gamificacao/conquistas")
    Call<List<ConquistaDTO>> listarConquistas(@Query("email") String email);

    @GET("api/gamificacao/ranking")
    Call<List<UsuarioRankingDTO>> obterRanking();


    @POST("api/gamificacao/checkin?detalhado=true")
    Call<com.example.tcc_final.dto.CheckinRespostaDTO> realizarCheckinDetalhado(@Query("email") String email,@Query("tipoRefeicao") String tipoRefeicao);

    @GET("api/usuarios/dieta")
    Call<com.example.tcc_final.dto.DietaSugeridaDTO> buscarDieta(@Query("email") String email);

    @GET("api/gamificacao/horarios")
    Call<com.example.tcc_final.dto.HorariosCheckinDTO> buscarHorariosCheckin();

    @POST("api/gamificacao/personagem")
    Call<StatusGamificacao> selecionarPersonagem(@Query("email") String email,
            @Body com.example.tcc_final.dto.PersonagemEscolhaDTO escolha);
}
