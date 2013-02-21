package com.trialanderrorapps.landthatplane;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class MenuActivity extends Activity implements OnClickListener {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.menu);
    ((TextView)findViewById(R.id.bStart)).setOnClickListener(this);
    ((TextView)findViewById(R.id.bQuit)).setOnClickListener(this);
    ((TextView)findViewById(R.id.bResume)).setOnClickListener(this);
    TextView tv = (TextView)findViewById(R.id.bHighScore);
    tv.setText(String.format("HIGH SCORE: %02d", 0));
    tv.setEnabled(false);
  }

  @Override
  public void onClick(View view) {
    switch(view.getId()) {
      case R.id.bQuit:
        finish();
        break;
      case R.id.bStart:
        Intent intent = new Intent(this, GameActivity.class);
        startActivityForResult(intent, 1);
        break;
      case R.id.bResume:
        break;
    }
  }
}
