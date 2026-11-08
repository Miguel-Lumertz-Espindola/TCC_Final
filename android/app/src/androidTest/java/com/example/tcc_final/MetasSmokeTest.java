package com.example.tcc_final;
import android.content.*;
import android.widget.*;
import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.containsString;

@RunWith(AndroidJUnit4.class)
public class MetasSmokeTest {
    private static class FormularioPronto implements IdlingResource {
        MetasActivity activity;ResourceCallback callback;
        @Override public String getName(){return "FormularioMetasCarregado";}
        @Override public boolean isIdleNow(){boolean pronto=activity!=null&&activity.findViewById(R.id.salvarMetas).isEnabled();if(pronto&&callback!=null)callback.onTransitionToIdle();return pronto;}
        @Override public void registerIdleTransitionCallback(ResourceCallback callback){this.callback=callback;}
    }
    // Executar com servidor de teste isolado e argumento metasStub=true; nunca grava metas reais.
    @Test public void objetivoDoCadastroValidacaoESalvamentoComHistorico(){
        org.junit.Assume.assumeTrue("Requer servidor isolado de metas", "true".equals(InstrumentationRegistry.getArguments().getString("metasStub")));
        Context c=InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPreferences prefs=c.getSharedPreferences("SessaoUsuario",Context.MODE_PRIVATE);
        boolean tinha=prefs.contains("email_ativo");String anterior=prefs.getString("email_ativo",null);
        prefs.edit().putString("email_ativo","teste-interface-metas@example.invalid").commit();
        FormularioPronto pronto=new FormularioPronto();
        try(ActivityScenario<MetasActivity> tela=ActivityScenario.launch(MetasActivity.class)){
            tela.onActivity(a->pronto.activity=a);IdlingRegistry.getInstance().register(pronto);
            onView(withId(R.id.objetivoMetas)).check(matches(withSpinnerText(containsString("Emagrecimento"))));
            onView(withId(R.id.caloriasMetas)).perform(scrollTo(),replaceText("0"),closeSoftKeyboard());
            onView(withId(R.id.salvarMetas)).perform(scrollTo(),click());
            onView(withId(R.id.caloriasMetas)).check(matches(hasErrorText(c.getString(R.string.metas_invalid))));
            onView(withId(R.id.caloriasMetas)).perform(scrollTo(),replaceText("2000"),closeSoftKeyboard());
            onView(withId(R.id.salvarMetas)).perform(scrollTo(),click());
            onView(withId(R.id.statusMetas)).perform(scrollTo()).check(matches(withText(containsString("Metas em vigor"))));
            onView(withId(R.id.valoresHistoricoMetas)).perform(scrollTo()).check(matches(withText(containsString("2000 kcal"))));
        }finally{
            IdlingRegistry.getInstance().unregister(pronto);
            if(tinha)prefs.edit().putString("email_ativo",anterior).commit();else prefs.edit().remove("email_ativo").commit();
        }
    }
}
