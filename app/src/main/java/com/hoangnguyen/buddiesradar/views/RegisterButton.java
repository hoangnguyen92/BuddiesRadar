package com.hoangnguyen.buddiesradar.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ToggleButton;

import java.util.List;

import com.hoangnguyen.buddiesradar.models.Room;
import com.hoangnguyen.buddiesradar.models.User;

/**
 * Created by tl on 19.02.15.
 */
public class RegisterButton extends ToggleButton {

    public RegisterButton(Context context) {
        super(context);
    }

    public RegisterButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setData(User currentUser, Room selectedRoom) {
        List<User> users = selectedRoom.getUsers();

        setChecked(users.contains(currentUser));
    }
}
