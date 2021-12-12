package com.subi.vpbike;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.subi.vpbike.adapter.ShowDialog;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText user, pass, pass2;
    private Button register, close;
    private ShowDialog dialog;
    private FirebaseAuth mAuth;
    private String TAG = "RegisterActivity";
    private ProgressDialog progressDialog;
    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("vpbike");
    Map<String, String> map;
    Boolean isTaiXe = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        init();

        //Khi nhấn nút đóng
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //Khi ấn đăng ký
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tk, mk, mk2;
                tk = user.getText().toString();
                mk = pass.getText().toString();
                mk2 = pass2.getText().toString();

                if (checkValidate(tk, mk, mk2)) {
                    if (tk.split("@")[1].equals("vpbike.com") || tk.split("@")[1].equals("vpbike.vn") || tk.split("@")[1].equals("vpbike.com.vn")) {
                        regTaiXe(tk, mk);
                    } else {
                        reg(tk, mk);
                    }
                }

            }
        });
    }

    private Boolean checkValidate(String tk, String mk, String mk2) {
        Boolean check = false;
        if (tk.isEmpty() || mk.isEmpty() || mk2.isEmpty()) {
            dialog.show("Không được để trống!");
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(tk).matches()) {
            dialog.show("Không đúng dạng email!");
        } else if (!mk.equals(mk2)) {
            dialog.show("Mật khẩu không khớp nhau!");
        } else {
            check = true;
        }

        return check;
    }

    private void init() {
        user = findViewById(R.id.edtTkDK);
        pass = findViewById(R.id.edtMkDK);
        pass2 = findViewById(R.id.edtMkDK2);
        register = findViewById(R.id.btnDangKy);
        close = findViewById(R.id.btnNhapLai);
        dialog = new ShowDialog(this);
        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        getSupportActionBar().hide();
    }

    private void regTaiXe(String tk, String mk) {
        isTaiXe = true;
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.activity_register_tx);
        Window window = dialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        window.setWindowAnimations(R.style.Animation_Design_BottomSheetDialog);
        TextView ten = dialog.findViewById(R.id.edt_ho_ten);
        TextView xe = dialog.findViewById(R.id.edt_ten_xe);
        TextView bienso = dialog.findViewById(R.id.edt_bs_xe);
        TextView sdt = dialog.findViewById(R.id.edt_numphone);

        Button ok = dialog.findViewById(R.id.btnDangKyx);

        ok.setOnClickListener(view -> {
            //Check
            String tenx, xex, bsx, sdtx;
            tenx = ten.getText().toString();
            xex = xe.getText().toString();
            bsx = bienso.getText().toString();
            sdtx = sdt.getText().toString();
            if (tenx.isEmpty() || xex.isEmpty() || bsx.isEmpty() || sdtx.isEmpty()) {
                this.dialog.show("Không được để trống!");
            } else {
                 map = new HashMap<>();
                map.put("bienso", bsx);
                map.put("loaixe", "Xe tay ga");
                map.put("name", tenx);
                map.put("tenxe", xex);
                map.put("phone", sdtx);
                mDatabase.child("taixe").child("taixe1").setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        reg(tk, mk);
                        dialog.dismiss();
                    }
                });
            }
        });
        dialog.show();
    }

    private void reg(String tk, String mk) {
        progressDialog.setMessage("Đang đăng ký...");
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(tk, mk)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if (isTaiXe){
                                mDatabase.child("taixe").child(FirebaseAuth.getInstance().getUid()).setValue(map);
                            }
                            progressDialog.dismiss();
                            //Truyền mk và tk đi khi đăng ký thành công
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            intent.putExtra("tk", tk);
                            intent.putExtra("mk", mk);
                            startActivity(intent);
                        } else {
                            progressDialog.dismiss();
                            Log.d(TAG + "//", task.getException().toString());
                            dialog.show("Tài khoản đã tồn tại!");
                        }
                    }
                });
    }
}