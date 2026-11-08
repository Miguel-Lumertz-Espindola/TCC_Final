package api;
import com.example.tcc_final.dto.*;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;
public interface MetasService {
    @GET("api/metas") Call<MetasConsultaDTO> consultar(@Query("email") String email,@Query("data") String data);
    @GET("api/metas/historico") Call<List<MetasDTO>> historico(@Query("email") String email);
    @POST("api/metas") Call<MetasDTO> salvar(@Query("email") String email,@Body MetasDTO dados);
}
