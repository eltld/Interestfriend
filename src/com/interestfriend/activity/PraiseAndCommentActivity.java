package com.interestfriend.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.exceptions.EaseMobException;
import com.interestfriend.R;
import com.interestfriend.adapter.CommentAdapter;
import com.interestfriend.adapter.GrowthImgAdapter;
import com.interestfriend.adapter.PraiseAdapter;
import com.interestfriend.data.CircleMember;
import com.interestfriend.data.Comment;
import com.interestfriend.data.Growth;
import com.interestfriend.data.GrowthImage;
import com.interestfriend.data.enums.RetError;
import com.interestfriend.db.DBUtils;
import com.interestfriend.interfaces.AbstractTaskPostCallBack;
import com.interestfriend.popwindow.CommentPopwindow;
import com.interestfriend.popwindow.CommentPopwindow.OnCommentOnClick;
import com.interestfriend.showbigpic.ImagePagerActivity;
import com.interestfriend.task.DeleteCommentTask;
import com.interestfriend.task.GetGrowthTask;
import com.interestfriend.task.SendCommentTask;
import com.interestfriend.utils.Constants;
import com.interestfriend.utils.DateUtils;
import com.interestfriend.utils.DialogUtil;
import com.interestfriend.utils.SharedUtils;
import com.interestfriend.utils.ToastUtil;
import com.interestfriend.utils.UniversalImageLoadTool;
import com.interestfriend.utils.Utils;
import com.interestfriend.view.ExpandGridView;
import com.interestfriend.view.HorizontalListView;

