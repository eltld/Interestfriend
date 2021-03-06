package com.interestfriend.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.interestfriend.data.enums.RetError;
import com.interestfriend.data.enums.RetStatus;
import com.interestfriend.data.result.ApiRequest;
import com.interestfriend.data.result.Result;
import com.interestfriend.db.Const;
import com.interestfriend.parser.GrowthListParser;
import com.interestfriend.parser.IParser;
import com.interestfriend.utils.SharedUtils;

public class GrowthList extends AbstractData {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final String GROWTH_LIST_API = "GetGrowthListServlet";
	private final int GROUTH_COUNT = 20;

	private List<Growth> growths = new ArrayList<Growth>();
	private List<Growth> writeGrowths = new ArrayList<Growth>();

	private int cid = 0;

	private String refushTime = "0";
	private int refushState = 1;// 1 上拉刷新 2 加载更多

	public int getRefushState() {
		return refushState;
	}

	public void setRefushState(int refushState) {
		this.refushState = refushState;
	}

	public GrowthList(int cid) {
		this.cid = cid;
	}

	public List<Growth> getWriteGrowths() {
		return writeGrowths;
	}

	public void setWriteGrowths(List<Growth> writeGrowths) {
		this.writeGrowths = writeGrowths;
	}

	public String getRefushTime() {
		return refushTime;
	}

	public void setRefushTime(String refushTime) {
		this.refushTime = refushTime;
	}

	public List<Growth> getGrowths() {
		sort();
		return growths;
	}

	public void setGrowths(List<Growth> growths) {
		this.growths = growths;
	}

	public int getCid() {
		return cid;
	}

	public void setCid(int cid) {
		this.cid = cid;
	}

	private void sort() {
		Collections.sort(growths, new Comparator<Growth>() {
			@Override
			public int compare(Growth lhs, Growth rhs) {
				return rhs.getPublished().compareTo(lhs.getPublished());
			}
		});

	}

	public RetError refushGrowth() {
		IParser parser = new GrowthListParser();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("cid", cid);
		params.put("refushTime", refushTime);
		params.put("refushState", refushState);
		Result ret = ApiRequest.request(GROWTH_LIST_API, params, parser);
		if (ret.getStatus() == RetStatus.SUCC) {
			GrowthList lists = (GrowthList) ret.getData();
			updateGrowth(lists.getGrowths());
			writeGrowths.addAll(lists.getGrowths());
			return RetError.NONE;
		} else {
			return ret.getErr();
		}
	}

	private void delById(int growth_id) {
		for (Iterator<Growth> it = growths.iterator(); it.hasNext();) {
			if (it.next().getGrowth_id() == growth_id) {
				it.remove();
				break;
			}
		}

	}

	private void updateGrowth(List<Growth> lists) {
		for (Growth growth : lists) {
			if (growth.getStatus() == Status.UPDATE) {
				delById(growth.getGrowth_id());
			}
			this.growths.add(growth);
		}
	}

	public void writeGrowth(SQLiteDatabase db) {
		List<Growth> newGrowths = new ArrayList<Growth>();
		for (Growth growth : writeGrowths) {
			if (growth.getStatus() == Status.UPDATE) {
				db.delete(Const.GROWTHS_TABLE_NAME, "growth_id=?",
						new String[] { growth.getGrowth_id() + "" });
				db.delete(Const.GROWTH_IMAGE_TABLE_NAME, "growth_id=?",
						new String[] { growth.getGrowth_id() + "" });
				db.delete(Const.COMMENT_TABLE_NAME, "growth_id=?",
						new String[] { growth.getGrowth_id() + "" });
				db.delete(Const.PRAISE_TABLE_NAME, "growth_id=?",
						new String[] { growth.getGrowth_id() + "" });
			}
			newGrowths.add(growth);
		}
		for (Growth growth : newGrowths) {

			growth.write(db);
		}
		writeGrowths.clear();
		if (refushState == 1) {
			Cursor cursor = db.query(Const.GROWTHS_TABLE_NAME,
					new String[] { "_id" }, "cid=?", new String[] { cid + "" },
					null, null, null);
			if (cursor.getCount() > GROUTH_COUNT) {
				cursor.move(GROUTH_COUNT);
				int id = cursor.getInt(cursor.getColumnIndex("_id"));
				db.delete(Const.GROWTHS_TABLE_NAME, "_id> ? and cid=?",
						new String[] { id + "", cid + "" });
				db.delete(Const.GROWTH_IMAGE_TABLE_NAME, "_id> ? and cid=?",
						new String[] { id + "", cid + "" });
				db.delete(Const.PRAISE_TABLE_NAME, "_id> ?", new String[] { id
						+ "" });
				db.delete(Const.COMMENT_TABLE_NAME, "_id> ? ",
						new String[] { id + "" });
			}
		}
	}

