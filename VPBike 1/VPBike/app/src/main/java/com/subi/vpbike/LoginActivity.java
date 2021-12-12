package com.subi.vpbike;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.subi.vpbike.adapter.ShowDialog;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private static final String TAG = "testlogin";
    private ImageView fb;
    private TextInputEditText user, pass;
    private Button login;
    private TextView register;
    //Lưu data
    private SharedPreferences pref;
    private ShowDialog dialog;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //Set tiêu đề
        setTitle("ĐĂNG NHẬP");
        init();
        //Tự truyền tk, mk nếu từ đăng ký quá
        if (getIntent().getStringExtra("tk") != null) {
            dialog.show("Đăng ký thành công!");
            user.setText(getIntent().getStringExtra("tk"));
            pass.setText(getIntent().getStringExtra("mk"));
        }

        //Sự kiện khi nhấn đăng ký
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        //Đăng nhập bằng email, password
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.setMessage("Đang đăng nhập...");
                progressDialog.show();
                String tk, mk;
                tk = user.getText().toString();
                mk = pass.getText().toString();
                if (checkValidate(tk, mk)) {
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(tk, mk)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Intent intent;
                                        if (tk.split("@")[1].equals("vpbike.com") || tk.split("@")[1].equals("vpbike.vn") || tk.split("@")[1].equals("vpbike.com.vn")) {
                                            intent = new Intent(LoginActivity.this, MainActivity.class);
                                        } else {
                                            intent = new Intent(LoginActivity.this, MapsActivity.class);
                                        }
                                        progressDialog.dismiss();
                                        startActivity(intent);
                                    } else {
                                        progressDialog.dismiss();
                                        dialog.show("Tài khoản hoặc mật khẩu không chính xác!");
                                    }
                                }
                            });
                }
            }
        });
    }

    private void init() {
        mAuth = FirebaseAuth.getInstance();
        pref = getSharedPreferences("LOGIN", MODE_PRIVATE);
        user = findViewById(R.id.edtTk);
        pass = findViewById(R.id.edtMk);
        login = findViewById(R.id.btnDangNhap);
        register = findViewById(R.id.tvDangKy);
        dialog = new ShowDialog(this);
        progressDialog = new ProgressDialog(this);
        getSupportActionBar().hide();
        ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION},
                0);
    }

    @Override
    public void onBackPressed() {

    }

    private Boolean checkValidate(String tk, String mk) {
        Boolean check = false;
        if (tk.isEmpty() || mk.isEmpty()) {
            dialog.show("Không được để trống!");
            progressDialog.dismiss();
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(tk).matches()) {
            dialog.show("Không đúng dạng email!");
            progressDialog.dismiss();
        } else {
            check = true;
        }
        return check;
    }
}