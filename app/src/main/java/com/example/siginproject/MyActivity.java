package com.example.siginproject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MyLocationStyle;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MyActivity extends AppCompatActivity implements LocationSource, AMapLocationListener {

    private static final int STROKE_COLOR = Color.argb(180, 3, 145, 255);
    private static final int FILL_COLOR = Color.argb(10, 0, 0, 180);

    private MapView mapView = null;
    private AMap aMap;
    private Button request;
    private AMapLocationClient mLocationClient;
    private AMapLocationClientOption mLocationOption;
    private OnLocationChangedListener mListener;
    //标识，用于判断是否只显示一次定位信息和用户重新定位
    private boolean isFirstLoc = true;
    String date = null;
    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_activity);
        //    得到跳转到该Activity的Intent对象
        Intent intent = getIntent();
        final String studentNo = intent.getStringExtra("studentNo");
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<StudentCourseRelation> values = JSON.parseArray(getCourseByPost(studentNo), StudentCourseRelation.class);
                spinner = (Spinner)findViewById(R.id.spinner);
                List<SpinnerData> lst = new ArrayList<SpinnerData>();
                if (values != null && values.size() > 0) {
                    for (int i = 0; i < values.size(); i++) {
                        SpinnerData c = new SpinnerData(values.get(i).getCourseNo(), values.get(i).getPlanId(), values.get(i).getCourseName());
                        lst.add(c);
                    }
                }
                ArrayAdapter<SpinnerData> adapter = new ArrayAdapter<SpinnerData>(getActivity(), android.R.layout.simple_spinner_item, lst);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String var = spinner.getSelectedItem().toString();
                        Toast.makeText(MyActivity.this,var,Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }
        }).start();

        //获取地图控件引用
        mapView = findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，实现地图生命周期管理
        mapView.onCreate(savedInstanceState);
        //获取签到按钮
        request = findViewById(R.id.sign_button);
        init();
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        mLocationClient.setLocationListener(this);
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是否只定位一次
        mLocationOption.setOnceLocation(true);
        //设置是否强制刷新WiFi
        mLocationOption.setWifiScan(true);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(2000);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
    }
    public String getCourseByPost(String studentNo) {
        String path = Server.SERVER_HOST + "/course/" + studentNo;
        return HttpURLConnectionUtils.connection(path, "");
    }

    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
            setUpMap();
            UiSettings settings = aMap.getUiSettings();
            aMap.setLocationSource(this);
            settings.setMyLocationButtonEnabled(true);
            aMap.setMyLocationEnabled(true);
        }
    }

    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        setupLocationStyle();
    }

    /**
     * 设置自定义定位蓝点
     */
    private void setupLocationStyle(){
        // 自定义系统定位蓝点
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        // 自定义定位蓝点图标
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.
                fromResource(R.drawable.gps_point));
        // 自定义精度范围的圆形边框颜色
        myLocationStyle.strokeColor(STROKE_COLOR);
        //自定义精度范围的圆形边框宽度
        myLocationStyle.strokeWidth(5);
        // 设置圆形的填充颜色
        myLocationStyle.radiusFillColor(FILL_COLOR);
        // 将自定义的 myLocationStyle 对象添加到地图上
        aMap.setMyLocationStyle(myLocationStyle);
    }

    @Override
    public void onLocationChanged(final AMapLocation aMapLocation) {

        if (aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                //定位成功回调信息，设置相关消息
                aMapLocation.getLocationType();
                aMapLocation.getLatitude();//获取纬度
                aMapLocation.getLongitude();//获取经度
                aMapLocation.getAccuracy();//获取精度信息
                SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
                Date nowdate = new Date(aMapLocation.getTime());
                date = df.format(nowdate);//定位时间
                aMapLocation.getAddress();//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
                aMapLocation.getCountry();//国家信息
                aMapLocation.getProvince();//省信息
                aMapLocation.getCity();//城市信息
                aMapLocation.getDistrict();//城区信息
                aMapLocation.getStreet();//街道信息
                aMapLocation.getStreetNum();//街道门牌号信息
                aMapLocation.getBuildingId();//获取当前室内定位的建筑物Id
                aMapLocation.getAccuracy();//获取精度信息
                aMapLocation.getPoiName();
                aMapLocation.getAoiName();
                Log.e("", aMapLocation.getLocationDetail());
                //设置缩放级别
                aMap.moveCamera(CameraUpdateFactory.zoomTo(17));
                //将地图移动到定位点
                aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude())));
                //点击定位按钮 能够将地图的中心移动到定位点
                mListener.onLocationChanged(aMapLocation);
                isFirstLoc = false;
            }
        } else {
            Log.d("why", String.valueOf(aMapLocation.getErrorCode()));
            //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
            Log.e("AmapError", "location Error, ErrCode:"
                    + aMapLocation.getErrorCode() + ", errInfo:"
                    + aMapLocation.getErrorInfo());
            Toast.makeText(getApplicationContext(), "定位失败", Toast.LENGTH_LONG).show();
        }

        request.setOnClickListener(new View.OnClickListener() {
            //    得到跳转到该Activity的Intent对象
            Intent intent = getIntent();
            final String studentNo = intent.getStringExtra("studentNo");
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(MyActivity.this)
                        .setTitle("签到")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Message msg = new Message();
                                        Bundle data = new Bundle();
                                        String courseNo = ((SpinnerData)spinner.getSelectedItem()).getCourseNo();
                                        int planId = ((SpinnerData)spinner.getSelectedItem()).getPlanId();
                                        String latitude = String.valueOf(aMapLocation.getLatitude());//获取纬度
                                        String longitude = String.valueOf(aMapLocation.getLongitude());//获取经度
                                        data.putString("value", signinByPost(studentNo, courseNo, planId, longitude, latitude));
                                        msg.setData(data);
                                        handler1.sendMessage(msg);
                                    }
                                }).start();
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                    }
                }).show();
            }
        });
    }

    //激活定位
    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
    }

    @Override
    public void deactivate() {
        mListener = null;
    }

    Handler handler1 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String val = data.getString("value");
            //匹配密码
            if ("success".equals(val)) {
                Toast.makeText(MyActivity.this, "签到成功！", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MyActivity.this, "签到失败", Toast.LENGTH_SHORT).show();
            }
        }
    };


    public String signinByPost(String studentNo, String courseNo, int planId, String longitude, String latitude) {
        String path = Server.SERVER_HOST + "signin/" + studentNo + "/" + courseNo + "/" + planId + "/" + longitude + "/" + latitude;
        return HttpURLConnectionUtils.connection(path, "");
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mapView.onDestroy();
        //mLocationClient.stopLocation();
        mLocationClient.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，实现地图生命周期管理
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，实现地图生命周期管理
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，实现地图生命周期管理
        mapView.onSaveInstanceState(outState);
    }

    //设置再按一次结束程序
    private long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //标题栏返回箭头设置
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public Context getActivity() {
        return this;
    }
}
