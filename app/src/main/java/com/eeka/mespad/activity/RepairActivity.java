package com.eeka.mespad.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eeka.mespad.R;
import com.eeka.mespad.adapter.CommonRecyclerAdapter;
import com.eeka.mespad.adapter.RecyclerViewHolder;
import com.eeka.mespad.http.HttpHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * 选择返修工序界面
 * Created by Lenovo on 2017/8/11.
 */

public class RepairActivity extends BaseActivity {

    private LinearLayout mLayout_component;
    private JSONArray mList_component;
    private RecyclerView mRecyclerView;
    private List<JSONObject> mList_type;
    private NcAdapter mAdapter;
    private int mComponentPosition;//选择的部件
    private int mTypePosition = -1;//选择的返工工序

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aty_repair);

        initView();
        initData();
    }

    @Override
    protected void initView() {
        super.initView();
        mLayout_component = (LinearLayout) findViewById(R.id.layout_repair_component);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView_repairType);
        GridLayoutManager layoutManager = new GridLayoutManager(mContext, 4);
        mRecyclerView.setLayoutManager(layoutManager);
        mList_type = new ArrayList<>();
        mAdapter = new NcAdapter(mContext, mList_type, R.layout.gv_item_recordnc, layoutManager);
        mRecyclerView.setAdapter(mAdapter);

        findViewById(R.id.btn_done).setOnClickListener(this);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
    }

    @Override
    protected void initData() {
        super.initData();
        mList_component = (JSONArray) getIntent().getSerializableExtra("data");
        if (mList_component != null && mList_component.size() != 0) {
            mLayout_component.removeAllViews();
            for (int i = 0; i < mList_component.size(); i++) {
                mLayout_component.addView(getTabView(mList_component.getJSONObject(i), i));
            }
            refreshTab(0);
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v.getId() == R.id.btn_done) {

        } else if (v.getId() == R.id.btn_cancel) {
            finish();
        }
    }

    /**
     * 获取标签布局
     */
    private View getTabView(final JSONObject item, final int position) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_tab, null);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        layoutParams.weight = 1;
        view.setLayoutParams(layoutParams);
        TextView tv_tabName = (TextView) view.findViewById(R.id.textView);
        tv_tabName.setText(item.getString("PROD_COMPONENT_DESC"));
        tv_tabName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mComponentPosition = position;
                mTypePosition = -1;
                showLoading();
                HttpHelper.getRepairProcess(item.getString("PROD_COMPONENT"), "", RepairActivity.this);
            }
        });
        return view;
    }

    private void refreshTab(int position) {
        int childCount = mLayout_component.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = mLayout_component.getChildAt(i);
            childAt.findViewById(R.id.textView).setEnabled(true);
        }
        mLayout_component.getChildAt(position).findViewById(R.id.textView).setEnabled(false);
    }


    private class NcAdapter extends CommonRecyclerAdapter<JSONObject> {

        NcAdapter(Context context, List<JSONObject> list, int layoutId, RecyclerView.LayoutManager layoutManager) {
            super(context, list, layoutId, layoutManager);
        }

        @Override
        public void convert(RecyclerViewHolder holder, final JSONObject item, final int position) {
            TextView tv_type = holder.getView(R.id.tv_recordNc_type);
            tv_type.setText(item.getString("DESCRIPTION"));
            if (position == mTypePosition) {
                tv_type.setBackgroundResource(R.color.text_gray_default);
            } else {
                tv_type.setBackgroundResource(R.color.white);
            }
            holder.getView(R.id.btn_recordNc_sub).setVisibility(View.GONE);

            tv_type.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mTypePosition = position;
                    mAdapter.notifyItemChanged(mTypePosition);
                }
            });
        }
    }

    @Override
    public void onSuccess(String url, JSONObject resultJSON) {
        super.onSuccess(url, resultJSON);
        if (HttpHelper.isSuccess(resultJSON)) {
            if (HttpHelper.getRepairProcess.equals(url)) {
                mList_type = JSON.parseArray(resultJSON.getJSONArray("result").toString(), JSONObject.class);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    public static Intent getIntent(Context context, JSONArray components) {
        Intent intent = new Intent(context, RepairActivity.class);
        intent.putExtra("data", components);
        return intent;
    }
}