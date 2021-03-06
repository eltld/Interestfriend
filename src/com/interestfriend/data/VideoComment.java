package com.interestfriend.data;

import java.util.HashMap;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.interestfriend.data.AbstractData.Status;
import com.interestfriend.data.enums.RetError;
import com.interestfriend.data.enums.RetStatus;
import com.interestfriend.data.result.ApiRequest;
import com.interestfriend.data.result.Result;
import com.interestfriend.data.result.StringResult;
import com.interestfriend.db.Const;
import com.interestfriend.parser.IParser;
import com.interestfriend.parser.SimpleParser;
import com.interestfriend.parser.StringParser;

public class VideoComment extends AbstractData {

	private final String VIDEO_COMMENT_API = "VideoCommentServlet";
	private final String DEL_COMMENT_PAI = "DelVideoCommentServlet";

	private int comment_id;
	private int video_id;
	private int publisher_id;
	private String comment_content = "";
	private String comment_time = "";
	private String publisher_name = "";
	private String publisher_avatar = "";

	private String reply_someone_name = "";
	private int reply_someone_id = 0;

	public String getReply_someone_name() {
		return reply_someone_name;
	}

	public void setReply_someone_name(String reply_someone_name) {
		this.reply_someone_name = reply_someone_name;
	}

	public int getReply_someone_id() {
		return reply_someone_id;
	}

	public void setReply_someone_id(int reply_someone_id) {
		this.reply_someone_id = reply_someone_id;
	}

	public String getPublisher_name() {
		return publisher_name;
	}

	public void setPublisher_name(String publisher_name) {
		this.publisher_name = publisher_name;
	}

	public String getPublisher_avatar() {
		return publisher_avatar;
	}

	public void setPublisher_avatar(String publisher_avatar) {
		this.publisher_avatar = publisher_avatar;
	}

	public int getPublisher_id() {
		return publisher_id;
	}

	public void setPublisher_id(int publisher_id) {
		this.publisher_id = publisher_id;
	}

	public int getComment_id() {
		return comment_id;
	}

	public void setComment_id(int comment_id) {
		this.comment_id = comment_id;
	}

	public int getVideo_id() {
		return video_id;
	}

	public void setVideo_id(int video_id) {
		this.video_id = video_id;
	}

	public String getComment_content() {
		return comment_content;
	}

	public void setComment_content(String comment_content) {
		this.comment_content = comment_content;
	}

	public String getComment_time() {
		return comment_time;
	}

	public void setComment_time(String comment_time) {
		this.comment_time = comment_time;
	}

	public RetError sendComment() {
		IParser parser = new StringParser("comment_id");
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("comment_content", comment_content);
		params.put("video_id", video_id);
		params.put("reply_someone_name", reply_someone_name);
		params.put("reply_someone_id", reply_someone_id);
		Result ret = ApiRequest.request(VIDEO_COMMENT_API, params, parser);
		if (ret.getStatus() == RetStatus.SUCC) {
			StringResult sr = (StringResult) ret;
			this.comment_id = Integer.valueOf(sr.getStr());
			return RetError.NONE;
		} else {
			return ret.getErr();
		}

	}

	public RetError deleteComment() {
		IParser parser = new SimpleParser();
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("comment_id", comment_id);
		Result ret = ApiRequest.request(DEL_COMMENT_PAI, params, parser);
		if (ret.getStatus() == RetStatus.SUCC) {
			this.status = Status.DEL;
			return RetError.NONE;
		} else {
			return ret.getErr();
		}
	}

	@Override
	public void write(SQLiteDatabase db) {
		String dbName = Const.VIDEO_COMMENT_TABLE_NAME;
		if (this.status == Status.DEL) {
			db.delete(dbName, "comment_id=?", new String[] { comment_id + "" });
			return;
		}
		ContentValues cv = new ContentValues();
		cv.put("video_id", this.video_id);
		cv.put("comment_id", this.comment_id);
		cv.put("publisher_id", this.publisher_id);
		cv.put("comment_time", this.comment_time);
		cv.put("comment_content", this.comment_content);
		cv.put("publisher_name", this.publisher_name);
		cv.put("publisher_avatar", this.publisher_avatar);
		cv.put("reply_someone_id", this.reply_someone_id);
		cv.put("reply_someone_name", this.reply_someone_name);

		db.insert(dbName, null, cv);

	}
}
