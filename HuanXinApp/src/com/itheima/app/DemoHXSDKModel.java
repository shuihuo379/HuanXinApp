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

import java.util.List;
import java.util.Map;

import android.content.Context;

import com.itheima.huanxin.applib.model.DefaultHXSDKModel;
import com.itheima.huanxin.db.DbOpenHelper;
import com.itheima.huanxin.db.UserDao;
import com.itheima.huanxin.domain.User;


public class DemoHXSDKModel extends DefaultHXSDKModel{
    public DemoHXSDKModel(Context ctx) {
        super(ctx);
    }
    
    // demo will use HuanXin roster
    public boolean getUseHXRoster() {
        return true;
    }
    
    // demo will switch on debug mode
    public boolean isDebugMode(){
        return true;
    }
    
    @Override
    public String getAppProcessName() {
        // TODO Auto-generated method stub
        return "com.itheima.huanxin";
    }
    
    public boolean saveContactList(List<User> contactList) {
        // TODO Auto-generated method stub
        UserDao dao = new UserDao(context);
        dao.saveContactList(contactList);
        return true;
    }

    public Map<String, User> getContactList() {
        // TODO Auto-generated method stub
        UserDao dao = new UserDao(context);
        return dao.getContactList();
    }
    
    public void closeDB() {
        // TODO Auto-generated method stub
        DbOpenHelper.getInstance(context).closeDB();
    }
}
