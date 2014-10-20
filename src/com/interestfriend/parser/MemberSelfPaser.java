package com.interestfriend.parser;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.interestfriend.data.CircleMember;
import com.interestfriend.data.CircleMemberList;
import com.interestfriend.data.enums.CircleMemberState;
import com.interestfriend.data.result.Result;
import com.interestfriend.utils.SharedUtils;

public class MemberSelfPaser implements IParser {

	@Override
	public Result parse(JSONObject jsonObj) throws Exception {
		if (jsonObj == null) {
			return Result.defContentErrorResult();
		}
		int circle_id = jsonObj.getInt("circle_id");
		long lastReqTime = jsonObj.getLong("lastReqTime");
		SharedUtils.setCircleMemberLastReqTime(circle_id, lastReqTime);
		JSONArray jsonArr = jsonObj.getJSONArray("members");
		if (jsonArr == null) {
			return Result.defContentErrorResult();
		}
		List<CircleMember> lists = new ArrayList<CircleMember>();
		for (int i = 0; i < jsonArr.length(); i++) {
			JSONObject obj = (JSONObject) jsonArr.opt(i);
			int userID = obj.getInt("userID");
			String userName = obj.getString("userName");
			String userAvatar = obj.getString("userAvatar");
			String userGender = obj.getString("userGender");
			String userBirthday = obj.getString("userBirthday");
			String userRegisterTime = obj.getString("userRegisterTime");
			String userChatId = obj.getString("userChatId");
			String pinyinFir = obj.getString("pinYinFir");
			String sortKey = obj.getString("sortKey");
			String user_state = obj.getString("userState");
			String user_declaration = obj.getString("userDeclaration");
			String user_description = obj.getString("userDescription");

			CircleMember member = new CircleMember();
			member.setUser_id(userID);
			member.setUser_name(userName);
			member.setUser_avatar(userAvatar);
			member.setUser_birthday(userBirthday);
			member.setUser_gender(userGender);
			member.setUser_register_time(userRegisterTime);
			member.setUser_chat_id(userChatId);
			member.setCircle_id(circle_id);
			member.setSortkey(sortKey);
			member.setPinyinFir(pinyinFir);
			member.setState(CircleMemberState.convert(user_state));
			member.setUser_declaration(user_declaration);
			member.setUser_description(user_description);
			lists.add(member);
		}
		CircleMemberList list = new CircleMemberList();
		list.setCircleMemberLists(lists);
		Result ret = new Result();
		ret.setData(list);
		return ret;
	}
}