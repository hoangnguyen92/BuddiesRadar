package com.hoangnguyen.buddiesradar.views;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import com.hoangnguyen.buddiesradar.Constants;
import com.hoangnguyen.buddiesradar.R;
import com.hoangnguyen.buddiesradar.activities.ProfileActivity;
import com.hoangnguyen.buddiesradar.activities.RoomActivity;
import com.hoangnguyen.buddiesradar.models.Room;
import com.hoangnguyen.buddiesradar.models.User;
import com.hoangnguyen.buddiesradar.tools.NotificationHelper;

/**
 * Created by tl on 18.02.15.
 */
public class RoomItem extends LinearLayout implements View.OnClickListener {

    private static final String USER_ID = "USER_ID";

    private TextView mTvRoomName;
    private TextView mTvUsername;
    private ParseImageView mPivAvatar;
    private RegisterButton mRbRegister;
    private Button mBtnJoin;

    private User mCurrentUser;
    private Room mRoom;

    public RoomItem(Context context) {
        super(context);
        init();
    }

    public RoomItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.item_room, this);

        mTvRoomName = (TextView) findViewById(R.id.tvRoomName);
        mTvUsername = (TextView) findViewById(R.id.tvUsername);
        mPivAvatar = (ParseImageView) findViewById(R.id.pivAvatar);
        mRbRegister = (RegisterButton) findViewById(R.id.rbRegister);
        mBtnJoin = (Button) findViewById(R.id.btnJoin);

        mTvUsername.setOnClickListener(this);
        mPivAvatar.setOnClickListener(this);
        mBtnJoin.setOnClickListener(this);
        mRbRegister.setOnClickListener(this);
    }

    public void setData(User currentUser,Room room) {
        mRoom = room;
        mCurrentUser = currentUser;

        mRoom.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (mRoom.isDataAvailable()) {
                    mTvRoomName.setText(mRoom.getName());

                    final User user = mRoom.getCreatedBy();

                    user.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        @Override
                        public void done(ParseObject parseObject, ParseException e) {
                            mTvUsername.setText(user.getUsername());
                            mPivAvatar.setParseFile(user.getAvatar());

                            if (user.getAvatar() != null) {
                                mPivAvatar.loadInBackground();
                            } else {
                                mPivAvatar.setBackground(getResources().getDrawable(R.drawable.ic_avatar));
                            }
                        }
                    });

                    mCurrentUser.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject parseObject, ParseException e) {
                            mRbRegister.setData(mCurrentUser, mRoom);

                            if (mRbRegister.isChecked()) {
                                setRegisteredVisibilityAndStyle();
                            } else {
                                setNotRegisteredVisibilityAndStyle();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.tvUsername:
            case R.id.pivAvatar:
                goToProfile();
                break;
            case R.id.rbRegister:
                onRegisterClicked();
                break;
            case R.id.btnJoin:
                onJoinClicked();
                break;
        }
    }

    private void onRegisterClicked() {
        if (!mRbRegister.isChecked()) {
            if (mRoom.getUsers().contains(mCurrentUser)) {
                setNotRegisteredVisibilityAndStyle();
                removeUserFromRoom();
            } else {
                setNotRegisteredVisibilityAndStyle();
            }
        } else {
            if (!mRoom.getUsers().contains(mCurrentUser)) {
                checkForPassKey();
            } else {
                setRegisteredVisibilityAndStyle();
            }
        }
    }

    private void goToProfile() {
        Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
        profileIntent.putExtra(USER_ID, mRoom.getCreatedBy().getObjectId());
        getContext().startActivity(profileIntent);
    }

    private void onJoinClicked() {
        goToRoom();
    }

    private void checkForPassKey() {
        setNotRegisteredVisibilityAndStyle();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        LayoutInflater inflater = LayoutInflater.from(getContext());
        final View dvCreateRoom = inflater.inflate(R.layout.dialog_passkey_check, null);

        final EditText etPassKey = (EditText) dvCreateRoom.findViewById(R.id.etPassKey);

        builder.setView(dvCreateRoom)
                .setTitle(getContext().getString(R.string.check_keypass_title))
                .setPositiveButton(getContext().getString(R.string.room_register_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String passKey = etPassKey.getText().toString().trim();

                        if (passKey.equals(mRoom.getPassKey())) {
                            addUserToRoom();
                        } else {
                            NotificationHelper.alert(getContext(),
                                    getContext().getString(R.string.dialog_error_title),
                                    getContext().getString(R.string.passkey_incorrect_message));
                        }
                    }
                })
                .setNegativeButton(getContext().getString(R.string.cancel_btn), null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void removeUserFromRoom() {
        mRoom.getUsers().remove(mCurrentUser);

        mRoom.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    setNotRegisteredVisibilityAndStyle();
                } else {
                    NotificationHelper.alert(getContext(),
                            getContext().getString(R.string.dialog_error_title),
                            e.getMessage());
                }
            }
        });
    }

    private void addUserToRoom() {
        mRoom.getUsers().add(mCurrentUser);

        mRoom.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    setRegisteredVisibilityAndStyle();
                } else {
                    setNotRegisteredVisibilityAndStyle();
                    NotificationHelper.alert(getContext(),
                            getContext().getString(R.string.dialog_error_title),
                            e.getMessage());
                }
            }
        });
    }

    private void setNotRegisteredVisibilityAndStyle() {
        mRbRegister.setChecked(false);
        mBtnJoin.setVisibility(View.GONE);
        mRbRegister.setBackgroundColor(getResources().getColor(R.color.br_button));
        mRbRegister.setTextColor(getResources().getColor(R.color.br_text));
    }

    private void setRegisteredVisibilityAndStyle() {
        mRbRegister.setChecked(true);
        mBtnJoin.setVisibility(View.VISIBLE);
        mRbRegister.setBackgroundColor(getResources().getColor(R.color.br_toggle_on));
        mRbRegister.setTextColor(getResources().getColor(android.R.color.white));
    }

    private void goToRoom() {
        Intent roomIntent = new Intent(getContext(), RoomActivity.class);
        roomIntent.putExtra(Constants.ROOM_ID, mRoom.getObjectId());
        getContext().startActivity(roomIntent);
    }
}
