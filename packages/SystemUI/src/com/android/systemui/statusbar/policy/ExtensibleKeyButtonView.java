package com.android.systemui.statusbar.policy;


import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import com.android.systemui.eclipse.EclipseTarget;
import com.android.systemui.statusbar.policy.KeyButtonView;
import com.android.systemui.R;


public class ExtensibleKeyButtonView extends KeyButtonView {

    private EclipseTarget mEclipseTarget;

    public String mClickAction, mLongpress;

    public ExtensibleKeyButtonView(Context context, AttributeSet attrs, String ClickAction, String Longpress) {
        super(context, attrs);
        setActions(ClickAction,Longpress);
    }

    public void setEclipseTarget(EclipseTarget targ){
        mEclipseTarget = targ;
        }

    public void setActions(String ClickAction, String Longpress){
        mClickAction = ClickAction;
        mLongpress = Longpress;
        if (ClickAction != null){
            if (ClickAction.equals(EclipseTarget.ACTION_HOME)) {
                setCode(KeyEvent.KEYCODE_HOME);
                setId(R.id.home);
            } else if (ClickAction.equals(EclipseTarget.ACTION_BACK)) {
                setCode (KeyEvent.KEYCODE_BACK);
                setId(R.id.back);
            } else if (ClickAction.equals(EclipseTarget.ACTION_MENU)) {
                setCode (KeyEvent.KEYCODE_MENU);
                setId(R.id.menu);
            } else if (ClickAction.equals(EclipseTarget.ACTION_POWER)) {
                setCode (KeyEvent.KEYCODE_POWER);
            } else if (ClickAction.equals(EclipseTarget.ACTION_SEARCH)) {
                setCode (KeyEvent.KEYCODE_SEARCH);
            } else {
                setOnClickListener(mClickListener);
                if (ClickAction.equals(EclipseTarget.ACTION_RECENTS))
                    setId(R.id.recent_apps);                        setId(R.id.recent_apps);
            }
            setSupportsLongPress (false);
            if (Longpress != null)
                if ((!Longpress.equals(EclipseTarget.ACTION_NULL)) || (getCode() !=0)) {
                    // I want to allow long presses for defined actions, or if
                    // primary action is a 'key' and long press isn't defined otherwise
                    setSupportsLongPress(true);
                    setOnLongClickListener(mLongPressListener);
                    }
        }
    }

    private OnClickListener mClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mEclipseTarget.launchAction(mClickAction);
        }
    };

    private OnLongClickListener mLongPressListener = new OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            return mEclipseTarget.launchAction(mLongpress);
        }
    };
}
