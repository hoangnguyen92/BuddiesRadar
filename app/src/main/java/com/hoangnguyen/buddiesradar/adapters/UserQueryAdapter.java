package com.hoangnguyen.buddiesradar.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import java.util.ArrayList;
import java.util.List;

import com.hoangnguyen.buddiesradar.Constants;
import com.hoangnguyen.buddiesradar.data.LocalDb;
import com.hoangnguyen.buddiesradar.models.Follow;
import com.hoangnguyen.buddiesradar.models.User;
import com.hoangnguyen.buddiesradar.views.UserItem;

/**
 * Created by tl on 17.02.15.
 */
public class UserQueryAdapter extends ParseQueryAdapter<User> {

    private static final int LIMIT = 50;

    public UserQueryAdapter(final Context context, final String searchQuery, final String category, final User user) {
        super(context, new QueryFactory<User>() {

            @Override
            public ParseQuery<User> create() {
                ParseQuery<User> query = new ParseQuery<>(Constants.USER_TABLE);
                query.orderByDescending(Constants.PARSE_COL_CREATED_AT);

                if (category.equals(Constants.SEARCH)) {
                    query.setLimit(LIMIT)
                            .whereNotEqualTo(Constants.PARSE_COL_OBJECT_ID,
                                LocalDb.getInstance().getCurrentUser().getObjectId());

                } else if (category.equals(Constants.FOLLOWING) && user != null) {
                    Follow follow = getFollowObject(user);

                    List<User> users = follow.getFollowings();
                    List<String> objectIds = fetchUsersObjectIds(users);

                    query.whereContainedIn(Constants.PARSE_COL_OBJECT_ID, objectIds);
                } else if (category.equals(Constants.FOLLOWER) && user != null) {
                    Follow follow = getFollowObject(user);

                    List<User> users = follow.getFollowers();
                    List<String> objectIds = fetchUsersObjectIds(users);

                    query.whereContainedIn(Constants.PARSE_COL_OBJECT_ID, objectIds);
                }

                if (searchQuery != null) {
                    query.whereContains(Constants.USER_COL_USERNAME, searchQuery);
                }

                return query;
            }
        });
    }

    private static List<String> fetchUsersObjectIds(List<User> users) {
        List<String> objectIds = new ArrayList<>();

        for (User user: users) {
            objectIds.add(user.getObjectId());
        }
        return objectIds;
    }

    private static Follow getFollowObject(User user) {
        Follow follow = null;

        try {
            follow = user.getFollow();
            follow.fetchIfNeeded();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return follow;
    }

    @Override
    public View getItemView(User user, View v, ViewGroup parent) {
        if (v == null) {
            v = new UserItem(getContext());
        }

        ((UserItem) v).setData(user);

        return v;
    }
}
