package com.example.tcc_final;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.*;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.hamcrest.Matcher;
import java.io.*;
import java.util.concurrent.atomic.AtomicReference;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class PersonagemGifSmokeTest {
    private static class GifPronto implements IdlingResource {
        MainActivity activity;ResourceCallback callback;
        @Override public String getName(){return "PersonagemGifAnimado";}
        @Override public boolean isIdleNow(){
            ImageView imagem=activity==null?null:activity.findViewById(R.id.imageViewMascote);
            boolean pronto=imagem!=null&&imagem.getDrawable() instanceof GifDrawable&&((GifDrawable)imagem.getDrawable()).isRunning();
            if(pronto&&callback!=null)callback.onTransitionToIdle();return pronto;
        }
        @Override public void registerIdleTransitionCallback(ResourceCallback callback){this.callback=callback;}
    }
    private static ViewAction verificarAnimacao(AtomicReference<GifDrawable> atual){return new ViewAction(){
        @Override public Matcher<View> getConstraints(){return isAssignableFrom(ImageView.class);}
        @Override public String getDescription(){return "Verificar personagem com transparencia e movimento";}
        @Override public void perform(UiController ui,View v){
            Drawable d=((ImageView)v).getDrawable();assertTrue(d instanceof GifDrawable);GifDrawable gif=(GifDrawable)d;atual.set(gif);
            assertTrue(gif.getFrameCount()==13||gif.getFrameCount()==12);assertTrue(gif.isRunning());
            Bitmap quadro=Bitmap.createBitmap(v.getWidth(),v.getHeight(),Bitmap.Config.ARGB_8888);
            Rect limites=new Rect(gif.getBounds());
            gif.setBounds(0,0,quadro.getWidth(),quadro.getHeight());
            gif.draw(new Canvas(quadro));gif.setBounds(limites);
            assertEquals(0,Color.alpha(quadro.getPixel(0,0)));quadro.recycle();
            int antes=gif.getFrameIndex();ui.loopMainThreadForAtLeast(450);assertNotEquals(antes,gif.getFrameIndex());
        }
    };}
    @Test public void gifAnimaPausaAoSairERetomaAoVoltar()throws Exception{
        GifPronto pronto=new GifPronto();AtomicReference<GifDrawable> gif=new AtomicReference<>();
        try(ActivityScenario<MainActivity> tela=ActivityScenario.launch(MainActivity.class)){
            tela.onActivity(a->pronto.activity=a);IdlingRegistry.getInstance().register(pronto);
            onView(withId(R.id.imageViewMascote)).perform(verificarAnimacao(gif));
            IdlingRegistry.getInstance().unregister(pronto);
            onView(withId(R.id.navigation_dashboard)).perform(click());
            tela.onActivity(a->assertFalse(gif.get().isRunning()));
            onView(withId(R.id.navigation_home)).perform(click());IdlingRegistry.getInstance().register(pronto);
            onView(withId(R.id.imageViewMascote)).check(matches(isDisplayed())).perform(verificarAnimacao(gif));
            Bitmap imagem=InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
            if(imagem!=null){File f=new File(InstrumentationRegistry.getInstrumentation().getTargetContext().getCacheDir(),"personagem-gif-home.png");try(FileOutputStream out=new FileOutputStream(f)){imagem.compress(Bitmap.CompressFormat.PNG,100,out);}imagem.recycle();}
        }finally{IdlingRegistry.getInstance().unregister(pronto);}
    }
}
