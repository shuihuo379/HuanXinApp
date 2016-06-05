package com.itheima.huanxin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.itheima.app.Constant;
import com.itheima.app.MyApplication;
import com.itheima.huanxin.fragment.FragmentCoversation;
import com.itheima.huanxin.fragment.FragmentFind;
import com.itheima.huanxin.fragment.FragmentFriends;
import com.itheima.huanxin.fragment.FragmentProfile;
import com.itheima.huanxin.view.AddPopWindow;

/**
 * APP主界面
 * @author zhangming
 */
public class MainActivity extends BaseActivity {
    private Fragment[] fragments;
    private FragmentCoversation homefragment;
    private FragmentFriends contactlistfragment;
    private FragmentFind findfragment;
    private FragmentProfile profilefragment;
    
    private ImageView[] imagebuttons;
    private TextView[] textviews;
    private int index;
    private int currentTabIndex;  // 当前fragment的index
    
    private ImageView iv_add;
    private ImageView iv_search;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null && savedInstanceState.getBoolean(Constant.ACCOUNT_REMOVED,false)) {
            // 防止被移除后，没点确定按钮然后按了home键，长期在后台又进app导致的crash
            // 三个fragment里加的判断同理
            MyApplication.getInstance().logout(null);
            finish();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        } else if (savedInstanceState != null
                && savedInstanceState.getBoolean("isConflict", false)) {
            // 防止被T后，没点确定按钮然后按了home键，长期在后台又进app导致的crash
            // 三个fragment里加的判断同理
            finish();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }
		setContentView(R.layout.activity_main);
		initView();
	}
	
	private void initView(){
		homefragment = new FragmentCoversation();
	    contactlistfragment = new FragmentFriends();
	    findfragment = new FragmentFind();
	    profilefragment = new FragmentProfile();
	    fragments = new Fragment[] {homefragment, contactlistfragment,findfragment, profilefragment};
	
        imagebuttons = new ImageView[4];
        imagebuttons[0] = (ImageView) findViewById(R.id.ib_weixin);
        imagebuttons[1] = (ImageView) findViewById(R.id.ib_contact_list);
        imagebuttons[2] = (ImageView) findViewById(R.id.ib_find);
        imagebuttons[3] = (ImageView) findViewById(R.id.ib_profile);

        imagebuttons[0].setSelected(true);
        textviews = new TextView[4];
        textviews[0] = (TextView) findViewById(R.id.tv_weixin);
        textviews[1] = (TextView) findViewById(R.id.tv_contact_list);
        textviews[2] = (TextView) findViewById(R.id.tv_find);
        textviews[3] = (TextView) findViewById(R.id.tv_profile);
        textviews[0].setTextColor(0xFF45C01A);
        // 添加显示第一个fragment
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, homefragment)
                .add(R.id.fragment_container, contactlistfragment)
                .add(R.id.fragment_container, profilefragment)
                .add(R.id.fragment_container, findfragment)
                .hide(contactlistfragment).hide(profilefragment)
                .hide(findfragment).show(homefragment).commit();
        
        
        iv_add = (ImageView) this.findViewById(R.id.iv_add);
        iv_search = (ImageView) this.findViewById(R.id.iv_search);
        iv_add.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AddPopWindow addPopWindow = new AddPopWindow(MainActivity.this);
                addPopWindow.showPopupWindow(iv_add);
            }
        });
        iv_search.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
	}
	
	public void onTabClicked(View view) {
        switch (view.getId()) {
        case R.id.re_weixin:
            index = 0;
            break;
        case R.id.re_contact_list:
            index = 1;
            break;
        case R.id.re_find:
            index = 2;
            break;
        case R.id.re_profile:
            index = 3;
            break;
        }

        if (currentTabIndex != index) {
            FragmentTransaction trx = getSupportFragmentManager()
                    .beginTransaction();
            trx.hide(fragments[currentTabIndex]);
            if (!fragments[index].isAdded()) {
                trx.add(R.id.fragment_container, fragments[index]);
            }
            trx.show(fragments[index]).commit();
        }
        imagebuttons[currentTabIndex].setSelected(false);
        // 把当前tab设为选中状态
        imagebuttons[index].setSelected(true);
        textviews[currentTabIndex].setTextColor(0xFF999999);
        textviews[index].setTextColor(0xFF45C01A);
        currentTabIndex = index;
	}
	
	private long exitTime = 0;
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出程序",
                        Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                moveTaskToBack(false);
                finish();

            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