public class PraiseAndCommentActivity extends BaseActivity implements
		OnClickListener, OnItemClickListener, TextWatcher, OnCommentOnClick {
	private ImageView img_avatar;
	private TextView txt_user_name;
	private TextView txt_time;
	private TextView txt_context;
	private ImageView img;
	private ExpandGridView img_grid_view;
	private ImageView back;
	private TextView txt_title;
	private Button btnComment;
	private EditText edit_comment;
	private ListView mListView;
	private ScrollView layout_scroll;

	private Growth growth;

	private Dialog dialog;

	private CommentAdapter adapter;
	private List<Comment> comments = new ArrayList<Comment>();

	private boolean isReplaySomeOne = false;

	private int replaySomeOneID = 0;
	private String replaySomeOneName = "";

	private CommentPopwindow pop;

	private LinearLayout parise_layout;
	private HorizontalListView praise_listView;
	private PraiseAdapter praiseAdapter;

	private int growth_id;
	private String user_id = "";
	private int circle_id;
	private EMConversation conversation;
	private EMMessage lastMessage;

	private LinearLayout comment_layout;

	private RelativeLayout layout_title;

	private TextView btn_praise;
	private TextView btn_comment;
	private RelativeLayout layout_parent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_praise_and_comment);
		growth = new Growth();
		getDataByMessage();
		initView();
		getGrowth();
	}

	private void getDataByMessage() {
		user_id = getIntent().getStringExtra("userId");
		conversation = EMChatManager.getInstance().getConversation(user_id);
		lastMessage = conversation.getLastMessage();
		try {
			String grwid = lastMessage.getStringAttribute("growth_id");
			growth_id = Integer.valueOf(grwid);
			circle_id = Integer.valueOf(lastMessage
					.getStringAttribute("circle_id"));

		} catch (EaseMobException e) {
			e.printStackTrace();
		}
	}

	private void initView() {
		layout_parent = (RelativeLayout) findViewById(R.id.layout_parent);
		layout_parent.setVisibility(View.GONE);
		btn_praise = (TextView) findViewById(R.id.btn_prise);
		btn_comment = (TextView) findViewById(R.id.btn_comment);
		layout_scroll = (ScrollView) findViewById(R.id.layout_scroll);
		layout_title = (RelativeLayout) findViewById(R.id.layout_title);
		back = (ImageView) findViewById(R.id.back);
		txt_title = (TextView) findViewById(R.id.title_txt);
		txt_title.setText("����");
		img = (ImageView) findViewById(R.id.img);
		txt_context = (TextView) findViewById(R.id.txt_content);
		txt_time = (TextView) findViewById(R.id.txt_time);
		txt_user_name = (TextView) findViewById(R.id.txt_user_name);
		img_avatar = (ImageView) findViewById(R.id.img_avatar);
		img_grid_view = (ExpandGridView) findViewById(R.id.imgGridview);
		btnComment = (Button) findViewById(R.id.btnComment);
		edit_comment = (EditText) findViewById(R.id.edit_content);
		mListView = (ListView) findViewById(R.id.listView1);
		parise_layout = (LinearLayout) findViewById(R.id.layout_praise);
		praise_listView = (HorizontalListView) findViewById(R.id.praise_listView);
		comment_layout = (LinearLayout) findViewById(R.id.layout_comment);
		setListener();
	}

	private void setListener() {
		img.setOnClickListener(this);
		back.setOnClickListener(this);
		btnComment.setOnClickListener(this);
		edit_comment.addTextChangedListener(this);
		mListView.setOnItemClickListener(this);
		img_grid_view.setOnItemClickListener(new GridViewOnItemClick());
		Utils.getFocus(layout_title);
	}

	class GridViewOnItemClick implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> arg0, View v, int posit,
				long arg3) {
			intentImagePager(posit);
		}
	}

	private void intentImagePager(int index) {
		List<String> imgUrl = new ArrayList<String>();
		for (GrowthImage img : growth.getImages()) {
			imgUrl.add(img.getImg());
		}
		Intent intent = new Intent(this, ImagePagerActivity.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable(Constants.EXTRA_IMAGE_URLS,
				(Serializable) imgUrl);
		intent.putExtras(bundle);
		intent.putExtra(Constants.EXTRA_IMAGE_INDEX, index);
		startActivity(intent);
	}

	public void getGrowth() {
		dialog = DialogUtil.createLoadingDialog(this, "���Ժ�");
		dialog.show();
		growth.setCid(circle_id);
		growth.setGrowth_id(growth_id);
		GetGrowthTask task = new GetGrowthTask();
		task.setmCallBack(new AbstractTaskPostCallBack<RetError>() {
			@Override
			public void taskFinish(RetError result) {
				if (dialog != null) {
					dialog.dismiss();
				}
				if (result != RetError.NONE) {
					return;
				}
				layout_parent.setVisibility(View.VISIBLE);
				setValue();

				List<EMMessage> messages = conversation.getAllMessages();
				for (int i = messages.size() - 1; i >= 0; i--) {
					conversation.removeMessage(messages.get(i).getMsgId());
				}
				conversation.resetUnsetMsgCount();
			}
		});
		task.executeParallel(growth);
	}

	private void setValue() {
		layout_scroll.setVisibility(View.VISIBLE);
		CircleMember member = new CircleMember();
		member.setUser_id(growth.getPublisher_id());
		member.getNameAndAvatar(DBUtils.getDBsa(1));
		int imageSize = growth.getImages().size();
		if (imageSize > 1) {
			if (imageSize > 2) {
				img_grid_view.setNumColumns(3);
			} else {
				img_grid_view.setNumColumns(2);
			}
			img_grid_view.setAdapter(new GrowthImgAdapter(this, growth
					.getImages()));
			img.setVisibility(View.GONE);
			img_grid_view.setVisibility(View.VISIBLE);
		} else if (imageSize == 1) {
			String path = growth.getImages().get(0).getImg();
			if (!path.startsWith("http")) {
				path = "file://" + path;
			}
			UniversalImageLoadTool.disPlay(path, img, R.drawable.empty_photo);
			img.setVisibility(View.VISIBLE);
			img_grid_view.setVisibility(View.GONE);
		} else {
			img.setVisibility(View.GONE);
			img_grid_view.setVisibility(View.GONE);
		}
		String content = growth.getContent();
		if ("".equals(content)) {
			txt_context.setVisibility(View.GONE);
		} else {
			txt_context.setVisibility(View.VISIBLE);
			txt_context.setText(content);

		}
		txt_time.setText(growth.getPublished());
		UniversalImageLoadTool.disPlay(growth.getPublisher_avatar(),
				img_avatar, R.drawable.default_avatar);
		txt_user_name.setText(growth.getPublisher_name());
		System.out.println();
		comments = growth.getComments();
		adapter = new CommentAdapter(this, comments);
		mListView.setAdapter(adapter);

		viewLineVisible();
		if (growth.getPraises().size() > 0) {
			parise_layout.setVisibility(View.VISIBLE);
			praiseAdapter = new PraiseAdapter(this, growth.getPraises());
			praise_listView.setAdapter(praiseAdapter);
		} else {
			parise_layout.setVisibility(View.GONE);

		}
		if (growth.getComments().size() > 0) {
			btn_comment.setText("�ظ�(" + growth.getComments().size() + ")");
		} else {
			btn_comment.setText("�ظ�");
		}
		Drawable drawable = getResources().getDrawable(
				R.drawable.praise_img_nomal);
		if (growth.isPraise()) {
			drawable = getResources().getDrawable(R.drawable.praise_img_focus);
		}
		drawable.setBounds(0, 0, drawable.getMinimumWidth(),
				drawable.getMinimumHeight());
		btn_praise.setCompoundDrawables(drawable, null, null, null);
		if (growth.getPraise_count() > 0) {
			btn_praise.setText("��(" + growth.getPraise_count() + ")");
		} else {
			btn_praise.setText("��");
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finishThisActivity();
			break;
		case R.id.btnComment:
			String content = edit_comment.getText().toString().trim();
			if (content.length() == 0) {
				return;
			}
			sendComment(content.replace("@" + replaySomeOneName, ""));
			break;
		case R.id.img:
			intentImagePager(1);
			break;
		default:
			break;
		}
	}

	private void sendComment(String content) {
		dialog = DialogUtil.createLoadingDialog(this, "���Ժ�");
		dialog.show();
		final Comment comment = new Comment();
		comment.setComment_content(content);
		if (isReplaySomeOne) {
			comment.setReply_someone_name(replaySomeOneName);
			comment.setReply_someone_id(replaySomeOneID);
		}
		comment.setGrowth_id(growth.getGrowth_id());
		comment.setComment_time(DateUtils.getGrowthShowTime());
		comment.setPublisher_id(SharedUtils.getIntUid());
		comment.setPublisher_avatar(SharedUtils.getAPPUserAvatar());
		comment.setPublisher_name(SharedUtils.getAPPUserName());
		SendCommentTask task = new SendCommentTask(growth.getPublisher_id());
		task.setmCallBack(new AbstractTaskPostCallBack<RetError>() {
			@Override
			public void taskFinish(RetError result) {
				if (dialog != null) {
					dialog.dismiss();
				}
				if (result != RetError.NONE) {
					return;
				}
				edit_comment.setText("");
				ToastUtil.showToast("�ظ��ɹ�", Toast.LENGTH_SHORT);
				comments.add(0, comment);
				adapter.notifyDataSetChanged();
				viewLineVisible();

			}
		});
		task.executeParallel(comment);
	}

	private void viewLineVisible() {
		if (comments.size() > 0) {
			comment_layout.setVisibility(View.VISIBLE);
			btn_comment.setText("�ظ�(" + growth.getComments().size() + ")");
		} else {
			comment_layout.setVisibility(View.GONE);
			btn_comment.setText("�ظ�");
		}
	}

	private void delReplaySomeOne() {
		isReplaySomeOne = false;
		replaySomeOneID = 0;
		replaySomeOneName = "";
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position,
			long arg3) {
		pop = new CommentPopwindow(this, view, position,
				growth.getPublisher_id());
		pop.setmCallBack(this);
		pop.show();

	}

	@Override
	public void afterTextChanged(Editable str) {
		if (isReplaySomeOne) {
			if (replaySomeOneName.equals(str.toString().replace("@", ""))) {
				edit_comment.setText("");
				delReplaySomeOne();
			}
		}
	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {

	}

	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

	}

	@Override
	public void onClick(int position, int id) {
		switch (id) {
		case R.id.txt_reply:
			reply(position);
			break;
		case R.id.txt_del:
			del(position);
			break;
		default:
			break;
		}
	}

	private void del(final int position) {
		final Dialog dialog = DialogUtil.createLoadingDialog(this, "���Ժ�");
		dialog.show();
		Comment comment = comments.get(position);
		DeleteCommentTask task = new DeleteCommentTask();
		task.setmCallBack(new AbstractTaskPostCallBack<RetError>() {
			@Override
			public void taskFinish(RetError result) {
				if (dialog != null) {
					dialog.dismiss();
				}
				if (result != RetError.NONE) {
					return;
				}
				comments.remove(position);
				adapter.notifyDataSetChanged();
				viewLineVisible();

			}
		});
		task.executeParallel(comment);
	}

	private void reply(int position) {
		Comment comment = comments.get(position);
		edit_comment.setText(Html.fromHtml("<font color=#F06617>@"
				+ comment.getPublisher_name() + "</font> "));
		edit_comment.setSelection(edit_comment.getText().toString().length());
		Utils.getFocus(edit_comment);
		isReplaySomeOne = true;
		replaySomeOneID = comment.getPublisher_id();
		replaySomeOneName = comment.getPublisher_name();
	}
}
