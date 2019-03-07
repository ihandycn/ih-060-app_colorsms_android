package com.android.messaging.smsshow;

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class DynamicalPagerAdapter extends PagerAdapter {
    // This holds all the currently displayable views, in order from left to right.
    private ArrayList<MessageBoxConversationItemView> views = new ArrayList<>();

    //-----------------------------------------------------------------------------
    // Used by ViewPager.  "Object" represents the page; tell the ViewPager where the
    // page should be displayed, from left-to-right.  If the page no longer exists,
    // return POSITION_NONE.
    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public int getItemPosition(@NonNull Object object) {
        int index = views.indexOf(object);
        if (index == -1)
            return POSITION_NONE;
        else
            return index;
    }

    //-----------------------------------------------------------------------------
    // Used by ViewPager.  Called when ViewPager needs a page to display; it is our job
    // to add the page to the container, which is normally the ViewPager itself.  Since
    // all our pages are persistent, we simply retrieve it from our "views" ArrayList.
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        MessageBoxConversationItemView v = views.get(position);
        container.addView(v);
        return v;
    }

    //-----------------------------------------------------------------------------
    // Used by ViewPager.  Called when ViewPager no longer needs a page to display; it
    // is our job to remove the page from the container, which is normally the
    // ViewPager itself.  Since all our pages are persistent, we do nothing to the
    // contents of our "views" ArrayList.
    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView(views.get(position));
    }

    //-----------------------------------------------------------------------------
    // Used by ViewPager; can be used by app as well.
    // Returns the total number of pages that the ViewPage can display.  This must
    // never be 0.
    @Override
    public int getCount() {
        return views.size();
    }

    //-----------------------------------------------------------------------------
    // Used by ViewPager.
    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    //-----------------------------------------------------------------------------
    // Add "view" to right end of "views".
    // Returns the position of the new view.
    // The app should call this to add pages; not used by ViewPager.
    public int addView(MessageBoxConversationItemView v) {
        return addView(v, views.size());
    }

    //-----------------------------------------------------------------------------
    // Add "view" at "position" to "views".
    // Returns position of new view.
    // The app should call this to add pages; not used by ViewPager.
    public int addView(MessageBoxConversationItemView v, int position) {
        views.add(position, v);
        return position;
    }


    public ArrayList<MessageBoxConversationItemView> getViews() {
        return views;
    }

    //-----------------------------------------------------------------------------
    // Returns the "view" at "position".
    // The app should call this to retrieve a view; not used by ViewPager.
    public MessageBoxConversationItemView getView(int position) {
        return views.get(position);
    }

    // Other relevant methods:

    // finishUpdate - called by the ViewPager - we don't care about what pages the
    // pager is displaying so we don't use this method.
}
