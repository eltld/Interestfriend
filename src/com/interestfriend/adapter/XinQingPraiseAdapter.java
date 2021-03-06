package com.interestfriend.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.interestfriend.R;
import com.interestfriend.data.XinQingPraise;
import com.interestfriend.interfaces.OnAvatarClick;
import com.interestfriend.utils.UniversalImageLoadTool;
import com.interestfriend.view.RoundAngleImageView;

public class XinQingPraiseAdapter extends BaseAdapter {
	private List<XinQingPraise> praises;
	private Context mContext;

	public XinQingPraiseAdapter(Context context, List<XinQingPraise> praises) {
		this.mContext = context;
		this.praises = praises;
	}

	@Override
	public int getCount() {
		return praises.size();
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.praise_item, null);
			holder = new ViewHolder();
			holder.user_avatar = (RoundAngleImageView) convertView
					.findViewById(R.id.img_user_avatar);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		UniversalImageLoadTool.disPlay(praises.get(position).getUser_avatar(),
				holder.user_avatar, R.drawable.picture_default_head);
		holder.user_avatar.setOnClickListener(new OnAvatarClick(praises.get(
				position).getUser_id(), mContext));
		return convertView;
	}

	class ViewHolder {
		RoundAngleImageView user_avatar;
	}
}
