package com.newmedia.erxeslibrary.ui.conversations.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.DB;
import com.newmedia.erxeslibrary.R;
import com.newmedia.erxeslibrary.model.User;

import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class SupportAdapter extends RecyclerView.Adapter<SupportAdapter.Holder> {

    private RealmResults<User> list;
    private Context context;
    public SupportAdapter(Context context) {
        this.context = context;
        Realm.init(context);
        Realm realm = DB.getDB();
        Config config = Config.getInstance(context);
        if(config.messengerdataInteg!=null&&config.messengerdataInteg.supporterIds!=null){
            list = realm.where(User.class).in("_id",config.messengerdataInteg.supporterIds.toArray(new String[0])).findAll();
        }
        else if(config.messengerdata!=null && config.messengerdata.supporterIds!=null){
            list = realm.where(User.class).in("_id",config.messengerdata.supporterIds.toArray(new String[0])).findAll();
        }
//        list.addChangeListener(new RealmChangeListener<RealmResults<User>>() {
//            @Override
//            public void onChange(RealmResults<User> users) {
//                   Log.d("GETSUP","changed all");
//            }
//        });
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.profile_image_online, parent, false);
//        view.setOnClickListener(onClickListener);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        if(list.get(position).avatar!=null)

            Glide.with(context).load(list.get(position).avatar)
                    .placeholder(R.drawable.avatar)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.circleImageView);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        CircleImageView circleImageView;
        TextView date,content,name;
        View parent ;//,isonline;
        public Holder(View itemView) {
            super(itemView);
            parent=itemView;
            circleImageView =  itemView.findViewById(R.id.profile_image);

        }
    }

}
