package tk.internet.praktikum.foursquare.user;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import tk.internet.praktikum.foursquare.R;

/**
 * Created by Christian on 22.06.2017.
 */

public class DummyProfile extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        addFragment();
    }

    public void addFragment() {
        getFragmentManager().beginTransaction().add(R.id.login_layout, new ProfileFragment()).commit();
    }
}
