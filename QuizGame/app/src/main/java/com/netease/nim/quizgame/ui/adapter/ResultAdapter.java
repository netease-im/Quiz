package com.netease.nim.quizgame.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.netease.nim.quizgame.R;
import com.netease.nim.quizgame.ui.model.SuccessfulUserInfo;

import java.util.List;

/**
 * 冲关结果列表adapter
 * Created by winnie on 13/01/2018.
 */

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> {

    List<SuccessfulUserInfo> userInfoList;

    public ResultAdapter(List<SuccessfulUserInfo> userInfoList) {
        this.userInfoList = userInfoList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.result_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.accountText.setText(userInfoList.get(position).getAccount());
        float money = userInfoList.get(position).getMoney();
        holder.moneyText.setText(String.valueOf((float) (Math.round(money * 10)) / 10));
    }

    @Override
    public int getItemCount() {
        return userInfoList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView accountText;
        public TextView moneyText;

        public ViewHolder(View itemView) {
            super(itemView);
            accountText = itemView.findViewById(R.id.account_text);
            moneyText = itemView.findViewById(R.id.money_text);
        }
    }
}
