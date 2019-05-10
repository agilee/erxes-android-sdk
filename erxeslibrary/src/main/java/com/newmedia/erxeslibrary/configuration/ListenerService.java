package com.newmedia.erxeslibrary.configuration;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.Html;
import android.util.Log;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.ApolloSubscriptionCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport;
import com.newmedia.erxes.basic.type.CustomType;
import com.newmedia.erxes.subscription.ConversationAdminMessageInsertedSubscription;
import com.newmedia.erxeslibrary.DataManager;
import com.newmedia.erxeslibrary.ui.login.ErxesActivity;
import com.newmedia.erxeslibrary.model.Conversation;
import com.newmedia.erxeslibrary.model.ConversationMessage;
import com.newmedia.erxeslibrary.R;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;
import io.realm.Realm;
import io.realm.RealmResults;
import okhttp3.OkHttpClient;

public class ListenerService extends Service{

    private static final String TAG = ListenerService.class.getName();
    private OkHttpClient okHttpClient;
    private ApolloClient apolloClient;
    private String customerId;
    private CompositeDisposable disposables = new CompositeDisposable();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Realm.init(this);

//        startListen();
        DataManager dataManager;
        dataManager =  DataManager.getInstance(this);
        customerId  = dataManager.getDataS(DataManager.customerId);
        okHttpClient = new OkHttpClient.Builder().build();
        apolloClient = ApolloClient.builder()
                .serverUrl(dataManager.getDataS("HOST3100"))
                .okHttpClient(okHttpClient)
                .subscriptionTransportFactory(new WebSocketSubscriptionTransport.Factory(dataManager.getDataS("HOST3300"), okHttpClient))
                .addCustomTypeAdapter(CustomType.JSON,new JsonCustomTypeAdapter())
//                .addCustomTypeAdapter(com.newmedia.erxes.subscription.type.CustomType.JSON,new JsonCustomTypeAdapter())
                .build();


        Log.d(TAG,"oncreate");
    }




    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"destory");
    }


    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        conversation_listen();
        return super.onStartCommand(intent, flags, startId);
    }

    public void conversation_listen(){
        ApolloSubscriptionCall<ConversationAdminMessageInsertedSubscription.Data> subscriptionCall;
        if(apolloClient == null)
            return;
        subscriptionCall = apolloClient
                                .subscribe(ConversationAdminMessageInsertedSubscription.builder()
                                .customerId(customerId)
                                .build());
        DisposableSubscriber<Response<ConversationAdminMessageInsertedSubscription.Data>> a = Rx2Apollo.from(subscriptionCall)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(
                        new DisposableSubscriber<Response<ConversationAdminMessageInsertedSubscription.Data>>() {

                            @Override
                            protected void onStart() {
                                super.onStart();
                                Log.d(TAG,"onstarted "+customerId);
                            }

                            @Override public void onError(Throwable e) {
                                Log.d(TAG,"onerror "+customerId);
                                e.printStackTrace();
                            }

                            @Override public void onNext(Response<ConversationAdminMessageInsertedSubscription.Data> response) {
                                if(!response.hasErrors()){
//                                    if(ErxesRequest.erxesRequest != null) {
//                                        ErxesRequest.erxesRequest.ConversationMessageSubsribe_handmade(response.data().conversationMessageInserted());
//                                        Log.d("erxesservice","alive");
//                                    }
                                    DataManager dataManager =  DataManager.getInstance(ListenerService.this);
                                    if(dataManager.getDataB("chat_is_going")==false) {
                                        Log.d(TAG,"dead");
                                        String chat_message = response.data().conversationAdminMessageInserted().fragments().conversationMessageFragment().content();
                                        String name = "";
                                        try {
//                                            if (response.data().conversationAdminMessageInserted().fragments().conversationMessageFragment().user() != null)
//                                                name = response.data().conversationAdminMessageInserted().fragments().conversationMessageFragment().user().details().fullName();
                                        }catch (Exception e){}
                                        createNotificationChannel(chat_message,name,response.data().conversationAdminMessageInserted().fragments().conversationMessageFragment().conversationId());

                                    }

                                    Realm inner =  DB.getDB();
                                    inner.beginTransaction();
                                    inner.insertOrUpdate(ConversationMessage.convert(response.data().conversationAdminMessageInserted().fragments().conversationMessageFragment()));
                                    inner.commitTransaction();

                                    Conversation conversation = inner.where(Conversation.class).equalTo("_id",response.data().conversationAdminMessageInserted().fragments().conversationMessageFragment().conversationId()).findFirst();

                                    Log.d(TAG,"insert to database");

                                    if(conversation != null) {
                                        Log.d(TAG,"parent change");
                                        inner.beginTransaction();
                                        conversation.content = (response.data().conversationAdminMessageInserted().fragments().conversationMessageFragment().content());
                                        conversation.isread = false;
                                        inner.insertOrUpdate(conversation);
                                        inner.commitTransaction();
                                    }
                                    inner.close();
//
                                }
                                Log.d(TAG,"onnext "+customerId);


                            }

                            @Override public void onComplete() {
                                Log.d(TAG,"subsrioption ehsouced");
                            }
                        }
                );

    }
    private void createNotificationChannel(String chat_message,String name,String conversion_id) {


        Intent intent = new Intent(this, ErxesActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "erxeschannel")
//                .setBadgeIconType(R.drawable.icon)
//                .setContentTitle(name)
//                .setContentText(Html.fromHtml( chat_message))
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                // Set the intent that will fire when the user taps the notification
//                .setContentIntent(pendingIntent)
//                .setAutoCancel(true);
//        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
//        notificationManager.notify(0, mBuilder.build());
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "erxes";
            CharSequence name1 = "erxes_channel";
            String Description = "erxes notification";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name1, importance);
            mChannel.setDescription(Description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200});
            mChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(mChannel);

            Notification notification = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle("таньд мессеж ирлээ")
                    .setContentText(Html.fromHtml(chat_message))
                    .setSmallIcon(R.drawable.icon)
                    .setSound(alarmSound)
                    .setContentIntent(pendingIntent).getNotification();
            notificationManager.notify(123, notification);
        }
        else{
            Notification notification = new Notification.Builder(this)
                    .setContentTitle("таньд мессеж ирлээ")
                    .setContentText(Html.fromHtml(chat_message))
                    .setSmallIcon(R.drawable.icon)
                    .setSound(alarmSound)
                    .setContentIntent(pendingIntent).getNotification();
            notificationManager.notify(123, notification);
        }

        Log.d(TAG,"notification can");
    }



}
