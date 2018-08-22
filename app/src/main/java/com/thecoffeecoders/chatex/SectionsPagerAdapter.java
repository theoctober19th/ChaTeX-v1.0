package com.thecoffeecoders.chatex;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by bikalpa on 1/4/2018.
 */

class SectionsPagerAdapter extends FragmentPagerAdapter {

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position){
           /* case 0: THIS WILL BE CODE IF WE INCLUDE REQUEST FRAGMENT ALSO. I HAVE REMOVED IT
                RequestsFragment requestsFragment = new RequestsFragment();
                return requestsFragment;
            case 1:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;
            case 2:
                FriendsFragment friendsFragment = new FriendsFragment();
                return  friendsFragment;
           */
            case 0:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;
            case 1:
                FriendsFragment friendsFragment = new FriendsFragment();
                return  friendsFragment;
            case 2:
                RequestsFragment requestsFragment = new RequestsFragment();
                return requestsFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;  //IF NEEDED REQUESTS FRAGMENT ALSO, MAKE THIS COUNT TO 3
    }

    public CharSequence getPageTitle(int position){
        switch (position){
            /* IF REQUESTS FRAGMENT IS ALSO INCLUDED
            case 0:
                return "Requests";
            case 1:
                return "Chats";
            case 2:
                return "Friends";
             */

            case 0:
                return "Chats";
            case 1:
                return "Friends";
            case 2:
                return "Requests";
            default:
                return null;
        }
    }
}
