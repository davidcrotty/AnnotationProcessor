package net.davidcrotty.annotationprocessor;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import net.davidcrotty.viewbinder.NewIntent;

@NewIntent
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
