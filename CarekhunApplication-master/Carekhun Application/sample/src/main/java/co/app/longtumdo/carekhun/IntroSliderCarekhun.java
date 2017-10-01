package co.app.longtumdo.carekhun;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.github.paolorotolo.appintro.AppIntro;

/**
 * Created by TOPPEE on 16/09/2017.
 */

public class IntroSliderCarekhun extends AppIntro {

    @Override
    public void init(Bundle savedInstanceState) {

        addSlide(IntroSliderFragment.newInstance(R.layout.app_intro1));
        addSlide(IntroSliderFragment.newInstance(R.layout.app_intro2));
        addSlide(IntroSliderFragment.newInstance(R.layout.app_intro3));
        addSlide(IntroSliderFragment.newInstance(R.layout.app_intro4));

        showStatusBar(false);
        showSkipButton(false);
        setDepthAnimation();
    }

    @Override
    public void onSkipPressed() {
        Toast.makeText(getApplicationContext(), getString(R.string.app_intro_skip), Toast.LENGTH_SHORT).show();
        Intent i = new Intent(getApplicationContext(), MainActivityLogin.class);
        startActivity(i);
    }

    @Override
    public void onNextPressed() {}

    @Override
    public void onDonePressed() {
        Intent i = new Intent(getApplicationContext(), MainActivityLogin.class);
        startActivity(i);
    }

    @Override
    public void onSlideChanged() {}
}