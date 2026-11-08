package api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ApiClient {

    // Desenvolvimento: executar conectar-api.ps1 para configurar adb reverse.
    public static final String BASE_URL = "http://127.0.0.1:8080/";

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(ScalarsConverterFactory.create()) // Lê os textos do Spring
                    .addConverterFactory(GsonConverterFactory.create())    // Envia o JSON do Usuario
                    .build();
        }
        return retrofit;
    }
}