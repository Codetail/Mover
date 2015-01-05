package codetail.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

import codetail.animation.ViewAnimationUtils;
import codetail.components.R;
import codetail.text.RobotoTextView;
import codetail.utils.ResourceUtils;
import codetail.utils.ViewUtils;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class Toolbar extends FrameLayoutCompat {

    ImageView mHomeButton;
    RobotoTextView mTitle;
    TextView mSubtitle;

    View mTitleLayout;
    SearchView mSearchView;

    final int mActionButtonSize;
    final int mActionButtonPadding;

    public Toolbar(Context context) {
        this(context, null);
    }

    public Toolbar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Toolbar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setClipChildren(false);

        LayoutInflater factory = LayoutInflater.from(context);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CodetailToolbar);
        Drawable drawable = array.getDrawable(R.styleable.CodetailToolbar_buttonsSelector);

        Resources r = getResources();

        mActionButtonSize = r.getDimensionPixelSize(R.dimen.toolbar_action_btn_mtrl_size);
        mActionButtonPadding = r.getDimensionPixelSize(R.dimen.toolbar_edges_margin);

        mHomeButton = new ImageView(context);
        mHomeButton.setClickable(true);
        mHomeButton.setPadding(mActionButtonPadding, mActionButtonPadding, mActionButtonPadding, mActionButtonPadding);
        mHomeButton.setColorFilter(array.getColor(R.styleable.CodetailToolbar_homeTintColor, Color.TRANSPARENT),
                PorterDuff.Mode.SRC_ATOP);
        mHomeButton.setImageDrawable(array.getDrawable(R.styleable.CodetailToolbar_homeIcon));

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(mActionButtonSize, mActionButtonSize);

        mTitleLayout = factory.inflate(R.layout.acb_title_layout, this, false);
        mTitle = ViewUtils.findView(mTitleLayout, R.id.action_bar_title);

        mTitle.setTextAppearance(context, array.getResourceId(R.styleable.CodetailToolbar_titleAppearance,
                R.style.TextAppearance_AppCompat_Medium));
        mTitle.setText(array.getText(R.styleable.CodetailToolbar_titleText));
        mTitle.setTextColor(array.getColor(R.styleable.CodetailToolbar_titleColor, Color.BLACK));
        mTitle.setFontFamily("Roboto-Bold");

        mSubtitle = ViewUtils.findView(mTitleLayout, R.id.action_bar_subtitle);
        mSubtitle.setTextAppearance(context, array.getResourceId(R.styleable.CodetailToolbar_subtitleAppearance,
                R.style.TextAppearance_AppCompat_Medium));
        mSubtitle.setText(array.getText(R.styleable.CodetailToolbar_subtitleText));
        mSubtitle.setTextColor(array.getColor(R.styleable.CodetailToolbar_subtitleColor, Color.BLACK));

        mSearchView = new SearchView(context);

        addView(mHomeButton, params);

        params = generateLayoutParams(mTitleLayout.getLayoutParams());
        params.leftMargin = ResourceUtils.getPixelSize(R.dimen.toolbar_title_layout_left);
        params.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;

        addView(mTitleLayout, params);

        params = new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        params.gravity = Gravity.RIGHT;

        addView(mSearchView, params);


        if(drawable == null){
            mHomeButton.setBackgroundResource(R.drawable.overlay_background);
        }else{
            mSearchView.setSearchButtonBackground(drawable);
            ViewUtils.setBackground(mHomeButton, drawable);
        }

        array.recycle();
    }

    public void setOnTitleClickListener(OnClickListener listener) {
        mTitleLayout.setOnClickListener(listener);
    }

    public void setDropdownIcon(Drawable dropdownIcon) {
        mTitle.setCompoundDrawablePadding(ResourceUtils.dp2px(getContext(), 8));
        mTitle.setCompoundDrawablesWithIntrinsicBounds(null, null, dropdownIcon, null);
    }

    public View getTitleLayout() {
        return mTitleLayout;
    }

    public SearchView getSearchView() {
        return mSearchView;
    }

    public void setTitleVisible(boolean visibility) {
        ViewUtils.setVisibility(mTitle, visibility);
    }

    public void doCreateAnimation() {
        final int height = getHeight();

        ViewHelper.setTranslationY(mHomeButton, -height);
        ViewHelper.setTranslationY(mTitleLayout, -height);
        ViewHelper.setTranslationY(mSearchView, -height);


        ViewPropertyAnimator.animate(mHomeButton)
                .translationY(0)
                .setDuration(ViewAnimationUtils.DEFAULT_DURATION)
                .setInterpolator(ViewAnimationUtils.ACCELERATE)
                .setStartDelay(120);

        ViewPropertyAnimator.animate(mTitleLayout)
                .translationY(0)
                .setDuration(ViewAnimationUtils.DEFAULT_DURATION)
                .setInterpolator(ViewAnimationUtils.ACCELERATE)
                .setStartDelay(240);


        ViewPropertyAnimator.animate(mSearchView)
                .translationY(0)
                .setDuration(ViewAnimationUtils.DEFAULT_DURATION)
                .setInterpolator(ViewAnimationUtils.ACCELERATE)
                .setStartDelay(360)
                .start();
    }

    public void setTitle(CharSequence title) {
        mTitle.setText(title);
    }

    public void setTitle(int title) {
        mTitle.setText(title);
    }

    public void setOnNavigationClickListener(OnClickListener menuClickListener) {
        mHomeButton.setOnClickListener(menuClickListener);
    }

    public void setNavigationIcon(Drawable drawable) {
        mHomeButton.setImageDrawable(drawable);
    }

    public Drawable getNavigationIcon() {
        return mHomeButton.getDrawable();
    }
}
