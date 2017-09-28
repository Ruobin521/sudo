package com.ruobin.sodu.View.Tab;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ruobin.sodu.Interface.IHtmlRequestResult;
import com.ruobin.sodu.Model.Book;
import com.ruobin.sodu.R;
import com.ruobin.sodu.Util.CustomRecyclerAdapter;
import com.ruobin.sodu.Util.DividerItemDecoration;
import com.ruobin.sodu.Util.HttpHelper;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadmoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by ruobin on 2017/9/26.
 */
public abstract class BaseTabFragment extends Fragment {

    private boolean isVisible;

    public boolean isLoading;

    public List<Book> books = new ArrayList<Book>();

    protected View currentView;


    private RecyclerView mRecyclerView;

    private RefreshLayout refreshLayout;

    private CustomRecyclerAdapter<Book> mAdapter;

    private int tabId;
    private int listItemId;

    public void setId(int tab, int listItem) {
        tabId = tab;
        listItemId = listItem;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (currentView == null) {

            currentView = inflater.inflate(tabId, container, false);
            //在这里做一些初始化处理
            initRefreshView();
            initRecylerView();

        } else {

            ViewGroup viewGroup = (ViewGroup) currentView.getParent();

            if (viewGroup != null)
                viewGroup.removeView(currentView);
        }

        if (isVisible && ifNeedLoadData() && !isLoading) {
            if (refreshLayout != null) {
                refreshLayout.autoRefresh();
            }
        }

        return currentView;
    }


    protected void initRefreshView() {

        refreshLayout = (RefreshLayout) currentView.findViewById(R.id.refreshLayout);
//        //设置 Header 为 Material风格
//        refreshLayout.setRefreshHeader(new ClassicsHeader(this.getContext()));
//        //设置 Footer 为 球脉冲
//        refreshLayout.setRefreshFooter(new ClassicsFooter(this.getContext()));

        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                loadData();
                //Toast.makeText(currentView.getContext(), "下拉刷新", Toast.LENGTH_SHORT).show();
              //  refreshlayout.finishRefresh(30);
            }
        });


        refreshLayout.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {

                loadData();
             //   Toast.makeText(currentView.getContext(), "上拉加载", Toast.LENGTH_SHORT).show();
            }
        });

    }

    protected void initRecylerView() {

        mRecyclerView = (RecyclerView) currentView.findViewById(R.id.list_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        mRecyclerView.setAdapter(mAdapter = new CustomRecyclerAdapter(books, listItemId));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this.getContext(),
                DividerItemDecoration.VERTICAL_LIST));

        mAdapter.setOnItemClickListener(new CustomRecyclerAdapter.ItemActionListener() {
            @Override
            public void onItemClick(View view, int position) {
                itemClick(view, position);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                itemLongClick(view, position);
            }

            @Override
            public void onItemInitData(View view, Object item) {
                itemInitData(view, item);
            }
        });
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        isVisible= isVisibleToUser;
        if (isVisibleToUser && ifNeedLoadData() && !isLoading) {
            if (refreshLayout != null) {
                refreshLayout.autoRefresh();
            }
        }
    }

    public void getHtmlByUrl(String url, final IHtmlRequestResult result) {

        HttpHelper.getMethod(url, new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {

                    String html = response.body().string();
                    result.success(html);

                } catch (Exception e) {
                    e.printStackTrace();
                    result.error();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                result.error();
            }
        });
    }


    public boolean ifNeedLoadData() {

        if (books == null || books.size() == 0) {
            return true;
        }
        return false;
    }

    //设置页面数据方法
    public void setData(String html) {

        mAdapter.updateData(books);
        refreshLayout.finishRefresh(30);
    }


    public final  void scrollToTop(){

        if(books!= null){
            mRecyclerView.smoothScrollToPosition(0);
        }
    }

    //获取数据失败时
    public void onRequestFailure() {
    }


    //当fragment隐藏时
    public void onFragmentUnVisible() {
    }


    //当fragment显示时
    public void onFragmentVisible() {


    }

    //获取数据
    public void loadData() {
        isLoading = true;
    }

    //获取数据
    public void loadMoreData() {
        isLoading = true;
        refreshLayout.finishLoadmore(30);
    }


    //点击
    public abstract void itemClick(View view, int position);

    //长按
    public abstract void itemLongClick(View view, int position);

    //初始化列表项数据
    public abstract void itemInitData(View view, Object item);
}