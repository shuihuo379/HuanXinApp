package com.itheima.huanxin.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

public class PasteEditText extends EditText{
	private Context context;
	    
    public PasteEditText(Context context) {
        super(context);
        this.context = context;
    }

    public PasteEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public PasteEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }
    
    @Override
    public boolean onTextContextMenuItem(int id) {
    	return super.onTextContextMenuItem(id);
    }
}
