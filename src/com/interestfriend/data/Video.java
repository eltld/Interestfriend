package com.interestfriend.data;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.interestfriend.data.enums.RetError;
import com.interestfriend.data.enums.RetStatus;
import com.interestfriend.data.result.ApiRequest;
import com.interestfriend.data.result.MapResult;
import com.interestfriend.data.result.Result;
import com.interestfriend.db.Const;
import com.interestfriend.parser.IParser;
import com.interestfriend.parser.MapParser;

public class Video extends AbstractData implements Serializable {

	private final String UP_LOAD_VIDEO = "UpLoadVideoServlet";

	private String video_img = "";
	private String video_path = "";
	public int video_size;
	public int video_duration;
	private int direct = 1;// 1 send 2 receive
	private int cid = 0;
	private int publisher_id = 0;
	int video_id;

	public int getVideo_id() {
		return video_id;
	}

	public void setVideo_id(int video_id) {
		this.video_id = video_id;
	}

	public int getCid() {
		return cid;
	}

	public void setCid(int cid) {
		this.cid = cid;
	}

	public int getPublisher_id() {
		return publisher_id;
	}

	public void setPublisher_id(int publisher_id) {
		this.publisher_id = publisher_id;
	}

	public int getDirect() {
		return direct;
	}

	public void setDirect(int direct) {
		this.direct = direct;
	}

	public String getVideo_img() {
		return video_img;
	}

	public void setVideo_img(String video_img) {
		this.video_img = video_img;
	}

	public String getVideo_path() {
		return video_path;
	}

	public void setVideo_path(String video_path) {
		this.video_path = video_path;
	}

	public int getVideo_size() {
		return video_size;
	}

	public void setVideo_size(int video_size) {
		this.video_size = video_size;
	}

	public int getVideo_duration() {
		return video_duration;
	}

	public void setVideo_duration(int video_duration) {
		this.video_duration = video_duration;
	}

	public RetError upLoadVideo() {
		String[] keys = { "video_img_path", "video_path" };
		IParser parser = new MapParser(keys);
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("cid", cid);
		params.put("publisher_id", publisher_id);
		params.put("video_size", video_size);
		params.put("video_duration", video_duration);
		Result ret = ApiRequest.requestWithFile(UP_LOAD_VIDEO, params,
				new File(video_path), parser);
		if (ret.getStatus() == RetStatus.SUCC) {
			MapResult mret = (MapResult) ret;
			video_path = (String) (mret.getMaps().get("video_path"));
			video_img = (String) (mret.getMaps().get("video_img_path"));
			return RetError.NONE;
		} else {
			return ret.getErr();
		}
	}

	@Override
	public void write(SQLiteDatabase db) {
		String dbName = Const.VIDEO_TABLE_NAME;
		ContentValues cv = new ContentValues();
		cv.put("video_id", this.video_id);
		cv.put("cid", this.cid);
		cv.put("publisher_id", this.publisher_id);
		cv.put("video_size", this.video_size);
		cv.put("video_duration", this.video_duration);
		cv.put("video_img", this.video_img);
		cv.put("video_path", this.video_path);
		db.insert(dbName, null, cv);
	}
}
