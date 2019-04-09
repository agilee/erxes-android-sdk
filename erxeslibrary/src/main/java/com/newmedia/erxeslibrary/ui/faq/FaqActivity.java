package com.newmedia.erxeslibrary.ui.faq;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.newmedia.erxeslibrary.R;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.DB;
import com.newmedia.erxeslibrary.configuration.Helper;
import com.newmedia.erxeslibrary.configuration.SoftKeyboard;
import com.newmedia.erxeslibrary.model.ConversationMessage;
import com.newmedia.erxeslibrary.model.KnowledgeBaseCategory;
import com.newmedia.erxeslibrary.model.KnowledgeBaseTopic;
import com.newmedia.erxeslibrary.model.User;
import com.newmedia.erxeslibrary.ui.conversations.ConversationListActivity;
import com.newmedia.erxeslibrary.ui.conversations.adapter.ArticleAdapter;
import com.newmedia.erxeslibrary.ui.conversations.adapter.FaqAdapter;
import com.newmedia.erxeslibrary.ui.login.ErxesActivity;
import com.newmedia.erxeslibrary.ui.message.MessageActivity;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;

public class FaqActivity extends AppCompatActivity {
    private ViewGroup container;
    private Point size;
    private Config config;
    private TextView general,general_number,general_description;
    private Realm realm;
    private RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_faq);
        realm = DB.getDB();
        config = Config.getInstance(this);
        load_findViewByid();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(config.customerId == null) {
            this.finish();
        }
    }

    public void Click_back(View v){
        finish();
    }
    private void load_findViewByid(){
        container = this.findViewById(R.id.container);

        size = Helper.display_configure(this,container,"#00000000");
        InputMethodManager im = (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE);

        SoftKeyboard softKeyboard;
        softKeyboard = new SoftKeyboard((ViewGroup)this.findViewById(R.id.linearlayout), im);
        softKeyboard.setSoftKeyboardCallback(new SoftKeyboard.SoftKeyboardChanged() {
            @Override
            public void onSoftKeyboardHide() {
                FaqActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        container.getLayoutParams().height =size.y*8/10;
                        container.requestLayout();
                    }
                });
            }
            @Override
            public void onSoftKeyboardShow() {
                FaqActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        container.getLayoutParams().height = WindowManager.LayoutParams.MATCH_PARENT;
                        container.requestLayout();
                    }
                });
            }
        });
        this.findViewById(R.id.info_header).setBackgroundColor(config.colorCode);
        this.findViewById(R.id.close).setOnTouchListener(touchListener);
        this.findViewById(R.id.back).setOnTouchListener(touchListener);
        recyclerView = this.findViewById(R.id.recycler_view);
        general = this.findViewById(R.id.general);
        general_number = this.findViewById(R.id.general_number);
        general_description = this.findViewById(R.id.general_description);
        String id = getIntent().getStringExtra("id");
        if( id != null) {
            KnowledgeBaseCategory knowledgeBaseCategory = realm.where(KnowledgeBaseCategory.class).equalTo("_id",id).findFirst();
            general.setText(knowledgeBaseCategory.title);
            general_number.setText("("+knowledgeBaseCategory.numOfArticles+")");
            general_description.setText(knowledgeBaseCategory.description);
            recyclerView.setAdapter(new ArticleAdapter(this, knowledgeBaseCategory.articles));
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
    }
    private View.OnTouchListener touchListener =  new View.OnTouchListener() {
        @Override
        public boolean onTouch(final View v, MotionEvent event) {

            if(event.getAction() == MotionEvent.ACTION_DOWN){
                FaqActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        v.setBackgroundResource(R.drawable.action_background);
                    }
                });
            }
            else if(event.getAction() == MotionEvent.ACTION_UP){
                FaqActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        v.setBackgroundResource(0);
                        if(v.getId() == R.id.close)
                            logout(null);
                        else if(v.getId() == R.id.back)
                            Click_back(null);
                    }
                });
            }
            return true;
        }
    };
    public void logout(View v){
        realm.beginTransaction();
        realm.delete(ConversationMessage.class);
        realm.delete(User.class);
        realm.commitTransaction();
        config.Logout();
        finish();
    }
}
