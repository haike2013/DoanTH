package com.subi.vpbike;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.internal.Constants;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;
import com.subi.vpbike.adapter.ShowDialog;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapsActivity extends AppCompatActivity
        implements GoogleMap.OnMyLocationButtonClickListener,
        OnMapReadyCallback {
    public static final String TAG = "MapsActivityTest";
    private TextView textView, moto, pkl, scooter, car, num_km, title_tx, huydon;
    private Marker myMarker;
    private GoogleMap mMap;
    private Button book, confirm, cash, momo, b_call;
    private FrameLayout frame_book, frame_pay, frame_booked;
    private Location location;
    private TextInputEditText address_to, phone;
    private ShowDialog showDialog;
    private LatLng currentLatLng, toLatLng;
    private Double km = 0.0;
    private String shoow_price, show_km, uid, from_address, to_address, price, type_bike, km_add, numberphone;
    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("vpbike");
    private RadioGroup radioGroup;
    private CardView cv1, cv2, cv3, cv4;
    private RadioButton rcv1, rcv2, rcv3, rcv4;
    private ProgressDialog progressDialog;
    private TextView b_tx, b_bienso, b_tenxe, b_loaixe, b_km, b_price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("VPBIKE");
        setContentView(R.layout.activity_maps);
        FirebaseUser cur = FirebaseAuth.getInstance().getCurrentUser();
        if (cur != null) {
            uid = cur.getUid();
        } else {
            //Check User, if not exist -> log out
//           finish();
        }

        //Khai b??o id
        init();

        //Tr???ng th??i c?? t??i x???
        haveTaiXe();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //S??? ki???n click
        book.setOnClickListener(v -> {
            book.setVisibility(View.GONE);
            frame_book.setVisibility(View.VISIBLE);
        });

        confirm.setOnClickListener(v -> {
            String to = address_to.getText().toString();
            String num = phone.getText().toString();
            if (to.isEmpty() || num.isEmpty()) {
                showDialog.show("Kh??ng ???????c ????? tr???ng!");
            } else if (to.length() < 10) {
                showDialog.show("Vui l??ng nh???p ?????y ????? ?????a ch???!");
            } else {
                numberphone = num;
                toLatLng = getLatLangFromAddress(to);
                to_address = to;
                Log.i(TAG, "success: " + toLatLng);
                frame_book.setVisibility(View.GONE);

                //Chuy???n ?????i ????? hi???n lo???i xe v?? thanh to??n
                showTypeBike();
            }
        });

    }

    private void haveTaiXe() {
        mDatabase.child("booking").child("book/status").addValueEventListener(new ValueEventListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String status = snapshot.getValue(String.class);
                try {
                    switch (status) {

                        case "GOING": {
                            progressDialog.dismiss();
                            showDialog.show("???? t??m th???y t??i x???!");
                            frame_booked.setVisibility(View.VISIBLE);
                            b_call.setFocusable(false);
                            //setValue cho chuy???n ??i
                            setValueDangden();
                            break;
                        }

                        case "DOING": {
//                            title_tx.setText("");
                            b_call.setText("??ANG DI CHUY???N");
                            b_call.setFocusable(false);
                            break;
                        }

                        case "DONE":
                            showDialog.show("Chuy???n ??i ???? ho??n t???t, c???m ??n b???n ???? s??? d???ng VPBIKE c???a ch??ng t??i!");
                            mDatabase.child("booking").child("book/status").setValue("EMTY");
                            break;

                        case "CANCEL": {
                            mDatabase.child("booking").child("book/status").setValue("EMTY");
                            break;
                        }

                        case "EMTY": {
                            frame_booked.setVisibility(View.GONE);
                            textView.setVisibility(View.VISIBLE);
                            mMap.setMyLocationEnabled(true);
                            book.setVisibility(View.VISIBLE);
                            break;
                        }
                    }
                } catch (Exception e) {
                    Log.d(TAG, e.getMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    private void setValueDangden() {
        mDatabase.child("taixe").child("taixe1").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                try {
                    b_tx.setText(snapshot.child("name").getValue().toString());
                    b_tenxe.setText(snapshot.child("tenxe").getValue().toString());
                    b_loaixe.setText(snapshot.child("loaixe").getValue().toString());
                    b_bienso.setText(snapshot.child("bienso").getValue().toString());
                    b_price.setText(price);
                    b_km.setText(km_add);

                    //Set n??t g???i
                    b_call.setOnClickListener(v -> {
                        String phone = snapshot.child("phone").getValue().toString();
                        Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                        dialIntent.setData(Uri.parse("tel:" + phone));
                        startActivity(dialIntent);
                    });
                } catch (Exception e) {
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }


    @SuppressLint("MissingPermission")
    private void showTypeBike() {
        textView.setVisibility(View.GONE);
        mMap.setMyLocationEnabled(false);
        //T??nh km
        km = distanceBetweenKm(currentLatLng, toLatLng);
        Log.i(TAG, "Kho???ng c??ch: " + km);
        frame_pay.setVisibility(View.VISIBLE);
        km_add = km + " KM";
        //Set th??ng tin km v?? t??nh ti???n
        num_km.setText(km_add);
        Double payMoto, payPkl, payScooter, payCar;
        //t??nh ti???n
        payMoto = km * 4500;
        payPkl = km * 6000;
        payScooter = km * 5000;
        payCar = km * 10000;
        DecimalFormat fm = new DecimalFormat("#,###");
        moto.setText(fm.format(payMoto) + " VN??");
        pkl.setText(fm.format(payPkl) + " VN??");
        scooter.setText(fm.format(payScooter) + " VN??");
        car.setText(fm.format(payCar) + " VN??");


        //Set s??? ki???n khi b???m h???y -> Tr??? v??? m??n h??nh ch??nh

        //H???y ????n h??ng
        huydon.setOnClickListener(v -> {
            textView.setVisibility(View.VISIBLE);
            mMap.setMyLocationEnabled(true);
            frame_pay.setVisibility(View.GONE);
            book.setVisibility(View.VISIBLE);
        });
        //Ch???n m???t ?????nh tr?????c
        type_bike = "Xe m??y";
        price = fm.format(payMoto) + " VN??";
        //S??? ki???n khi click ch???n
        cv1.setOnClickListener(v -> {
            type_bike = "Xe m??y";
            price = fm.format(payMoto) + " VN??";
            rcv1.setChecked(true);
            rcv2.setChecked(false);
            rcv3.setChecked(false);
            rcv4.setChecked(false);
        });

        cv2.setOnClickListener(v -> {
            type_bike = "Xe tay ga";
            price = fm.format(payScooter) + " VN??";
            rcv1.setChecked(false);
            rcv2.setChecked(true);
            rcv3.setChecked(false);
            rcv4.setChecked(false);
        });

        cv3.setOnClickListener(v -> {
            type_bike = "Xe PKL";
            price = fm.format(payPkl) + " VN??";
            rcv1.setChecked(false);
            rcv2.setChecked(false);
            rcv3.setChecked(true);
            rcv4.setChecked(false);
        });

        cv4.setOnClickListener(v -> {
            type_bike = "Xe h??i";
            price = fm.format(payCar) + " VN??";
            rcv1.setChecked(false);
            rcv2.setChecked(false);
            rcv3.setChecked(false);
            rcv4.setChecked(true);
        });

        //Set s??? ki???n khi ?????t
        cash.setOnClickListener(v -> {
            //Th??m v??o map ????? ?????y d??? li???u
            Map<String, String> booking_info = new HashMap<>();
            booking_info.put("from", from_address);
            booking_info.put("to", to_address);
            booking_info.put("km", km_add);
            booking_info.put("type", type_bike);
            booking_info.put("price", price);
            booking_info.put("phone", numberphone);
            booking_info.put("status", "FINDING");
            booking_info.put("lat", toLatLng.latitude + "");
            booking_info.put("lng", toLatLng.longitude + "");

            show_km = km_add;
            shoow_price = price;
            mDatabase.child("booking")
                    .child("book").setValue(booking_info).addOnCompleteListener(task ->
            {
                frame_pay.setVisibility(View.GONE);
                progressDialog.setCancelable(false);
                progressDialog.setMessage("??ang t??m t??i x???...");
                progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Hu???", (dialog, which) -> {
                    mDatabase.child("booking").child("book/status").setValue("CANCEL");
                    progressDialog.dismiss();
                });
                //T??m t??i x???
                progressDialog.show();
            })
                    .addOnFailureListener(e -> {
                        textView.setVisibility(View.VISIBLE);
                        mMap.setMyLocationEnabled(true);
                        book.setVisibility(View.VISIBLE);
                        frame_pay.setVisibility(View.GONE);
                        showDialog.show("C?? v???n ????? x???y ra, vui l??ng th??? l???i sau!");
                    });
        });

        momo.setOnClickListener(v -> {
            progressDialog.setMessage("??ang thanh to??n v???i momo...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            new Handler().postDelayed(() -> {
                progressDialog.dismiss();
                Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.item_momo);
                dialog.setCancelable(true);
                Window window = dialog.getWindow();
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                if (dialog != null && dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                }
                window.setWindowAnimations(R.style.Animation_Design_BottomSheetDialog);
                TextView textInfo = dialog.findViewById(R.id.tv_money);
                TextView home = dialog.findViewById(R.id.tv_home);
                textInfo.setText("B???n ???? thanh to??n th??nh c??ng s??? ti???n " + price + " cho VPBike. \nVui l??ng v??? m??n h??nh ch??nh ????? t???n h?????ng chuy???n ??i!");

                home.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        //Th??m v??o map ????? ?????y d??? li???u
                        Map<String, String> booking_info = new HashMap<>();
                        booking_info.put("from", from_address);
                        booking_info.put("to", to_address);
                        booking_info.put("km", km_add);
                        booking_info.put("type", type_bike);
                        booking_info.put("price", price);
                        booking_info.put("phone", numberphone);
                        booking_info.put("status", "FINDING");
                        booking_info.put("lat", toLatLng.latitude + "");
                        booking_info.put("lng", toLatLng.longitude + "");
                        show_km = km_add;
                        shoow_price = price;
                        mDatabase.child("booking")
                                .child("book").setValue(booking_info).addOnCompleteListener(task ->
                        {
                            frame_pay.setVisibility(View.GONE);
                            progressDialog.setCancelable(false);
                            progressDialog.setMessage("??ang t??m t??i x???...");
                            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Hu???", (dialog, which) -> {
                                mDatabase.child("booking").child("book/status").child("CANCEL");
                                progressDialog.dismiss();
                            });
                            //T??m t??i x???
                            progressDialog.show();
                        })
                                .addOnFailureListener(e -> {
                                    textView.setVisibility(View.VISIBLE);
                                    mMap.setMyLocationEnabled(true);
                                    book.setVisibility(View.VISIBLE);
                                    frame_pay.setVisibility(View.GONE);
                                    showDialog.show("C?? v???n ????? x???y ra, vui longf th??? l???i sau!");
                                });
                    }
                });
                dialog.show();
            }, 2000);
        });

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
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
                markerGPS(new LatLng(location.getLatitude(), location.getLongitude()));
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
                Log.w(TAG, strReturnedAddress.toString());
            } else {
                Log.w(TAG, "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, "Canont get Address!");
        }
        return strAdd;
    }

    @Override
    public boolean onMyLocationButtonClick() {
        location = mMap.getMyLocation();
        markerGPS(new LatLng(location.getLatitude(), location.getLongitude()));
        return false;
    }


    private void markerGPS(LatLng latLng) {
        if (myMarker != null) {
            myMarker.remove();
        }
        currentLatLng = latLng;
        myMarker = mMap.addMarker(new MarkerOptions()
                .position(currentLatLng)
                .title("V??? tr?? c???a b???n"));
        //Th??m hi???u ???ng zoom ?????n
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16));
        //Set ?????a ch??? hi???n t???i
        from_address = getCompleteAddressString(latLng);
        textView.setText(from_address);
    }


    private void init() {
        progressDialog = new ProgressDialog(this);
        textView = findViewById(R.id.tv_current_address);
        book = findViewById(R.id.btn_book);
        frame_book = findViewById(R.id.frame_book);
        frame_pay = findViewById(R.id.frame_pay);
        frame_booked = findViewById(R.id.frame_booked);

        //Layout choose address
        LinearLayout b = findViewById(R.id.bottomSheetContainer);
        confirm = b.findViewById(R.id.btn_confirm);
        address_to = b.findViewById(R.id.edt_to);
        phone = b.findViewById(R.id.edt_phone);

        //Layout pay
        LinearLayout p = findViewById(R.id.bottomPay);
        num_km = p.findViewById(R.id.num_km);
        moto = p.findViewById(R.id.tv_pay_moto);
        pkl = p.findViewById(R.id.tv_pay_pkl);
        scooter = p.findViewById(R.id.tv_pay_scooter);
        car = p.findViewById(R.id.tv_pay_car);
        cash = p.findViewById(R.id.btn_cash);
        momo = p.findViewById(R.id.btn_momo);
        huydon = p.findViewById(R.id.tv_huydon);
        cv1 = p.findViewById(R.id.cv1);
        cv2 = p.findViewById(R.id.cv2);
        cv3 = p.findViewById(R.id.cv3);
        cv4 = p.findViewById(R.id.cv4);

        rcv1 = p.findViewById(R.id.rd_moto);
        rcv2 = p.findViewById(R.id.rd_scooter);
        rcv3 = p.findViewById(R.id.rd_pkl);
        rcv4 = p.findViewById(R.id.rd_car);

        RelativeLayout bo = findViewById(R.id.bottom_booked);
        title_tx = bo.findViewById(R.id.title_tx);
        b_tx = bo.findViewById(R.id.b_tx);
        b_bienso = bo.findViewById(R.id.b_bien_so);
        b_tenxe = bo.findViewById(R.id.b_name_bike);
        b_loaixe = bo.findViewById(R.id.b_bike);
        b_km = bo.findViewById(R.id.b_km);
        b_price = bo.findViewById(R.id.b_price);
        b_call = bo.findViewById(R.id.btn_call);
        showDialog = new ShowDialog(this);
    }

    public LatLng getLatLangFromAddress(String strAddress) {
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyCWUNJzXbQ781iGctrfV6meRquPjfYjd98");
        }
        Geocoder coder = new Geocoder(this, Locale.getDefault());
        List<Address> address;
        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return new LatLng(10.773298908264719, 106.70459726328403);
            }
            Address location = address.get(0);
            return new LatLng(location.getLatitude(), location.getLongitude());
        } catch (IOException e) {
            return new LatLng(10.773298908264719, 106.70459726328403);
        }
    }

    //??o kho???ng c??ch
    public static Double distanceBetweenKm(LatLng point1, LatLng point2) {
        if (point1 == null || point2 == null) {
            return null;
        }
        Double km = SphericalUtil.computeDistanceBetween(point1, point2) / 1000;
        DecimalFormat df = new DecimalFormat("#.#");
        return Double.parseDouble(df.format(km).replace(",", "."));
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

}