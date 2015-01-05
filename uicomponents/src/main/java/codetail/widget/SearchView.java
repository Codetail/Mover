package codetail.widget;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Set;

import codetail.components.R;
import codetail.utils.ResourceUtils;
import codetail.utils.ViewUtils;

public class SearchView extends FrameLayoutCompat{

    QueryCallback mCallback;

    boolean isExpanded;

    ImageView mSearchButton;

    AutoCompleteTextView mSearchQuery;
    ArrayAdapter<String> mSuggestionAdapter;

    SharedPreferences mSearchQueriesCache;

    public SearchView(Context context) {
        this(context, null);
    }

    public SearchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SearchView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mSearchQueriesCache = context.getSharedPreferences("last_search_queries", Context.MODE_PRIVATE);

        Set<String> collection = mSearchQueriesCache.getAll().keySet();
        String[] suggestions = new String[collection.size()];
        collection.toArray(suggestions);

        mSuggestionAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1,
                android.R.id.text1, suggestions);

        Resources r = getResources();

        final int padding = r.getDimensionPixelSize(R.dimen.toolbar_edges_margin);
        final int size = r.getDimensionPixelSize(R.dimen.toolbar_action_btn_mtrl_size);

        mSearchButton = new ImageView(context);
        mSearchButton.setClickable(true);
        mSearchButton.setPadding(padding, padding, padding, padding);
        mSearchButton.setImageResource(R.drawable.abc_ic_search_api_mtrl_alpha);

        mSearchButton.setBackgroundResource(R.drawable.overlay_background);

        mSearchQuery = new AutoCompleteTextView(context);
        mSearchQuery.setPadding(0, 0, 0, 0);
        mSearchQuery.setImeOptions(EditorInfo.IME_ACTION_SEARCH | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        mSearchQuery.setInputType(EditorInfo.TYPE_CLASS_TEXT);
        mSearchQuery.setAdapter(mSuggestionAdapter);
        mSearchQuery.setFreezesText(true);
        mSearchQuery.setTextSize(18);
        mSearchQuery.setTextColor(Color.WHITE);
        mSearchQuery.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH){
                    String query = mSearchQuery.getText().toString().trim();

                    if(!mSearchQueriesCache.contains(query)) {
                        mSearchQueriesCache.edit()
                                .putString(query, query)
                                .apply();
                        mSuggestionAdapter.add(query);
                    }

                    if(mCallback != null){
                        mCallback.onSearchQuery(mSearchQuery.getText());
                    }

                    ViewUtils.hideKeyboard(mSearchQuery);

                    return true;
                }
                return false;
            }
        });

        ViewUtils.setBackground(mSearchQuery, null);
        ViewUtils.setVisibilityWithGoneFlag(mSearchQuery, false);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);
        params.gravity = Gravity.RIGHT | Gravity.TOP;
        addView(mSearchButton, params);

        params = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        params.rightMargin = size;
        params.leftMargin = ResourceUtils.getPixelSize(R.dimen.toolbar_title_layout_left);
        params.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;

        addView(mSearchQuery, params);
    }

    public void setSearchButtonBackground(Drawable drawable){
        if(drawable == null) {
            return;
        }

        ViewUtils.setBackground(mSearchButton, drawable);
    }

    public void setOnSearchClickListener(OnClickListener listener){
        mSearchButton.setOnClickListener(listener);
    }

    public void setCallback(QueryCallback callback) {
        mCallback = callback;
    }

    public void setHintTextColor(int color){
        mSearchQuery.setHintTextColor(color);
    }

    public void setQueryHintText(CharSequence text){
        mSearchQuery.setHint(text);
    }

    public void setIconsTint(int color){
        mSearchButton.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

    public boolean isSearchExpanded(){
        return isExpanded;
    }

    public void setQuery(String query){
        mSearchQuery.setText(query);
    }

    public CharSequence getQuery(){
        return mSearchQuery.getText();
    }

    public void expand(){
        isExpanded = true;
        ViewUtils.setVisibility(mSearchQuery, true);
        ViewUtils.showKeyboard(mSearchQuery);

        mSearchButton.setImageResource(R.drawable.abc_ic_clear_mtrl_alpha);

        mSearchQuery.setText("");
        mSearchQuery.requestFocus();

    }

    public void collapse(){
        isExpanded = false;
        ViewUtils.setVisibilityWithGoneFlag(mSearchQuery, false);
        mSearchButton.setImageResource(R.drawable.abc_ic_search_api_mtrl_alpha);

        ViewUtils.hideKeyboard(mSearchQuery);
    }

    public static interface QueryCallback{

        public void onSearchQuery(CharSequence query);

    }
}
