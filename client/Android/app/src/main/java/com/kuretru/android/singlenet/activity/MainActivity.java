package com.kuretru.android.singlenet.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.kuretru.android.singlenet.R;
import com.kuretru.android.singlenet.api.ApiManager;
import com.kuretru.android.singlenet.entity.ApiResponse;
import com.kuretru.android.singlenet.entity.ServerConfig;
import com.kuretru.android.singlenet.entity.SystemLog;
import com.kuretru.android.singlenet.entity.WanOption;
import com.kuretru.android.singlenet.service.AlarmService;
import com.kuretru.android.singlenet.service.SmsService;
import com.kuretru.android.singlenet.util.ConfigUtils;
import com.kuretru.android.singlenet.util.StringUtils;
import com.kuretru.android.singlenet.util.ToastUtils;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private Button btnSend;
    private Button btnAlarm;
    private Button btnCancel;
    private Button btnUpdate;
    private TextView tvAlarm;
    private EditText etUsername;
    private EditText etPassword;
    private ListView listView;

    private Context context;
    private ServerConfig serverConfig = null;
    private ApiManager apiManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LitePal.initialize(this);
        setContentView(R.layout.activity_main);
        context = this.getApplicationContext();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadServerConfig();
        if (serverConfig != null) {
            apiManager = new ApiManager(serverConfig);
            //apiManager.ping(this.getApplicationContext());
        }
        loadAlarmStatus();
        loadLog();
    }

    public void btnConfig_onClick(View view) {
        Intent intent = new Intent(this, ConfigActivity.class);
        this.startActivity(intent);
    }

    public void btnSend_onClick(View view) {
        Intent intent = new Intent(this, SmsService.class);
        this.startService(intent);
        ToastUtils.show(context, "获取密码短信发送成功！");
    }

    public void btnAlarm_onClick(View view) {
        AlarmService alarmService = new AlarmService(context);
        alarmService.register();
        loadAlarmStatus();
        ToastUtils.show(context, "注册定时任务成功！");
    }

    public void btnCancel_onClick(View view) {
        AlarmService alarmService = new AlarmService(context);
        alarmService.cancel();
        loadAlarmStatus();
        ToastUtils.show(context, "取消定时任务成功！");
    }

    public void btnUpdate_onClick(View view) {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        if (StringUtils.isNullOrEmpty(password)) {
            ToastUtils.show(context, "密码是必填项！");
            return;
        }
        WanOption wanOption = new WanOption(username, password);
        Call<ApiResponse<WanOption>> call = apiManager.setWanOption(wanOption);
        call.enqueue(new Callback<ApiResponse<WanOption>>() {
            @Override
            public void onResponse(Call<ApiResponse<WanOption>> call, Response<ApiResponse<WanOption>> response) {
                if (response.isSuccessful()) {
                    String message = StringUtils.isNullOrEmpty(username) ? "" : "用户名及";
                    ToastUtils.show(context, "更新" + message + "密码成功！");
                    return;
                }
                ApiResponse<String> errorResponse = StringUtils.getErrorResponse(response.errorBody());
                ToastUtils.show(context, errorResponse.getData());
            }

            @Override
            public void onFailure(Call<ApiResponse<WanOption>> call, Throwable t) {
                ToastUtils.show(context, "连接失败：" + t.getMessage());
            }
        });
    }

    private void initView() {
        this.btnSend = findViewById(R.id.btnSend);
        this.btnAlarm = findViewById(R.id.btnAlarm);
        this.btnCancel = findViewById(R.id.btnCancel);
        this.btnUpdate = findViewById(R.id.btnUpdate);
        this.tvAlarm = findViewById(R.id.tvAlarm);
        this.etUsername = findViewById(R.id.etUsername);
        this.etPassword = findViewById(R.id.etPassword);
        this.listView = findViewById(R.id.listView);
    }

    private void loadServerConfig() {
        ServerConfig serverConfig = ConfigUtils.loadServerConfig(this.getApplicationContext());
        if (StringUtils.isNullOrEmpty(serverConfig.getUrl()) || StringUtils.isNullOrEmpty(serverConfig.getSecret())) {
            btnSend.setEnabled(false);
            btnAlarm.setEnabled(false);
            btnCancel.setEnabled(false);
            btnUpdate.setEnabled(false);
            this.serverConfig = null;
            ToastUtils.show(context, "未检测到服务器配置，请配置服务器！");
        } else {
            btnSend.setEnabled(true);
            btnAlarm.setEnabled(true);
            btnCancel.setEnabled(true);
            btnUpdate.setEnabled(true);
            this.serverConfig = serverConfig;
        }
    }

    private void loadAlarmStatus() {
        AlarmService alarmService = new AlarmService(context);
        if (alarmService.isRegistered()) {
            long nextTime = alarmService.getNextTime();
            tvAlarm.setText(("下一次任务时间：" + StringUtils.timestampToString(nextTime)));
        } else {
            tvAlarm.setText("定时任务未注册！！");
        }
    }

    private void loadLog() {
        List<SystemLog> logs = LitePal.limit(10).order("id desc").find(SystemLog.class);
        List<String> data = new ArrayList<>(logs.size());
        for (SystemLog systemLog : logs) {
            data.add(systemLog.getTime() + " " + systemLog.getMessage());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, data);
        listView.setAdapter(adapter);
    }

}
