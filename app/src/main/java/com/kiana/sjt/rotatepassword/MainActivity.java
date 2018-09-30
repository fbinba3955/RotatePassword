package com.kiana.sjt.rotatepassword;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.kiana.sjt.library.RotatePassword;

public class MainActivity extends Activity {

    RotatePassword rotatePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rotatePassword = findViewById(R.id.rp);
        rotatePassword.setCorrectNumber(5,10, true);
        rotatePassword.setOnCircleChangedListener(new RotatePassword.OnCircleChangedListener() {
            @Override
            public void onChanged(int outerNumber, int innerNumber) {

            }

            @Override
            public void isCorrect(int outerNumber, int innerNumber) {
                Toast.makeText(MainActivity.this, "密码正确："+outerNumber+":"+innerNumber, Toast.LENGTH_LONG).show();
                Log.d("RotatePassword", "密码正确："+outerNumber+":"+innerNumber);
            }
        });
    }
}
