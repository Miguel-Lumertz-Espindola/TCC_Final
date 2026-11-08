package com.example.tcc_final;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;

@RunWith(AndroidJUnit4.class)
public class DiarioAlimentosSmokeTest {
    @Test public void adicionaEditaERemoveAlimentoSemSalvarRefeicao() {
        Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPreferences prefs=context.getSharedPreferences("SessaoUsuario",Context.MODE_PRIVATE);
        boolean tinhaSessao=prefs.contains("email_ativo");String anterior=prefs.getString("email_ativo",null);
        if(anterior==null||anterior.isEmpty()) prefs.edit().putString("email_ativo","teste-interface@example.invalid").commit();
        try(ActivityScenario<DiarioActivity> tela=ActivityScenario.launch(DiarioActivity.class)) {
            onView(withId(R.id.novaRefeicao)).perform(click());
            onView(withId(R.id.adicionarItemRefeicao)).perform(scrollTo(),click());
            onView(withId(R.id.nomeItem)).perform(replaceText("Arroz de teste"),closeSoftKeyboard());
            onView(withId(R.id.caloriasItem)).perform(scrollTo(),replaceText("0"),closeSoftKeyboard());
            onView(withId(android.R.id.button1)).perform(click());
            onView(withText("Arroz de teste")).perform(scrollTo()).check(matches(isDisplayed()));
            onView(withId(R.id.editarAlimentoForm)).perform(scrollTo(),click());
            onView(withId(R.id.caloriasItem)).perform(scrollTo()).check(matches(withText("0")));
            onView(withId(R.id.caloriasItem)).perform(replaceText("2.5"),closeSoftKeyboard());
            onView(withId(android.R.id.button1)).perform(click());
            onView(withId(R.id.removerAlimentoForm)).perform(scrollTo(),click());
            onView(withText("Arroz de teste")).check(doesNotExist());
            onView(withId(android.R.id.button2)).perform(click());
        }finally{
            if(tinhaSessao)prefs.edit().putString("email_ativo",anterior).commit();
            else prefs.edit().remove("email_ativo").commit();
        }
    }
}
