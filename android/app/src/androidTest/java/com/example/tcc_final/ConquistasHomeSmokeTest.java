package com.example.tcc_final;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.widget.*;
import android.view.View;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.*;
import com.example.tcc_final.ui.home.HomeViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.hamcrest.Matcher;
import java.io.*;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ConquistasHomeSmokeTest {
    private static class TelaPronta implements IdlingResource {
        MainActivity activity;HomeViewModel modelo;ResourceCallback callback;
        @Override public String getName(){return "HomeOuConquistasCarregadas";}
        @Override public boolean isIdleNow(){
            boolean pronto=false;
            if(activity!=null){BottomNavigationView nav=activity.findViewById(R.id.nav_view);
                if(nav.getSelectedItemId()==R.id.navigation_home)pronto=modelo.getStatus().getValue()!=null&&!Boolean.TRUE.equals(modelo.getCarregando().getValue())&&modelo.getEmAndamento().getValue()==null;
                else if(nav.getSelectedItemId()==R.id.navigation_dashboard){RecyclerView lista=activity.findViewById(R.id.recyclerViewConquistas);pronto=lista!=null&&lista.getAdapter()!=null&&lista.getAdapter().getItemCount()==4;}
                else pronto=true;
            }
            if(pronto&&callback!=null)callback.onTransitionToIdle();return pronto;
        }
        @Override public void registerIdleTransitionCallback(ResourceCallback callback){this.callback=callback;}
    }
    private static ViewAction alimentarPrimeira(){return new ViewAction(){
        @Override public Matcher<View> getConstraints(){return isAssignableFrom(RecyclerView.class);}
        @Override public String getDescription(){return "Registrar cafe da manha";}
        @Override public void perform(UiController ui,View v){RecyclerView lista=(RecyclerView)v;lista.scrollToPosition(0);ui.loopMainThreadUntilIdle();RecyclerView.ViewHolder h=lista.findViewHolderForAdapterPosition(0);assertNotNull(h);h.itemView.findViewById(R.id.btnAlimentar).performClick();ui.loopMainThreadUntilIdle();}
    };}
    private static void registrado(){onView(withId(R.id.recyclerViewAlimentos)).check((v,erro)->{RecyclerView lista=(RecyclerView)v;RecyclerView.ViewHolder h=lista.findViewHolderForAdapterPosition(0);assertNotNull(h);Button b=h.itemView.findViewById(R.id.btnAlimentar);assertFalse(b.isEnabled());assertEquals("Alimentado",b.getText().toString());});}
    private static void capturar(String nome)throws Exception{
        Bitmap imagem=InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
        if(imagem!=null){File f=new File(InstrumentationRegistry.getInstrumentation().getTargetContext().getCacheDir(),nome);try(FileOutputStream out=new FileOutputStream(f)){imagem.compress(Bitmap.CompressFormat.PNG,100,out);}imagem.recycle();}
    }
    private static void renderizarIcones()throws Exception{
        Context c=InstrumentationRegistry.getInstrumentation().getTargetContext();Bitmap b=Bitmap.createBitmap(640,190,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(b);canvas.drawColor(Color.BLACK);
        int[] ids={R.drawable.ic_conquista_primeiro_passo,R.drawable.ic_conquista_chama_viva,R.drawable.ic_conquista_mestre_almoco,R.drawable.ic_conquista_relogio_suico};
        for(int i=0;i<ids.length;i++){Drawable d=c.getDrawable(ids[i]);d.setBounds(i*160+16,16,i*160+144,144);d.draw(canvas);}
        try(FileOutputStream out=new FileOutputStream(new File(c.getCacheDir(),"icones-conquistas.png"))){b.compress(Bitmap.CompressFormat.PNG,100,out);}b.recycle();
    }
    @Test public void detalhesPontosImediatosEDiasEstaveisAoNavegar()throws Exception{
        org.junit.Assume.assumeTrue("Requer servidor isolado de gamificacao","true".equals(InstrumentationRegistry.getArguments().getString("gamificacaoStub")));
        Context c=InstrumentationRegistry.getInstrumentation().getTargetContext();SharedPreferences prefs=c.getSharedPreferences("SessaoUsuario",Context.MODE_PRIVATE);
        boolean tinha=prefs.contains("email_ativo");String anterior=prefs.getString("email_ativo",null);prefs.edit().putString("email_ativo","teste-home@example.invalid").commit();TelaPronta pronto=new TelaPronta();
        try(ActivityScenario<MainActivity> tela=ActivityScenario.launch(MainActivity.class)){
            tela.onActivity(a->{pronto.activity=a;pronto.modelo=new ViewModelProvider(a).get(HomeViewModel.class);});IdlingRegistry.getInstance().register(pronto);
            onView(withId(R.id.textPontuacao)).check(matches(withText(c.getString(R.string.home_pontos,0))));
            onView(withId(R.id.recyclerViewAlimentos)).perform(alimentarPrimeira());
            onView(withId(R.id.textPontuacao)).check(matches(withText(c.getString(R.string.home_pontos,30))));
            onView(withId(R.id.textOfensiva)).check(matches(withText(c.getResources().getQuantityString(R.plurals.home_dias,1,1))));registrado();
            onView(withId(R.id.navigation_dashboard)).perform(click());
            onView(withText("Relógio Suíço")).perform(click());
            onView(withId(R.id.criterioDetalheConquista)).check(matches(withText(containsString("10 check-ins"))));
            onView(withId(R.id.statusDetalheConquista)).check(matches(withText(R.string.conquista_locked)));capturar("detalhes-conquista.png");renderizarIcones();
            onView(withText(R.string.conquista_close)).perform(click());
            onView(withId(R.id.navigation_home)).perform(click());
            onView(withId(R.id.textPontuacao)).check(matches(withText(c.getString(R.string.home_pontos,30))));
            onView(withId(R.id.textOfensiva)).check(matches(withText(c.getResources().getQuantityString(R.plurals.home_dias,1,1))));registrado();
        }finally{IdlingRegistry.getInstance().unregister(pronto);if(tinha)prefs.edit().putString("email_ativo",anterior).commit();else prefs.edit().remove("email_ativo").commit();}
    }
}
