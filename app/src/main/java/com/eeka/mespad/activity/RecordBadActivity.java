package com.eeka.mespad.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eeka.mespad.R;
import com.eeka.mespad.adapter.CommonRecyclerAdapter;
import com.eeka.mespad.adapter.RecyclerViewHolder;
import com.eeka.mespad.bo.RecordBadBo;
import com.eeka.mespad.bo.TailorInfoBo;
import com.eeka.mespad.http.HttpHelper;

import java.io.Serializable;
import java.util.List;

/**
 * 记录不良界面
 * Created by Lenovo on 2017/7/19.
 */

public class RecordBadActivity extends BaseActivity {

    private TailorInfoBo mTailorInfo;
    private List<RecordBadBo> mList_badRecord;
    private ItemAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aty_recordbad);

        mTailorInfo = (TailorInfoBo) getIntent().getSerializableExtra("data");
        if (mTailorInfo == null) {
            toast("数据错误");
            return;
        }
        mList_badRecord = (List<RecordBadBo>) getIntent().getSerializableExtra("badList");
        initView();
    }

    @Override
    protected void initView() {
        super.initView();
        TextView tv_orderNum = (TextView) findViewById(R.id.tv_recordBad_orderNum);
        tv_orderNum.setText(mTailorInfo.getSHOP_ORDER_INFOR().getSHOP_ORDER());

        GridLayoutManager layoutManager = new GridLayoutManager(mContext, 4);
        mAdapter = new ItemAdapter(mContext, mList_badRecord, R.layout.gv_item_recordbad, layoutManager);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.gv_recordBad);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mAdapter);

        findViewById(R.id.btn_recordBad_save).setOnClickListener(this);
        findViewById(R.id.btn_recordBad_cancel).setOnClickListener(this);

        if (mList_badRecord == null) {
            showLoading();
            HttpHelper.getBadList(this);
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v.getId() == R.id.btn_recordBad_save) {
            showLoading();
            JSONObject json = new JSONObject();
            json.put("RESOURCE_BO", mTailorInfo.getRESR_INFOR().getRESOURCE_BO());
            json.put("NC_CODES", mList_badRecord);
            json.put("OPERATION", mTailorInfo.getOPER_INFOR().get(0).getOPERATION());
            json.put("OPERATION_BO", mTailorInfo.getOPER_INFOR().get(0).getOPERATION_BO());
            HttpHelper.saveBadRecord(json, this);
        } else if (v.getId() == R.id.btn_recordBad_cancel) {
            finish();
        }
    }

    private class ItemAdapter extends CommonRecyclerAdapter<RecordBadBo> {

        ItemAdapter(Context context, List<RecordBadBo> list, int layoutId, RecyclerView.LayoutManager layoutManager) {
            super(context, list, layoutId, layoutManager);
        }

        @Override
        public void convert(RecyclerViewHolder holder, final RecordBadBo item, final int position) {
            TextView tv_count = holder.getView(R.id.tv_recordBad_count);
            final int[] badCount = {item.getQTY()};
            if (badCount[0] == 0) {
                tv_count.setVisibility(View.GONE);
            } else {
                tv_count.setVisibility(View.VISIBLE);
                tv_count.setText(String.valueOf(badCount[0]));
            }

            TextView tv_type = holder.getView(R.id.tv_recordBad_type);
            tv_type.setText(item.getDESCRIPTION());
            tv_type.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    badCount[0]++;
                    item.setQTY(badCount[0]);
                    notifyItemChanged(position);
                }
            });
            holder.getView(R.id.btn_recordBad_sub).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (badCount[0] > 0) {
                        badCount[0]--;
                        item.setQTY(badCount[0]);
                        notifyItemChanged(position);
                    }
                }
            });
        }
    }

    @Override
    public void onSuccess(String url, JSONObject resultJSON) {
        super.onSuccess(url, resultJSON);
        if (HttpHelper.isSuccess(resultJSON)) {
            if (HttpHelper.getBadList.equals(url)) {
                mList_badRecord = JSON.parseArray(resultJSON.getJSONArray("result").toString(), RecordBadBo.class);
                mAdapter.setData(mList_badRecord);
            } else if (HttpHelper.saveBadRecord.equals(url)) {
                toast("保存成功");
                Intent intent = new Intent();
                intent.putExtra("badList", (Serializable) mList_badRecord);
                setResult(RESULT_OK, intent);
                finish();
            }
        } else {
            toast(resultJSON.getString("message"));
        }
    }

    @Override
    public void onFailure(String url, int code, String message) {
        super.onFailure(url, code, message);
        toast(message);
    }

    public static Intent getIntent(Context context, TailorInfoBo data, List<RecordBadBo> badList) {
        Intent intent = new Intent(context, RecordBadActivity.class);
        intent.putExtra("data", data);
        intent.putExtra("badList", (Serializable) badList);
        return intent;
    }
}
