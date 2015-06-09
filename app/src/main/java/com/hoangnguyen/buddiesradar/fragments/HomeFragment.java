package com.hoangnguyen.buddiesradar.fragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;

import com.hoangnguyen.buddiesradar.R;
import com.hoangnguyen.buddiesradar.activities.MainActivity;
import com.hoangnguyen.buddiesradar.adapters.RoomQueryAdapter;
import com.hoangnguyen.buddiesradar.data.LocalDb;
import com.hoangnguyen.buddiesradar.models.Room;
import com.hoangnguyen.buddiesradar.models.User;
import com.hoangnguyen.buddiesradar.views.RoomItem;

/**
 * Created by tl on 09.02.15.
 */
public class HomeFragment extends ListFragment implements SwipeRefreshLayout.OnRefreshListener{
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static HomeFragment newInstance(int sectionNumber) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    private LocalDb mLocalDb;

    private User mCurrentUser;
    private Room mMyRoom;
    private RoomQueryAdapter mRecentRoomQueryAdapter;

    private RoomItem mRiMyRoom;
    private TextView mTvNoRoomInfo;
    private View mHeaderView;
    private SwipeRefreshLayout mSrlRefresh;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        mHeaderView = inflater.inflate(R.layout.fragment_home_header, null);

        initData();
        initViews(rootView);
        fetchDataToViews();

        return rootView;
    }

    private void initData() {
        mLocalDb = LocalDb.getInstance();
        mCurrentUser = mLocalDb.getCurrentUser();
        mMyRoom = mCurrentUser.getRoom();

        mRecentRoomQueryAdapter = new RoomQueryAdapter(getActivity(), null);
    }

    private void initViews(View rootView) {
        mRiMyRoom = (RoomItem) mHeaderView.findViewById(R.id.riMyRoom);
        mTvNoRoomInfo = (TextView) mHeaderView.findViewById(R.id.tvNoRoomInfo);
        mSrlRefresh = (SwipeRefreshLayout) rootView.findViewById(R.id.srlRefresh);

        mSrlRefresh.setColorSchemeColors(
                getResources().getColor(R.color.br_dark_background));

        mSrlRefresh.setOnRefreshListener(this);
    }

    private void fetchDataToViews() {
        if (mMyRoom != null) {
            roomOwnedVisibility();

            mMyRoom.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject room, ParseException e) {
                    mRiMyRoom.setData(mCurrentUser, mMyRoom);
                }
            });
        } else {
            roomNotOwnedVisibility();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getListView().addHeaderView(mHeaderView);
        setListAdapter(mRecentRoomQueryAdapter);
    }

    private void roomNotOwnedVisibility() {
        mRiMyRoom.setVisibility(View.GONE);
        mTvNoRoomInfo.setVisibility(View.VISIBLE);
    }

    private void roomOwnedVisibility() {
        mRiMyRoom.setVisibility(View.VISIBLE);
        mTvNoRoomInfo.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER),
                    null);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        setListAdapter(null);
    }

    @Override
    public void onRefresh() {
        fetchDataToViews();
        mRecentRoomQueryAdapter = new RoomQueryAdapter(getActivity(), null);
        setListAdapter(mRecentRoomQueryAdapter);

        mSrlRefresh.setRefreshing(false);
    }
}
