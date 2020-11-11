package com.codies.Tattle.Utils;

import android.content.Context;
import android.util.Log;

import com.codies.Tattle.UI.Activities.CallActivity;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacksImpl;


public class WebRtcSessionManager extends QBRTCClientSessionCallbacksImpl {
    private static final String TAG = com.codies.Tattle.Utils.WebRtcSessionManager.class.getSimpleName();

    private static com.codies.Tattle.Utils.WebRtcSessionManager instance;
    private Context context;

    private static QBRTCSession currentSession;

    private WebRtcSessionManager(Context context) {
        this.context = context;
    }

    public static com.codies.Tattle.Utils.WebRtcSessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new com.codies.Tattle.Utils.WebRtcSessionManager(context);
        }

        return instance;
    }

    public QBRTCSession getCurrentSession() {
        return currentSession;
    }

    public void setCurrentSession(QBRTCSession qbCurrentSession) {
        currentSession = qbCurrentSession;
    }

    @Override
    public void onReceiveNewSession(QBRTCSession session) {
        Log.d(TAG, "onReceiveNewSession to WebRtcSessionManager");

        if (currentSession == null && session != null) {
            setCurrentSession(session);
            CallActivity.start(context, true);
        }
    }

    @Override
    public void onSessionClosed(QBRTCSession session) {
        Log.d(TAG, "onSessionClosed WebRtcSessionManager");

        if (session.equals(getCurrentSession())) {
            setCurrentSession(null);
        }
    }
}