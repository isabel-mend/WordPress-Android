package org.wordpress.android.ui.posts.adapters;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.wordpress.android.R;
import org.wordpress.android.WordPress;
import org.wordpress.android.models.PostsListPost;
import org.wordpress.android.ui.posts.PostsListFragment;
import org.wordpress.android.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for Posts/Pages list
 * Created by Dan Roundhill on 11/5/13.
 */
public class PostListAdapter extends BaseAdapter {
    public static interface OnLoadMoreListener {
        public void onLoadMore(boolean loadMore);
    }

    private final OnLoadMoreListener mOnLoadMoreListener;
    private Context mContext;
    private boolean mIsPage;
    private LayoutInflater mLayoutInflater;

    private List<PostsListPost> mPosts = new ArrayList<PostsListPost>();


    public PostListAdapter(Context context, boolean isPage, OnLoadMoreListener onLoadMoreListener) {
        mContext = context;
        mIsPage = isPage;
        mOnLoadMoreListener = onLoadMoreListener;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    public List<PostsListPost> getPosts() {
        return mPosts;
    }

    public void setPosts(List<PostsListPost> postsList) {
        if (postsList != null)
            this.mPosts = postsList;
    }

    @Override
    public int getCount() {
        return mPosts.size();
    }

    @Override
    public Object getItem(int position) {
        return mPosts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mPosts.get(position).getPostId();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        PostsListPost post = mPosts.get(position);
        PostViewWrapper wrapper;
        if (view == null) {
            view = mLayoutInflater.inflate(R.layout.row_post_page, parent, false);
            wrapper = new PostViewWrapper(view);
            view.setTag(wrapper);
        } else {
            wrapper = (PostViewWrapper) view.getTag();
        }

        String date = post.getFormattedDate();
        String status = post.getStatus();

        String titleText = post.getTitle();
        if (titleText.equals(""))
            titleText = "(" + mContext.getResources().getText(R.string.untitled) + ")";
        wrapper.getTitle().setText(titleText);
        if (post.isLocalDraft()) {
            wrapper.getDate().setVisibility(View.GONE);
        } else {
            wrapper.getDate().setText(date);
            wrapper.getDate().setVisibility(View.VISIBLE);
        }

        String formattedStatus = "";
        if (post.isLocalDraft()) {
            formattedStatus = mContext.getResources().getText(R.string.local_draft).toString();
        } else if (status.equals("publish")) {
            formattedStatus = mContext.getResources().getText(R.string.published).toString();
        } else if (status.equals("draft")) {
            formattedStatus = mContext.getResources().getText(R.string.draft).toString();
        } else if (status.equals("pending")) {
            formattedStatus = mContext.getResources().getText(R.string.pending_review).toString();
        } else if (status.equals("private")) {
            formattedStatus = mContext.getResources().getText(R.string.post_private).toString();
        }
        formattedStatus = formattedStatus.toUpperCase(Locale.getDefault());
        wrapper.getStatus().setText(formattedStatus);

        // load more posts when we near the end
        if (mOnLoadMoreListener != null && position >= getCount() - 1
                && position >= PostsListFragment.POSTS_REQUEST_COUNT - 1)
            mOnLoadMoreListener.onLoadMore(true);

        return view;
    }

    public void loadPosts() {
        // load posts from db
        new LoadPostsTask().execute();
    }

    public void clear() {
        if (mPosts.size() > 0) {
            mPosts.clear();
            notifyDataSetChanged();
        }
    }

    class PostViewWrapper {
        View base;
        TextView title = null;
        TextView date = null;
        TextView status = null;

        PostViewWrapper(View base) {
            this.base = base;
        }

        TextView getTitle() {
            if (title == null) {
                title = (TextView) base.findViewById(R.id.title);
            }
            return (title);
        }

        TextView getDate() {
            if (date == null) {
                date = (TextView) base.findViewById(R.id.date);
            }
            return (date);
        }

        TextView getStatus() {
            if (status == null) {
                status = (TextView) base.findViewById(R.id.status);
            }
            return (status);
        }
    }

    private class LoadPostsTask extends AsyncTask <Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... nada) {
            if (WordPress.getCurrentBlog() != null) {
                List<PostsListPost> postsList = WordPress.wpDB.getPostsListPosts(WordPress.getCurrentBlog().getLocalTableBlogId(), mIsPage);
                if (postsList.size() == 0) {
                    mOnLoadMoreListener.onLoadMore(false);
                } else {
                    setPosts(postsList);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void nada) {
            notifyDataSetChanged();
        }
    }
}