/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.itheima.app;

import java.util.Map;

import android.content.Intent;

import com.easemob.EMCallBack;
import com.itheima.huanxin.MainActivity;
import com.itheima.huanxin.applib.controller.HXSDKHelper;
import com.itheima.huanxin.applib.model.HXSDKModel;
import com.itheima.huanxin.domain.User;
 

/**
 * Demo UI HX SDK helper class which subclass HXSDKHelper
 * @author easemob
 */
public class DemoHXSDKHelper extends HXSDKHelper {
    /**
     * contact list in cache
     */
    private Map<String, User> contactList;
    
	@Override
	protected HXSDKModel createModel() {
		return new DemoHXSDKModel(appContext);
	}
	
	//注意:需要覆盖此方法
	@Override
    protected void initHXOptions() {
        super.initHXOptions();
        // you can also get EMChatOptions to set related SDK options
        // EMChatOptions options = EMChatManager.getInstance().getChatOptions();
    }
	
	@Override
    protected void onConnectionConflict() {
        Intent intent = new Intent(appContext, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("conflict", true);
        appContext.startActivity(intent);
    }

    @Override
    protected void onCurrentAccountRemoved() {
        Intent intent = new Intent(appContext, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constant.ACCOUNT_REMOVED, true);
        appContext.startActivity(intent);
    }
 
    /**
     * get demo HX SDK Model
     */
    public DemoHXSDKModel getModel() {
        return (DemoHXSDKModel) hxModel;
    }
    
    /**
     * 获取内存中好友user list
     * 
     * @return
     */
    public Map<String, User> getContactList() {
        if (getHXId() != null && contactList == null) {
            contactList = ((DemoHXSDKModel) getModel()).getContactList();
        }
        return contactList;
    }
    
    /**
     * 设置好友user list到内存中
     * @param contactList
     */
    public void setContactList(Map<String, User> contactList) {
        this.contactList = contactList;
    }
    
    //TODO DoSomething
    
    
    @Override
    public void logout(final EMCallBack callback) {
        super.logout(new EMCallBack() {
            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub
                setContactList(null);
                getModel().closeDB();
                if (callback != null) {
                    callback.onSuccess();
                }
            }

            @Override
            public void onError(int code, String message) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProgress(int progress, String status) {
                // TODO Auto-generated method stub
                if (callback != null) {
                    callback.onProgress(progress, status);
                }
            }
        });
    }
}
