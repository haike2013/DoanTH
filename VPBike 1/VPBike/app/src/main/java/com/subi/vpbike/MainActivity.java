package com.subi.vpbike;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.subi.vpbike.adapter.ShowDialog;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity
        implements GoogleMap.OnMyLocationButtonClickListener,
        OnMapReadyCallback{
    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("vpbike");
    private ShowDialog showDialog;
    private FrameLayout done;
    private TextView tu, den, tien, km;
    private Button call, don, trakhach;
    private LinearLayout dichuyen;
    private String fromz, kmz, phonez, pricez, toz, typez;
    private LinearLayout emty;
    private Ringtone r = null;
    private Marker myMarker, khachMarker;
    private GoogleMap mMap;
    private Location location;
    private TextView textView;
    private LatLng latLng,latLngCurrent;
    Polyline line;
    Dialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        //Setup maps
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //?????i ki???m ????n h??ng
        mDatabase.child("booking").child("book/status").addValueEventListener(new ValueEventListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String status = snapshot.getValue(String.class);
                try {
                    switch (status) {
                        case "FINDING": {
                            emty.setVisibility(View.GONE);
                            showDonHang();
                            break;
                        }
                        case "CANCEL":{
                            showDialog.show("????n h??ng ???? b??? hu???");
                            dialog.dismiss();
                        }
                    }
                } catch (Exception e) {
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    private void showDonHang() {
        //Rung chu??ng b??os
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
         dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_nhandon);
        dialog.setCancelable(true);
        Window window = dialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        window.setWindowAnimations(R.style.Animation_Design_BottomSheetDialog);
        TextView from = dialog.findViewById(R.id.tv_diemdon);
        TextView to = dialog.findViewById(R.id.tv_trakhach);
        TextView km = dialog.findViewById(R.id.tv_quangduong);
        TextView tien = dialog.findViewById(R.id.tv_tongtien);

        mDatabase.child("booking")
                .child("book").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                fromz = snapshot.child("from").getValue().toString();
                toz = snapshot.child("to").getValue().toString();
                phonez = snapshot.child("phone").getValue().toString();
                kmz = snapshot.child("km").getValue().toString();
                pricez = snapshot.child("price").getValue().toString();
                Double lat = Double.parseDouble(snapshot.child("lat").getValue(String.class));
                Double lng = Double.parseDouble(snapshot.child("lng").getValue(String.class));
                latLng = new LatLng(lat, lng);
                //Check v??? tr?? c???a kh??ch
                khachMarker = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title("V??? tr?? kh??ch"));
                //Th??m hi???u ???ng zoom ?????n
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                from.setText(fromz);
                to.setText(toz);
                km.setText(kmz);
                tien.setText(pricez);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });


        Button ok = dialog.findViewById(R.id.btn_nhandon);
        Button cancle = dialog.findViewById(R.id.btn_cancle);

        ok.setOnClickListener(view -> {
            dialog.dismiss();
            //D???ng chu??ng
            try {
                r.stop();
            } catch (Exception e) {

            }

            //Set th??ng tin tx ???? nh???n ????n
            mDatabase.child("taixe").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    HashMap<String, String> map = new HashMap<>();
                    for (DataSnapshot x:snapshot.getChildren()){
                        map.put(x.getKey(), x.getValue(String.class));
                    }
                    mDatabase.child("taixe").child("taixe1").setValue(map);
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });

            showDialog.show("C???m ??n b???n ???? nh???n ????n!");
            going();
        });

        cancle.setOnClickListener(view -> {
            emty.setVisibility(View.VISIBLE);
            dialog.dismiss();
            mDatabase.child("booking").child("book/status").setValue("CANCLE");
            mDatabase.child("booking").child("book/status").setValue("FINDING");
        });
        dialog.show();
    }

    //Tr???ng th??i di chuy???n
    private void going() {
        done.setVisibility(View.VISIBLE);
        mDatabase.child("booking").child("book/status").setValue("GOING");

        //V??? ???????ng
        try {
            line = mMap.addPolyline(new PolylineOptions()
                    .add(new LatLng(latLngCurrent.latitude, latLngCurrent.longitude), new LatLng(latLng.latitude, latLng.longitude))
                    .width(5)
                    .color(Color.RED));
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Set value
        tu.setText(fromz);
        den.setText(toz);
        tien.setText(pricez);
        km.setText(kmz);

        //G???i kh??ch
        call.setOnClickListener(v -> {
            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
            dialIntent.setData(Uri.parse("tel:" + phonez));
            startActivity(dialIntent);
        });

        don.setOnClickListener(v -> {
            mDatabase.child("booking").child("book/status").setValue("DOING");
            trakhach.setVisibility(View.VISIBLE);
            dichuyen.setVisibility(View.GONE);
        });

        trakhach.setOnClickListener(v -> {
            mDatabase.child("booking").child("book/status").setValue("DONE").addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<Void> task) {
                    showDialog.show("K???t th??c chuy???n ??i th??nh c??ng!");
                    done.setVisibility(View.GONE);
                    emty.setVisibility(View.VISIBLE);
                    line.remove();//xong chuy???n s??? b??? ???????ng d???n
                }
            });
        });

    }

    private void init() {
        textView = findViewById(R.id.tv_current_address);
        showDialog = new ShowDialog(this);
        done = findViewById(R.id.frame_done);
        tu = done.findViewById(R.id.dtv_diemdon);
        den = done.findViewById(R.id.dtv_trakhach);
        tien = done.findViewById(R.id.dtv_tongtien);
        km = done.findViewById(R.id.dtv_quangduong);
        call = done.findViewById(R.id.btn_call_done);
        don = done.findViewById(R.id.btn_dadon);
        trakhach = done.findViewById(R.id.btn_hoanthanh);
        dichuyen = done.findViewById(R.id.dichuyen);
        emty = findViewById(R.id.emty);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_logout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                //x??a h???t d??? li???u ???? l??u tk, mk
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, LoginActivity.class));
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onMyLocationButtonClick() {
        location = mMap.getMyLocation();
        markerGPS(new LatLng(location.getLatitude(), location.getLongitude()),"V??? tr?? c???a b???n");
        return false;
    }

    @Override
    public void onMapReady(@NonNull @NotNull GoogleMap googleMap) {
        mMap = googleMap;
        //Check quy???n
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(this);
            LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                markerGPS(new LatLng(location.getLatitude(), location.getLongitude()), "V??? tr?? c???a b???n");
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    0);
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private void markerGPS(LatLng latLng, String text) {

        if (myMarker != null) {
            myMarker.remove();
        }
        latLngCurrent = latLng;

//        currentLatLng = latLng;
        myMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(text));
        //Th??m hi???u ???ng zoom ?????n
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
        //Set ?????a ch??? hi???n t???i
    String    from_address = getCompleteAddressString(latLng);
        textView.setText(from_address);
    }

    //Chuy???n ?????i latlng sang ?????a ch???
    private String getCompleteAddressString(LatLng latLng) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
            } else {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strAdd;
    }}