package io.codetail.fragments;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import codetail.graphics.drawables.DrawableHelper;
import codetail.graphics.drawables.RippleDrawable;
import codetail.utils.ThemeUtils;
import codetail.utils.ViewUtils;
import codetail.widget.FrameLayoutCompat;
import io.codetail.watchme.R;

public class NavigationFragment extends BaseWatchMeFragment{

    public static final String USER_PICTURE_URL = "userPictureUrl";

    @InjectView(R.id.naviagition_container) LinearLayout mContainer;

    @InjectView(R.id.profile)  FrameLayoutCompat mProfile;
    @InjectView(R.id.avatar)   ImageView mAvatar;
    @InjectView(R.id.username) TextView mUsername;

    private int mSelectedPosition = -1;

    private int mPrimaryIconColor;
    private int mAccentIconColor;

    private ColorStateList mRippleOverlay;
    private CharSequence[] mNavigationItems;

    private AccountManager mAccountManager;

    final View.OnClickListener mOnNavigationSelected = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int selectedItem = mContainer.indexOfChild(v);

            ((DrawerLayout) getWatchMeActivity().findViewById(R.id.drawer_container))
                    .closeDrawer(GravityCompat.START);

            getSource().onNavigationItemSelected(getFragmentManager(), selectedItem);
            setSelected(selectedItem);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_navigation, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);

        ViewUtils.setVisibilityWithGoneFlag(mProfile, false);

        Context context = getActivity();
        mAccountManager = AccountManager.get(context);

        mPrimaryIconColor = ThemeUtils.getThemeColor(context, R.attr.navigationActionsPrimaryColor);
        mPrimaryIconColor = withAlpha(190, mPrimaryIconColor);

        mAccentIconColor = ThemeUtils.getThemeColor(context, R.attr.navigationActionsAccentColor);
        mAccentIconColor = withAlpha(190, mAccentIconColor);

        mRippleOverlay = ThemeUtils.getThemeColorStateList(context, R.attr.navigationRippleOverlayColor);

        populateNavigationMenu();
    }

    /**
     * @return current selected item position,
     *  return negative number if nothing selected
     */
    public int getSelectedPosition(){
        return mSelectedPosition;
    }

    /**
     * Populate navigation drawer with items
     */
    private void populateNavigationMenu() {
        LayoutInflater factory = LayoutInflater.from(getActivity());
        CharSequence[] navItems = getSource().getNavigationItems();
        Drawable[] icons = getSource().getNavigationIcons();

        for(int index = 0; index < navItems.length; index++){
            CharSequence item = navItems[index];
            Drawable icon = icons[index];
            DrawableHelper.setTint(icon, mPrimaryIconColor);

            TextView itemView = (TextView)
                    factory.inflate(R.layout.drawer_menu_item, mContainer, false);
            itemView.setText(item);
            itemView.setOnClickListener(mOnNavigationSelected);
            itemView.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);

            RippleDrawable.makeFor(itemView, mRippleOverlay, true);
            mContainer.addView(itemView);
        }

        mNavigationItems = navItems;
    }

    /**
     * Checks new selected item, and restore default state
     * of previously selected
     *
     * @param position position of item
     */
    public void setSelected(int position){
        if(mSelectedPosition != -1) {
            setSelectedInternal(mSelectedPosition, false);
        }

        setSelectedInternal(position, true);
        getWatchMeActivity().getToolbar().setTitle(mNavigationItems[position]);
        mSelectedPosition = position;
    }

    /**
     * Marks item view as selected or not
     * @see android.view.View#setSelected(boolean)
     *
     * @param position position of item
     * @param selected state of item
     */
    private void setSelectedInternal(int position, boolean selected){
        TextView item = (TextView) mContainer.getChildAt(position);
        item.setSelected(selected);

        DrawableHelper.setTint(item.getCompoundDrawables()[0], selected ? mAccentIconColor : mPrimaryIconColor);
    }

    static int withAlpha(int alpha, int color){
        return Color.argb(alpha, color, color, color);
    }

}
