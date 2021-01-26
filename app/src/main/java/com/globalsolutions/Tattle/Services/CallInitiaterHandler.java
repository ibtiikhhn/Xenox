package com.globalsolutions.Tattle.Services;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import com.globalsolutions.Tattle.Adapters.UsersAdapter;
import com.globalsolutions.Tattle.DB.QbUsersDbManager;
import com.globalsolutions.Tattle.R;
import com.globalsolutions.Tattle.UI.Activities.CallActivity;
import com.globalsolutions.Tattle.UI.Activities.OpponentsActivity;
import com.globalsolutions.Tattle.UI.Activities.PermissionsActivity;
import com.globalsolutions.Tattle.Utils.CollectionsUtils;
import com.globalsolutions.Tattle.Utils.Consts;
import com.globalsolutions.Tattle.Utils.PermissionsChecker;
import com.globalsolutions.Tattle.Utils.PushNotificationSender;
import com.globalsolutions.Tattle.Utils.SharedPrefsHelper;
import com.globalsolutions.Tattle.Utils.ToastUtils;
import com.globalsolutions.Tattle.Utils.WebRtcSessionManager;
import com.quickblox.chat.QBChatService;
import com.quickblox.messages.services.QBPushManager;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;

import java.util.ArrayList;
import java.util.List;

public class CallInitiaterHandler {

    private static final String TAG = OpponentsActivity.class.getSimpleName();

    private static final int PER_PAGE_SIZE_100 = 100;
    private static final String ORDER_RULE = "order";
    private static final String ORDER_DESC_UPDATED = "desc date updated_at";
    public static final String TOTAL_PAGES_BUNDLE_PARAM = "total_pages";

    private RecyclerView usersRecyclerview;
    private QBUser currentUser;
    private UsersAdapter usersAdapter;
    private int currentPage = 0;
    private Boolean isLoading = false;
    private Boolean hasNextPage = true;

    private QbUsersDbManager dbManager;
    private PermissionsChecker checker;
    private QBUser opponentUser;

    protected SharedPrefsHelper sharedPrefsHelper;
    Intent intent;
    String receiverEmail;
    boolean isAudioCall;
    Context context;
    Activity activity;
    QBUser qbUser;

    public CallInitiaterHandler(Context context,Activity activity) {
        this.context = context;
        this.activity = activity;
        sharedPrefsHelper = SharedPrefsHelper.getInstance();
        this.receiverEmail = receiverEmail;
        currentUser = SharedPrefsHelper.getInstance().getQbUser();
        dbManager = QbUsersDbManager.getInstance(context.getApplicationContext());
        checker = new PermissionsChecker(context.getApplicationContext());
        Log.i(TAG, "CallInitiaterHandler: " + receiverEmail);
//        getUserByEmail(receiverEmail);
        startLoginService();
    }

    public void setCallType(boolean isAudioCall) {
        if (isAudioCall) {
            this.isAudioCall = true;
        } else {
            this.isAudioCall = false;
        }
    }

    public void startCall(QBUser qbUser) {
        if (isAudioCall) {
            if (checkIsLoggedInChat()) {
                Log.i(TAG, "onCreate: "+"logged in to chat : true");
                startCall(false,qbUser);
            }
            if (checker.lacksPermissions(Consts.PERMISSIONS[1])) {
                startPermissionsActivity(true);
            }
        } else {
            if (checkIsLoggedInChat()) {
                startCall(true,qbUser);
            }
            if (checker.lacksPermissions(Consts.PERMISSIONS)) {
                startPermissionsActivity(false);
            }
        }
    }

/*    @Override
    protected void onResume() {
        super.onResume();
        boolean isIncomingCall = SharedPrefsHelper.getInstance().get(Consts.EXTRA_IS_INCOMING_CALL, false);
        if (isCallServiceRunning(CallService.class)) {
            Log.d(TAG, "CallService is running now");
            CallActivity.start(context, isIncomingCall);
        }
        clearAppNotifications();
//        loadUsers();
    }*/

