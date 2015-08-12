package top.oply.oplayer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

/**
 * Created by young on 2015/8/12.
 */
public class PageViewAdaptor extends FragmentPagerAdapter {
    private ArrayList<Fragment> mFragments;

    public PageViewAdaptor(FragmentManager fm, ArrayList<Fragment> lst) {
        super(fm);
        mFragments = lst;
    }

    public PageViewAdaptor(FragmentManager fm) {
        super(fm);
        mFragments = new ArrayList<Fragment>();
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Bundle b = mFragments.get(position).getArguments();
        return b.getString("Title");
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }
}
