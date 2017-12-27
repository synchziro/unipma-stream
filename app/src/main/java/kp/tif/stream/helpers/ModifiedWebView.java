package kp.tif.stream.helpers;

import android.content.Context;
import android.view.MotionEvent;
import android.webkit.WebView;

/**
 * Created by Synchziro.
 */

    public class ModifiedWebView extends WebView {
        public ModifiedWebView(Context context)
        {
            super(context);
        }

        // Note this!
        @Override
        public boolean onCheckIsTextEditor()
        {
            return true;
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev)
        {
            switch (ev.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_UP:
                    if (!hasFocus())
                        requestFocus();
                    break;
            }

            return super.onTouchEvent(ev);
        }
    }