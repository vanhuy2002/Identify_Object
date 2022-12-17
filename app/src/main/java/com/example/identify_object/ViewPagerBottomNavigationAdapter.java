package com.example.identify_object;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;


public class ViewPagerBottomNavigationAdapter extends FragmentStateAdapter {
    public ViewPagerBottomNavigationAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 1:
                return new HistoryFragment();
            case 2:
                return new StudyFragment();
            default:
                return new ScanFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
