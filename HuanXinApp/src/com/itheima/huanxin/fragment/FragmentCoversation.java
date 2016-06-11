package com.itheima.huanxin.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.itheima.huanxin.R;
import com.itheima.huanxin.adapter.ConversationAdapter;

/**
 * 对话(微信)
 * @author zhangming
 * @date 2016/06/04
 */
public class FragmentCoversation extends Fragment{
    private ListView listView;
    private ConversationAdapter adapter;
	private List<EMConversation> normal_list = new ArrayList<EMConversation>();
    
    public RelativeLayout errorItem;
    public TextView errorText;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_conversation, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		errorItem = (RelativeLayout) getView().findViewById(R.id.rl_error_item);
	    errorText = (TextView) errorItem.findViewById(R.id.tv_connect_errormsg);

        normal_list.addAll(loadConversationsWithRecentChat());
        listView = (ListView) getView().findViewById(R.id.list);
        adapter = new ConversationAdapter(getActivity(), normal_list);
        // 设置adapter
        listView.setAdapter(adapter);
	}
	
	/**
     * 刷新页面
     */
    public void refresh() {
        normal_list.clear();
        normal_list.addAll(loadConversationsWithRecentChat());
        adapter = new ConversationAdapter(getActivity(), normal_list);
        listView.setAdapter(adapter);
    }
	
	/**
     * 获取所有会话
     * @param context
     */
    private List<EMConversation> loadConversationsWithRecentChat() {
        // 获取所有会话，包括陌生人
        Hashtable<String, EMConversation> conversations = EMChatManager.getInstance().getAllConversations();
        List<EMConversation> list = new ArrayList<EMConversation>();
        //List<EMConversation> topList1 = new ArrayList<EMConversation>();

        // 置顶列表再刷新一次
        // 过滤掉messages seize为0的conversation
        for (EMConversation conversation : conversations.values()) {
            if (conversation.getAllMessages().size() != 0) {
            	list.add(conversation); 
            }
        }
        // 排序
        sortConversationByLastChatTime(list);
        return list;
    }
    
    /**
     * 根据最后一条消息的时间排序
     * @param usernames
     */
    private void sortConversationByLastChatTime(List<EMConversation> conversationList) {
        Collections.sort(conversationList, new Comparator<EMConversation>() {
            @Override
            public int compare(final EMConversation con1,final EMConversation con2) {
                EMMessage con2LastMessage = con2.getLastMessage();
                EMMessage con1LastMessage = con1.getLastMessage();
                if (con2LastMessage.getMsgTime() == con1LastMessage.getMsgTime()) {
                    return 0;
                } else if (con2LastMessage.getMsgTime() > con1LastMessage.getMsgTime()) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
    }
}
