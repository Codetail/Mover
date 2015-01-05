package io.codetail.fragments.mover;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ShareCompat;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.List;

import io.codetail.adapters.WatchMeAdapterNew;
import io.codetail.client.models.Video;
import io.codetail.client.mover.FetchAvailableVideoQualities;
import io.codetail.client.mover.Mover;
import io.codetail.fragments.WatchMeRecycleFragment;
import io.codetail.recycle.ItemClickSupport;
import io.codetail.watchme.R;
import io.codetail.work.JobManager;

public abstract class MoverRecycleFragment extends WatchMeRecycleFragment
        implements ItemClickSupport.OnItemClickListener, ItemClickSupport.OnItemLongClickListener{

    WatchMeAdapterNew mWatchMeAdapter;
    int mSelectedVideoPosition;

    AlertDialog mDialog;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mWatchMeAdapter = getWatchMeAdapter();
    }

    public void onSuggestionAvailable(final Mover.Suggestion suggestion){
        List<String> qualities = suggestion.getAvailableQuality();
        CharSequence[] items = new CharSequence[qualities.size()];

        Resources resources = getResources();

        for (int i = qualities.size() - 1; i >= 0; i--) {
            int resId = resources.getIdentifier(
                    "mover_video_quality_"+qualities.get(i), "string",
                    getActivity().getPackageName());

            items[i] = getText(resId);
        }

        mDialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.choose_video_quality)
                .setCancelable(true)
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CharSequence text = (CharSequence)
                                mDialog.getListView().getAdapter().getItem(which);

                        if (text.equals(getString(R.string.mover_video_quality_b))) {
                            openVideo("b", suggestion.getPosition());
                        } else if (text.equals(getString(R.string.mover_video_quality_m))) {
                            openVideo("m", suggestion.getPosition());
                        } else if (text.equals(getString(R.string.mover_video_quality_s))) {
                            openVideo("s", suggestion.getPosition());
                        }
                    }
                })
                .show();
    }

    public void openVideo(String quality, String id){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(Mover.MoverVideo.createVideoLink(id, quality)), "video/mp4");
        startActivity(Intent.createChooser(intent, "Watch"));
    }

    public abstract JobManager getJobManager();

    @Override
    public void onItemClick(RecyclerView parent, View view, int position, long id) {
        int viewType = mWatchMeAdapter.getItemViewType(position);

        switch (viewType){

            case WatchMeAdapterNew.TYPE_VIDEO_LAST:
            case WatchMeAdapterNew.TYPE_VIDEO:
                Video video = (Video) mWatchMeAdapter.getItem(position);
                Toast.makeText(getActivity(), R.string.please_wait_until_qualities_not_fetched, Toast.LENGTH_SHORT).show();
                getJobManager().addJob(new FetchAvailableVideoQualities(video.getId()));
                break;

            case WatchMeAdapterNew.TYPE_MORE:
                mWatchMeAdapter.displayAllSectionItems(position);
                break;
        }

    }

    private final PopupMenu.OnMenuItemClickListener mOnMenuItemClickListener = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {

            switch (menuItem.getItemId()){
                case R.id.share:
                    Video video = ((WatchMeAdapterNew.WatchMeHolder) getRecycleView()
                            .findViewHolderForPosition(mSelectedVideoPosition)).getObject();

                    ShareCompat.IntentBuilder.from(getActivity())
                            .setChooserTitle(R.string.abc_shareactionprovider_share_with)
                            .setType("text/plain")
                            .setText(getString(R.string.sharing_video_template, video.getTitle(), video.getLinkForShare()))
                            .startChooser();

                    return true;

                default:
                    return false;
            }
        }
    };

    @Override
    public boolean onItemLongClick(RecyclerView parent, View view, int position, long id) {

        int viewType = mWatchMeAdapter.getItemViewType(position);

        switch (viewType){
            case WatchMeAdapterNew.TYPE_HEADER_FIRST:
                getJobManager().addJob(new FetchAvailableVideoQualities("p3xpwfHm"));
                return true;

            case WatchMeAdapterNew.TYPE_VIDEO:
            case WatchMeAdapterNew.TYPE_VIDEO_LAST:

                mSelectedVideoPosition = position;

                PopupMenu popupMenu = new PopupMenu(view.getContext(), view, Gravity.TOP);
                popupMenu.setOnMenuItemClickListener(mOnMenuItemClickListener);
                popupMenu.inflate(R.menu.video_item_menu);
                popupMenu.show();
                return true;
        }

        return false;
    }
}
