package com.attendancemanager;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.attendancemanager.adapters.BottomSheetAdapter;
import com.attendancemanager.adapters.TimeTableAdapter;
import com.attendancemanager.data.DBHelper;
import com.attendancemanager.data.Subject;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import github.com.st235.lib_expandablebottombar.ExpandableBottomBar;

public class TimeTableFragment extends Fragment {

    public static final String[] DAY_NAMES = new String[]{"Monday", "Tuesday", "Wednesday", "Thursday",
            "Friday", "Saturday", "Sunday"};
    private static final String TAG = "TimeTableFragment";

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private DBHelper dbHelper;
    private ExtendedFloatingActionButton addButtonFab;
    private FloatingActionButton floatingActionButton;
    private RecyclerView mBottomSheetRecyclerView;
    private ConstraintLayout mBottomSheetLayout;
    private ExpandableBottomBar bottomNavBar;
    private List<Subject> mAllSubjectList;
    private BottomSheetBehavior mBottomSheetBehavior;
    private BottomSheetAdapter mBottomSheetAdapter;

    public TimeTableFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_time_table, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbar = view.findViewById(R.id.time_table_toolbar);
        tabLayout = view.findViewById(R.id.tab_layout);
        bottomNavBar = getActivity().findViewById(R.id.bottom_bar);
        viewPager = view.findViewById(R.id.day_view_pager);
        floatingActionButton = view.findViewById(R.id.floatingActionButton);
        addButtonFab = view.findViewById(R.id.add_extended_fab);
        mBottomSheetLayout = view.findViewById(R.id.bottom_sheet_constraint_layout);
        mBottomSheetRecyclerView = view.findViewById(R.id.bottom_sheet_recycler_view);
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        toolbar.setTitleTextAppearance(getContext(), R.style.RubixFontStyle);
        TimeTableViewPagerAdapter pagerAdapter = new TimeTableViewPagerAdapter(getChildFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        Calendar calendar = Calendar.getInstance();
        Date date = new Date();
        calendar.setTime(date);
        viewPager.setCurrentItem((calendar.get(Calendar.DAY_OF_WEEK) + 12) % 7);
        viewPager.setOffscreenPageLimit(3);

        dbHelper = new DBHelper(getContext());
        mAllSubjectList = dbHelper.getAllSubjects();

        floatingActionButton.setOnClickListener(v -> {
            if (addButtonFab.getVisibility() == View.VISIBLE) {
                addButtonFab.setVisibility(View.GONE);
                floatingActionButton.setImageResource(R.drawable.ic_edit);
            } else {
                addButtonFab.setVisibility(View.VISIBLE);
                floatingActionButton.setImageResource(R.drawable.ic_check);
            }
        });

        addButtonFab.setOnClickListener(v -> mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED));

        mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheetLayout);
        mBottomSheetBehavior.setDraggable(true);
        mBottomSheetBehavior.setPeekHeight(0);
        mBottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    addButtonFab.setVisibility(View.VISIBLE);
                    floatingActionButton.setVisibility(View.VISIBLE);
                } else {
                    addButtonFab.setVisibility(View.GONE);
                    floatingActionButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        mBottomSheetAdapter = new BottomSheetAdapter(mAllSubjectList);
        mBottomSheetAdapter.setOnAddButtonClickListener(position -> {
            viewPager.getCurrentItem();
            dbHelper.insertSubjectToDayTable(pagerAdapter.getPageTitle(viewPager.getCurrentItem()).toString(), new String[]{mAllSubjectList.get(position).getSubjectName()});
        });

        mBottomSheetRecyclerView.setAdapter(mBottomSheetAdapter);
        mBottomSheetRecyclerView.setHasFixedSize(true);
        mBottomSheetRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mBottomSheetRecyclerView.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            v.onTouchEvent(event);
            return true;
        });
        mBottomSheetRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

    }

    public static class DayFragment extends Fragment {

        private static final String ARG_DAY_NAME = "argDayName";
        private RecyclerView timeTableRecyclerView;
        private TimeTableAdapter timeTableAdapter;
        private DBHelper dbHelper;
        private List<Subject> daySubjectList;
        private String getArgDayName;

        public static DayFragment newInstance(String day) {
            /* Create new instance with the ARG_DAY_NAME */

            DayFragment dayFragment = new DayFragment();
            final Bundle args = new Bundle();
            args.putString(ARG_DAY_NAME, day);
            dayFragment.setArguments(args);
            return dayFragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getArgDayName = getArguments().getString(ARG_DAY_NAME);
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_day, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            timeTableRecyclerView = view.findViewById(R.id.time_table_recycler_view);
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            dbHelper = new DBHelper(getContext());
            daySubjectList = dbHelper.getSubjectsOfDay(getArgDayName);
            timeTableAdapter = new TimeTableAdapter(daySubjectList);
            timeTableRecyclerView.setAdapter(timeTableAdapter);
            timeTableRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            timeTableRecyclerView.setHasFixedSize(true);
        }
    }

    private static class TimeTableViewPagerAdapter extends FragmentPagerAdapter {

        public TimeTableViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return DayFragment.newInstance(DAY_NAMES[position]);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return DAY_NAMES[position];
        }

        @Override
        public int getCount() {
            return 7;
        }
    }
}