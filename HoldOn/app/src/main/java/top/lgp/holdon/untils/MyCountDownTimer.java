package top.lgp.holdon.untils;

import android.os.CountDownTimer;
import android.widget.TextView;

import java.util.Locale;

public class MyCountDownTimer extends CountDownTimer {

        public TextView textView;
        public TextView msgView;

        public  double num = 0;

        public MyCountDownTimer(long millisInFuture, long countDownInterval,TextView view,TextView msgView) {
            super(millisInFuture, countDownInterval);
            this.textView = view;
            this.msgView = msgView;
        }


        @Override
        public void onTick(long millisUntilFinished) {
              num++;
              textView.setText(String.format(Locale.CHINESE,"%.3f",num / 40));
        }
 
        @Override
        public void onFinish() {
            num = 0;
            msgView.setText("Loser!!!");
        }
}