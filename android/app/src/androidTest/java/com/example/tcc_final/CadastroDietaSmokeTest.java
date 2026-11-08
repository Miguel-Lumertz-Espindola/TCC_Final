package com.example.tcc_final;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.*;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;
import com.google.android.material.chip.ChipGroup;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.io.FileOutputStream;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class CadastroDietaSmokeTest {
    private static class DietaPronta implements IdlingResource {
        ResourceCallback callback;
        @Override public String getName(){return "SugestaoDoNovoCadastroCarregada";}
        @Override public boolean isIdleNow(){
            boolean pronta=false;
            for(Activity a:ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)){
                TextView nome=a.findViewById(R.id.nomePerfilDieta);
                if(nome!=null&&nome.getText().toString().contains("Manutenção"))pronta=true;
            }
            if(pronta&&callback!=null)callback.onTransitionToIdle();return pronta;
        }
        @Override public void registerIdleTransitionCallback(ResourceCallback c){callback=c;}
    }
    private static void texto(Activity a,int id,String texto){((TextView)a.findViewById(id)).setText(texto);}
    private static void capturar(String nome)throws Exception{
        Bitmap b=InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
        if(b!=null){try(FileOutputStream out=new FileOutputStream(new File(InstrumentationRegistry.getInstrumentation().getTargetContext().getCacheDir(),nome))){b.compress(Bitmap.CompressFormat.PNG,100,out);}b.recycle();}
    }
    @Test public void multiplasTagsSobrevivemRotacaoESaoUsadasNaSugestao()throws Exception{
        org.junit.Assume.assumeTrue("Requer servidor isolado de perfis","true".equals(InstrumentationRegistry.getArguments().getString("perfisStub")));
        Context c=InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPreferences prefs=c.getSharedPreferences("SessaoUsuario",Context.MODE_PRIVATE);
        boolean tinha=prefs.contains("email_ativo");String anterior=prefs.getString("email_ativo",null);
        DietaPronta pronto=new DietaPronta();
        try(ActivityScenario<CadastroActivity> tela=ActivityScenario.launch(CadastroActivity.class)){
            tela.onActivity(a->{texto(a,R.id.nome,"Teste Perfil");texto(a,R.id.email,"cadastro-perfis@example.invalid");texto(a,R.id.senha,"senha-teste");texto(a,R.id.confirmacao,"senha-teste");});
            onView(withId(R.id.continuar)).perform(scrollTo(),click());
            tela.onActivity(a->{texto(a,R.id.nascimento,"01/01/1995");texto(a,R.id.altura,"170");texto(a,R.id.peso,"70");});
            onView(withId(R.id.continuar)).perform(scrollTo(),click());
            tela.onActivity(a->{
                ((Spinner)a.findViewById(R.id.objetivo)).setSelection(1);
                ((Spinner)a.findViewById(R.id.atividade)).setSelection(3);
                ((Spinner)a.findViewById(R.id.praticidade)).setSelection(2);
                ((Spinner)a.findViewById(R.id.rigor)).setSelection(2);
                ((Spinner)a.findViewById(R.id.orcamento)).setSelection(2);
            });
            onView(withId(R.id.tagSemLactose)).perform(scrollTo(),click());
            onView(withId(R.id.tagSemGluten)).perform(scrollTo(),click());
            onView(withId(R.id.tagVegano)).perform(scrollTo(),click());
            tela.recreate();
            onView(withId(R.id.tagSemLactose)).check(matches(isChecked()));
            onView(withId(R.id.tagSemGluten)).check(matches(isChecked()));
            onView(withId(R.id.tagVegano)).check(matches(isChecked()));
            onView(withId(R.id.tagVegetariano)).check(matches(isNotChecked()));
            capturar("cadastro-tags.png");
            onView(withId(R.id.continuar)).perform(scrollTo(),click());
            IdlingRegistry.getInstance().register(pronto);
            onView(withId(R.id.nomePerfilDieta)).check(matches(withText("Manutenção · variado")));
            onView(withId(R.id.varianteDieta)).check(matches(withText(containsString("vegetal"))));
            onView(withId(R.id.tagsDieta)).check((v,e)->{if(e!=null)throw e;assertEquals(3,((ChipGroup)v).getChildCount());});
            onView(withId(R.id.avisoDieta)).check(matches(withText(containsString("Exemplo"))));
            capturar("dieta-sugerida.png");
        }finally{
            IdlingRegistry.getInstance().unregister(pronto);
            if(tinha)prefs.edit().putString("email_ativo",anterior).commit();else prefs.edit().remove("email_ativo").commit();
        }
    }
}
