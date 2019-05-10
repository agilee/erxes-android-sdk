package com.newmedia.erxeslibrary.ui.conversations.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.DB;
import com.newmedia.erxeslibrary.configuration.Helper;
import com.newmedia.erxeslibrary.R;
import com.newmedia.erxeslibrary.model.KnowledgeBaseCategory;
import com.newmedia.erxeslibrary.model.KnowledgeBaseTopic;
import com.newmedia.erxeslibrary.ui.faq.FaqActivity;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmResults;

public class FaqAdapter extends RecyclerView.Adapter<FaqAdapter.Holder> {

    private KnowledgeBaseTopic knowledgeBaseTopic;
    private RealmList<KnowledgeBaseCategory> categories;
    private Context context;
    private Config config;
    private int selected_position = -1;
    private Realm realm;
    public FaqAdapter(Context context) {
        this.context = context;
        this.config = Config.getInstance(context);
        Realm.init(context);
        realm = DB.getDB();
        init();
        realm.where(KnowledgeBaseTopic.class).findAll().addChangeListener(new RealmChangeListener<RealmResults<KnowledgeBaseTopic>>() {
            @Override
            public void onChange(RealmResults<KnowledgeBaseTopic> knowledgeBaseTopics) {
                init();
                FaqAdapter.this.notifyDataSetChanged();
            }
        });
    }
    private void init(){
        try {
            if (config.messengerdata.knowledgeBaseTopicId != null) {
                knowledgeBaseTopic = realm.where(KnowledgeBaseTopic.class).equalTo("_id", config.messengerdata.knowledgeBaseTopicId).findFirst();
            } else {
                knowledgeBaseTopic = realm.where(KnowledgeBaseTopic.class).findFirst();
            }
        }catch (Exception e1){
        }
        if(knowledgeBaseTopic!=null) {
            this.categories = knowledgeBaseTopic.categories;
        }else{
            //run app without error
            this.categories = new RealmList<>();

        }
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.faq_item, parent, false);
//        view.setOnClickListener(onClickListener);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        if(Helper.ICON_MAP.get(categories.get(position).icon) != null)
        holder.icon.setImageResource(Helper.ICON_MAP.get(categories.get(position).icon).intValue());
        holder.title.setText(categories.get(position).title+"("+categories.get(position).numOfArticles+") ");
        holder.description.setText(Html.fromHtml(categories.get(position).description));
        holder.parent.setTag(position);
        holder.parent.setTag(categories.get(position)._id);
        holder.parent.setOnClickListener(onClickListener);
        if(selected_position==position) {
            holder.recyclerView.setAdapter(new ArticleAdapter(context, categories.get(position).articles));
            holder.recyclerView.setLayoutManager(new LinearLayoutManager(context));
            holder.recyclerView.setVisibility(View.VISIBLE);
        }
        else
            holder.recyclerView.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }
    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int)v.getTag();
            if( selected_position == position){
                int temp = selected_position;
                selected_position = -1;
                FaqAdapter.this.notifyItemChanged(temp);
            }
            else{
                int temp = selected_position;
                selected_position = -1;
                if(temp > -1) {
                    FaqAdapter.this.notifyItemChanged(temp);
                }
                selected_position = position;
                FaqAdapter.this.notifyItemChanged(selected_position);
            }
        }
    };
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent a = new Intent(context, FaqActivity.class);
            a.putExtra("id",(String)v.getTag());
            context.startActivity(a);
        }
    };
    public class Holder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title,description;
        View parent ;
        RecyclerView recyclerView;
        public Holder(View itemView) {
            super(itemView);
            parent = itemView;
            icon = itemView.findViewById(R.id.icon);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);
            recyclerView = itemView.findViewById(R.id.recycler_view);
        }
    }

}
