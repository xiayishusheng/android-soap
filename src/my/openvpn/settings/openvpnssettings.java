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
		
        Resources res = getResources();		//���Drawables��Դ
        TabHost tabHost = getTabHost();		//���TabHost
        TabHost.TabSpec spec;				//Ϊÿ��ҳ������ǩ
        Intent intent;						//ÿ��ҳ���ظ�����Intent
        
        //����һ��Intent������Tab��Activity(���ظ�����)
        intent = new Intent().setClass(this, certuser.class);
        //��ʼ��ҳ�沢�Ҽ���TabHost
        spec = tabHost.newTabSpec("certuser").setIndicator("",res.getDrawable(R.drawable.tab_certuser)).setContent(intent);
        tabHost.addTab(spec);
        
        //����ı�ǩҳ��ͬ������
        intent = new Intent().setClass(this, pswuser.class);
        spec = tabHost.newTabSpec("pswuser").setIndicator("",res.getDrawable(R.drawable.tab_pswuser)).setContent(intent);
        tabHost.addTab(spec);

        tabHost.setCurrentTab(1);

    }
}