	@Override
	public void read(SQLiteDatabase db) {
		// read growth basic info
		Cursor cursor = db.query(Const.GROWTHS_TABLE_NAME, new String[] {
				"growth_id", "content", "publisher_id", "time",
				"publisher_name", "publisher_avatar", "isPraise",
				"praise_count", "last_update_time" }, "cid=?",
				new String[] { cid + "" }, null, null, null);
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			for (int j = 0; j < cursor.getCount(); j++) {
				int growth_id = cursor.getInt(cursor
						.getColumnIndex("growth_id"));
				int publisher = cursor.getInt(cursor
						.getColumnIndex("publisher_id"));
				String content = cursor.getString(cursor
						.getColumnIndex("content"));
				String publisher_name = cursor.getString(cursor
						.getColumnIndex("publisher_name"));
				String publisher_avatar = cursor.getString(cursor
						.getColumnIndex("publisher_avatar"));
				String time = cursor.getString(cursor.getColumnIndex("time"));
				String last_update_time = cursor.getString(cursor
						.getColumnIndex("last_update_time"));
				int isPraise = cursor.getInt(cursor.getColumnIndex("isPraise"));
				int praise_count = cursor.getInt(cursor
						.getColumnIndex("praise_count"));
				Growth growth = new Growth();
				growth.setCid(cid);
				growth.setContent(content);
				growth.setGrowth_id(growth_id);
				growth.setPublished(time);
				growth.setPublisher_id(publisher);
				growth.setPublisher_avatar(publisher_avatar);
				growth.setPublisher_name(publisher_name);
				growth.setPraise(isPraise > 0);
				growth.setPraise_count(praise_count);
				growth.setLast_update_time(last_update_time);
				// read growth images
				List<GrowthImage> images = new ArrayList<GrowthImage>();
				Cursor cursor2 = db.query(Const.GROWTH_IMAGE_TABLE_NAME,
						new String[] { "img_id", "img" }, "growth_id=?",
						new String[] { growth_id + "" }, null, null, null);
				if (cursor2.getCount() > 0) {
					cursor2.moveToFirst();
					for (int i = 0; i < cursor2.getCount(); i++) {
						int imgId = cursor2.getInt(cursor2
								.getColumnIndex("img_id"));
						String img = cursor2.getString(cursor2
								.getColumnIndex("img"));
						GrowthImage image = new GrowthImage(cid, growth_id,
								imgId, img);
						images.add(image);
						cursor2.moveToNext();
					}
					growth.setImages(images);
				}
				cursor2.close();

				// read comment

				List<Comment> comments = new ArrayList<Comment>();

				Cursor cursor3 = db.query(Const.COMMENT_TABLE_NAME,
						new String[] { "comment_id", "comment_time",
								"comment_content", "publisher_id",
								"publisher_name", "publisher_avatar",
								"reply_someone_id", "reply_someone_name" },
						"growth_id=?", new String[] { growth_id + "" }, null,
						null, null);
				if (cursor3.getCount() > 0) {
					cursor3.moveToFirst();
					for (int i = 0; i < cursor3.getCount(); i++) {
						int comment_id = cursor3.getInt(cursor3
								.getColumnIndex("comment_id"));
						String comment_time = cursor3.getString(cursor3
								.getColumnIndex("comment_time"));
						String comment_content = cursor3.getString(cursor3
								.getColumnIndex("comment_content"));
						int publisher_id = cursor3.getInt(cursor3
								.getColumnIndex("publisher_id"));
						publisher_name = cursor3.getString(cursor3
								.getColumnIndex("publisher_name"));
						publisher_avatar = cursor3.getString(cursor3
								.getColumnIndex("publisher_avatar"));
						int reply_someone_id = cursor3.getInt(cursor3
								.getColumnIndex("reply_someone_id"));
						String reply_someone_name = cursor3.getString(cursor3
								.getColumnIndex("reply_someone_name"));
						Comment comment = new Comment();
						comment.setComment_content(comment_content);
						comment.setComment_id(comment_id);
						comment.setPublisher_name(publisher_name);
						comment.setPublisher_avatar(publisher_avatar);
						comment.setComment_time(comment_time);
						comment.setPublisher_id(publisher_id);
						comment.setReply_someone_name(reply_someone_name);
						comment.setReply_someone_id(reply_someone_id);
						comments.add(comment);
						cursor3.moveToNext();
					}
					sortComment(comments);
					growth.setComments(comments);
					int index = comments.size() > 2 ? 2 : comments.size();
					for (int i = 0; i < index; i++) {
						growth.getCommentsListView().add(comments.get(i));
					}
				}
				cursor3.close();
				List<Praise> praises = new ArrayList<Praise>();
				Cursor cursor4 = db.query(Const.PRAISE_TABLE_NAME,
						new String[] { "user_id", "user_avatar" },
						"growth_id=?", new String[] { growth_id + "" }, null,
						null, null);
				if (cursor4.getCount() > 0) {
					cursor4.moveToFirst();
					for (int i = 0; i < cursor4.getCount(); i++) {
						int user_id = cursor4.getInt(cursor4
								.getColumnIndex("user_id"));
						String user_avatar = cursor4.getString(cursor4
								.getColumnIndex("user_avatar"));
						Praise praise = new Praise();
						praise.setUser_avatar(user_avatar);
						praise.setUser_id(user_id);
						praises.add(praise);
						cursor4.moveToNext();
					}
					growth.setPraises(praises);
				}
				cursor4.close();
				growths.add(growth);
				cursor.moveToNext();
			}
			cursor.close();
		}
	}

	public void sortComment(List<Comment> comments) {
		Collections.sort(comments, new Comparator<Comment>() {
			@Override
			public int compare(Comment lhs, Comment rhs) {
				return rhs.getComment_time().compareTo(lhs.getComment_time());
			}
		});

	}
}
