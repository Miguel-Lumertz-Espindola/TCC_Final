package api;
import com.example.tcc_final.dto.RefeicaoDTO;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;
public interface DiarioService {
    @GET("api/refeicoes") Call<List<RefeicaoDTO>> listar(@Query("email") String email, @Query("data") String data);
    @POST("api/refeicoes") Call<RefeicaoDTO> registrar(@Query("email") String email, @Body RefeicaoDTO dados);
    @PUT("api/refeicoes/{id}") Call<RefeicaoDTO> editar(@Query("email") String email, @Path("id") Long id, @Body RefeicaoDTO dados);
    @DELETE("api/refeicoes/{id}") Call<Void> excluir(@Query("email") String email, @Path("id") Long id);
}
