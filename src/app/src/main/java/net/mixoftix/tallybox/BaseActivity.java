package net.mixoftix.tallybox;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context base) {
        String language = LocaleHelper.getSavedLanguage(base);
        super.attachBaseContext(LocaleHelper.setLocale(base, language));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Force layout direction
        // forceLayoutDirection();

        // Extra force after language change
        getWindow().getDecorView().postDelayed(() -> {
            forceLayoutDirection();
        }, 500);

    }

    private void forceLayoutDirection() {
        Configuration config = getResources().getConfiguration();
        int direction = config.getLayoutDirection();

        // Apply to window
        getWindow().getDecorView().setLayoutDirection(direction);

        // Apply to root view (important for Toolbar, menus, etc.)
        if (getWindow().getDecorView().getRootView() != null) {
            getWindow().getDecorView().getRootView().setLayoutDirection(direction);
        }

        // Force Toolbar / ActionBar direction
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Re-apply direction when returning to activity
        forceLayoutDirection();
    }

    /**
     * Call this after language change before restarting activity
     */
    public void recreateWithDirection() {
        forceLayoutDirection();
        recreate();
    }
}