package com.newmedia.erxeslibrary.model;


//import com.newmedia.erxes.basic.GetSupporterQuery;
import com.newmedia.erxes.basic.ConversationDetailQuery;
import com.newmedia.erxes.basic.MessagesQuery;
import com.newmedia.erxes.basic.MessengerSupportersQuery;
import com.newmedia.erxes.subscription.ConversationAdminMessageInsertedSubscription;
import com.newmedia.erxes.subscription.fragment.ConversationMessageFragment;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class User extends RealmObject{
    @PrimaryKey
    public String _id;
    public String avatar;
    public String fullName;
    public void convert(MessagesQuery.User itemuser){
        this.avatar = itemuser.details().avatar();
        this.fullName = itemuser.details().fullName();
        this._id = itemuser._id();
    }
//    public void convert(ConversationMessageFragment.User itemuser){
//        this.avatar = itemuser.details().avatar();
//        this.fullName = itemuser.details().fullName();
//        this._id = itemuser._id();
//    }
    static public List<User> convert(List<MessengerSupportersQuery.MessengerSupporter> itemuser){
        User temp;
        List<User> users = new ArrayList<>();
        for(int  i = 0 ; i <  itemuser.size(); i++ ) {
            temp = new User();
            temp._id = itemuser.get(i)._id();
            temp.avatar = itemuser.get(i).details().avatar();
            temp.fullName = itemuser.get(i).details().fullName();
            users.add(temp);
        }
        return users;
    }
}
