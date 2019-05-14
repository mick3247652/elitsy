package com.ru.ruchurch.ui.widget;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import me.saket.bettermovementmethod.BetterLinkMovementMethod;

public class CopyTextView extends TextView {

	public CopyTextView(Context context) {
		super(context);
		//createLink(context);
	}

	public CopyTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		//createLink(context);
	}

	public CopyTextView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		//createLink(context);
	}

	private void createLink(Context context){
		BetterLinkMovementMethod
				.linkify(Linkify.ALL, this)
				.setOnLinkClickListener((textView, url) -> {
					// Do something.
					Log.v("YYYYY"," YEA.....");
					return true;
				});
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);

	}

	@SuppressWarnings("unused")
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public CopyTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	public interface CopyHandler {
		public String transformTextForCopy(CharSequence text, int start, int end);
	}

	private CopyHandler copyHandler;

	public void setCopyHandler(CopyHandler copyHandler) {
		this.copyHandler = copyHandler;
	}

	@Override
	public boolean onTextContextMenuItem(int id) {
		CharSequence text = getText();
		int min = 0;
		int max = text.length();
		if (isFocused()) {
			final int selStart = getSelectionStart();
			final int selEnd = getSelectionEnd();
			min = Math.max(0, Math.min(selStart, selEnd));
			max = Math.max(0, Math.max(selStart, selEnd));
		}
		String textForCopy = null;
		if (id == android.R.id.copy && copyHandler != null) {
			textForCopy = copyHandler.transformTextForCopy(getText(), min, max);
		}
		try {
			return super.onTextContextMenuItem(id);
		} finally {
			if (textForCopy != null) {
				ClipboardManager clipboard = (ClipboardManager) getContext().
						getSystemService(Context.CLIPBOARD_SERVICE);
				clipboard.setPrimaryClip(ClipData.newPlainText(null, textForCopy));
			}
		}
	}

}