    public boolean isCallServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void clearAppNotifications() {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }
    }

    private void startPermissionsActivity(boolean checkOnlyAudio) {
        PermissionsActivity.startActivity(activity, checkOnlyAudio, Consts.PERMISSIONS);
    }





   /* public void getUserByEmail(String email) {
        Log.i(TAG, "getUserByEmail: " + email);
        QBUsers.getUserByEmail(email).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                if (qbUser != null) {
                    opponentUser = qbUser;
                    startCall();
                }
                Log.i(TAG, "onSuccess: "+"opponent user is null");
            }

            @Override
            public void onError(QBResponseException e) {
                Log.i(TAG, "onError: " + e.getMessage());
                Log.i(TAG, "onError: " + e.getErrors());
                Log.i(TAG, "onError: " + e.getHttpStatusCode());
                Log.i(TAG, "onError: " + e.getLocalizedMessage());
                Toast.makeText(context, "User not found!!", Toast.LENGTH_SHORT).show();
            }
        });
    }*/


    private boolean checkIsLoggedInChat() {
        Log.i(TAG, "checkIsLoggedInChat: "+ QBChatService.getInstance().isLoggedIn());
        if (!QBChatService.getInstance().isLoggedIn()) {
            Log.i(TAG, "checkIsLoggedInChat: chatservice ma login ni hai");
            startLoginService();
            ToastUtils.shortToast(R.string.dlg_relogin_wait);
            return false;
        }
        return true;
    }

    private void startLoginService() {
        if (sharedPrefsHelper.hasQbUser()) {
            Log.i(TAG, "startLoginService: chatservice start ho gye hai");
            QBUser qbUser = sharedPrefsHelper.getQbUser();
            LoginService.start(context, qbUser);
        } else {
            Log.i(TAG, "startLoginService: shared prefs py user he ni hai");
        }
    }

    private void startCall(boolean isVideoCall,QBUser qbUser) {
        Log.d(TAG, "Starting Call");
    /*    if (usersAdapter.getSelectedUsers().size() > Consts.MAX_OPPONENTS_COUNT) {
            ToastUtils.longToast(String.format(getString(R.string.error_max_opponents_count),
                    Consts.MAX_OPPONENTS_COUNT));
            return;
        }*/
        if (qbUser == null) {
            Log.i(TAG, "startCall: qbuser is null");
        } else {


            List<QBUser> opponent = new ArrayList<>();
            opponent.add(qbUser);
            Log.i(TAG, "startCall: " + qbUser.toString());
            Log.i(TAG, "startCall: " + opponent.size());
            Log.i(TAG, "startCall: " + opponent.get(0).toString());

            ArrayList<Integer> opponentsList = CollectionsUtils.getIdsSelectedOpponents(opponent);
            QBRTCTypes.QBConferenceType conferenceType = isVideoCall
                    ? QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO
                    : QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO;
            Log.d(TAG, "conferenceType = " + conferenceType);

            QBRTCClient qbrtcClient = QBRTCClient.getInstance(context.getApplicationContext());
            QBRTCSession newQbRtcSession = qbrtcClient.createNewSessionWithOpponents(opponentsList, conferenceType);
            WebRtcSessionManager.getInstance(context).setCurrentSession(newQbRtcSession);

            // Make Users FullName Strings and ID's list for iOS VOIP push
            String newSessionID = newQbRtcSession.getSessionID();
            ArrayList<String> opponentsIDsList = new ArrayList<>();
            ArrayList<String> opponentsNamesList = new ArrayList<>();
            List<QBUser> usersInCall = opponent;

            // the Caller in exactly first position is needed regarding to iOS 13 functionality
            usersInCall.add(0, currentUser);

            for (QBUser user : usersInCall) {
                String userId = user.getId().toString();
                String userName = "";
                if (TextUtils.isEmpty(user.getFullName())) {
                    userName = user.getEmail();
                } else {
                    userName = user.getFullName();
                }

                opponentsIDsList.add(userId);
                opponentsNamesList.add(userName);
            }

            String opponentsIDsString = TextUtils.join(",", opponentsIDsList);
            String opponentNamesString = TextUtils.join(",", opponentsNamesList);

            Log.d(TAG, "New Session with ID: " + newSessionID + "\n Users in Call: " + "\n" + opponentsIDsString + "\n" + opponentNamesString);
            PushNotificationSender.sendPushMessage(opponentsList, currentUser.getFullName(), newSessionID, opponentsIDsString, opponentNamesString, isVideoCall);
            CallActivity.start(context, false);
        }
    }

    private class QBPushSubscribeListenerImpl implements QBPushManager.QBSubscribeListener {
        @Override
        public void onSubscriptionCreated() {

        }

        @Override
        public void onSubscriptionError(Exception e, int i) {

        }

        @Override
        public void onSubscriptionDeleted(boolean b) {

        }
    }
}
