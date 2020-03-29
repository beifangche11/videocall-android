package com.easemob.videocall.ui;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.videocall.DemoApplication;
import com.easemob.videocall.R;
import com.easemob.videocall.utils.PreferenceManager;
import com.hyphenate.util.EMLog;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyInfoActivity extends Activity implements View.OnClickListener {
    private  final String TAG = this.getClass().getSimpleName();

    ImageView imageView;
    TextView IDView;
    String urlparm;
    String url;
    String nickName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_info);


        imageView = (ImageView)findViewById(R.id.headImage_Info);
        IDView = (TextView)findViewById(R.id.nickname_Info);

        nickName = PreferenceManager.getInstance().getCurrentUserNick();
        if(nickName != null){
            IDView.setText(nickName);
        }else{
            IDView.setText("未设置昵称");
        }

       RelativeLayout btn_set_headImage = findViewById(R.id.btn_set_headImage);
       btn_set_headImage.setOnClickListener(this);
        RelativeLayout btn_set_nickname = findViewById(R.id.btn_set_nickname);
       btn_set_nickname.setOnClickListener(this);

        //load image
        urlparm  = PreferenceManager.getInstance().getCurrentUserAvatar();
        url = DemoApplication.baseurl;
        url = url + urlparm;
        loadImage();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_set_headImage:
                Intent intent1 = new Intent(MyInfoActivity.this, SetHeadImageActivity.class);
                startActivityForResult(intent1, 1);
                break;
            case R.id.btn_set_nickname:
                //Intent intent = new Intent(InfoActivity.this, SetNickNameActivity.class);
                //startActivityForResult(intent, 2);
                showModifyNickNameDialog();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 2 && resultCode == RESULT_OK) {
            if(data != null) {
                String name = data.getStringExtra("nickName");
                IDView.setText(name);
            }
        }else if((requestCode == 1 && resultCode == RESULT_OK)) {
            if(data != null) {
                String headImage = data.getStringExtra("headImage");
                url = DemoApplication.baseurl;
                url = url + headImage;
                loadImage();
            }
        }
    }

    private void showModifyNickNameDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MyInfoActivity.this);
        final AlertDialog dialog = builder.create();
        View dialogView = View.inflate(MyInfoActivity.this, R.layout.activity_nickname_editshow, null);
        dialog.setView(dialogView);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
        wmlp.gravity = Gravity.CENTER | Gravity.CENTER;
        dialog.show();

        final Button btn_ok = dialogView.findViewById(R.id.btn_ok_nickname);
        final Button btn_cancel = dialogView.findViewById(R.id.btn_cancel_nickname);
        final EditText editText = dialogView.findViewById(R.id.nickname_text);
        editText.setText(nickName);

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                nickName = editText.getText().toString().trim();
                if(nickName.length() == 0){
                    Toast.makeText(getApplicationContext(), "昵称不允许为空!",
                            Toast.LENGTH_SHORT).show();
                }else {
                    dialog.dismiss();
                    EMLog.e(TAG,"setting nickName  succeed  currentNickname:" + nickName);
                    PreferenceManager.getInstance().setCurrentUserNick(nickName);
                    IDView.setText(nickName);
                }
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                //主播已满不加入会议
                EMLog.e(TAG, "cancel setting nickename");
            }
        });
    }


    public void onMyInfoback(View view){
        String nickName = IDView.getText().toString().trim();
        getIntent().putExtra("nickName", nickName);
        getIntent().putExtra("headImage", PreferenceManager.getInstance().getCurrentUserAvatar());
        setResult(RESULT_OK, getIntent());
        finish();
    }

    /**
     * 获取网落图片资源
     * @return
     */
    private void loadImage() {
        new AsyncTask<String, Void, Bitmap>() {
            //该方法运行在后台线程中，因此不能在该线程中更新UI，UI线程为主线程
            @Override
            protected Bitmap doInBackground(String... params) {
                Bitmap bitmap = null;
                try {
                    String url = params[0];
                    URL HttpURL = new URL(url);
                    HttpURLConnection conn = (HttpURLConnection) HttpURL.openConnection();
                    conn.setDoInput(true);
                    conn.connect();
                    InputStream is = conn.getInputStream();
                    bitmap = BitmapFactory.decodeStream(is);
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return bitmap;
            }

            //在doInBackground 执行完成后，onPostExecute 方法将被UI 线程调用，
            // 后台的计算结果将通过该方法传递到UI线程，并且在界面上展示给用户.
            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if(bitmap != null){
                    imageView.setImageBitmap(bitmap);
                }
            }
        }.execute(url);
    }
}