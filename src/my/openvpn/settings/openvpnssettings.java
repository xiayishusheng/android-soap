package my.openvpn.settings;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class openvpnssettings extends TabActivity {
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
        Resources res = getResources();		//获得Drawables资源
        TabHost tabHost = getTabHost();		//获得TabHost
        TabHost.TabSpec spec;				//为每个页面分配标签
        Intent intent;						//每个页面重复利用Intent
        
        //创建一个Intent来启动Tab的Activity(可重复利用)
        intent = new Intent().setClass(this, certuser.class);
        //初始化页面并且加入TabHost
        spec = tabHost.newTabSpec("certuser").setIndicator("",res.getDrawable(R.drawable.tab_certuser)).setContent(intent);
        tabHost.addTab(spec);
        
        //另外的标签页做同样处理
        intent = new Intent().setClass(this, pswuser.class);
        spec = tabHost.newTabSpec("pswuser").setIndicator("",res.getDrawable(R.drawable.tab_pswuser)).setContent(intent);
        tabHost.addTab(spec);

        tabHost.setCurrentTab(1);

    }
}