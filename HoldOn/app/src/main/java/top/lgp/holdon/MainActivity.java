package top.lgp.holdon;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import top.lgp.holdon.untils.MyCountDownTimer;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;

    private MyCountDownTimer timer;
    private final long TIME = 60 * 1000L;
    private final long INTERVAL = 25L;

    public  int clickNum = 0;

    public void changState(View view){

        TextView timeView = (TextView)findViewById(R.id.show_time);
        TextView msgView = (TextView)findViewById(R.id.show_msg);
        msgView.setVisibility(View.GONE);
        msgView.setText("SO GOOD!");
        if(timer == null){
            timer = new MyCountDownTimer(TIME,INTERVAL,timeView,msgView);
        }
        clickNum ++;
        if(clickNum == 1){
            timer.start();
            view.setBackgroundResource(R.drawable.circular_layout_two);
        }else if(clickNum == 2){
            cancelTimer();
            msgView.setVisibility(View.VISIBLE);
            double d = Double.parseDouble(timeView.getText().toString());
            if( d > 10.2 || d < 9.8){
                msgView.setText("Loser!!!");
            }
            view.setBackgroundResource(R.drawable.circular_layout_three);
        }else if(clickNum == 3){
            timeView.setText(R.string.initTime);
            view.setBackgroundResource(R.drawable.circular_layout);
            clickNum = 0;
        }

    }

    /**
     * 取消倒计时
     */
    private void cancelTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTimer();
    }



    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            findViewById(R.id.tenSecond).setVisibility(View.GONE);
            findViewById(R.id.show_time).setVisibility(View.GONE);
            findViewById(R.id.show_msg).setVisibility(View.GONE);
            findViewById(R.id.audio_layout).setVisibility(View.GONE);
            findViewById(R.id.face_layout).setVisibility(View.GONE);
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    findViewById(R.id.tenSecond).setVisibility(View.VISIBLE);
                    findViewById(R.id.show_time).setVisibility(View.VISIBLE);
                    findViewById(R.id.show_msg).setVisibility(View.VISIBLE);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    findViewById(R.id.audio_layout).setVisibility(View.VISIBLE);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    findViewById(R.id.face_layout).setVisibility(View.VISIBLE);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

}
