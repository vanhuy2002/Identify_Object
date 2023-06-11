package com.example.identify_object.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.identify_object.History.HistoryFragment;
import com.example.identify_object.ScanFragment;
import com.example.identify_object.SettingsFragment;


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
                return new SettingsFragment();
            default:
                return new ScanFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
