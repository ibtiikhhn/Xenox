package com.codies.Tattle.Utils;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.Collection;


public class QBResRequestExecutor {
    private String TAG = com.codies.Tattle.Utils.QBResRequestExecutor.class.getSimpleName();

    public void signUpNewUser(final QBUser newQbUser, final QBEntityCallback<QBUser> callback) {
        QBUsers.signUp(newQbUser).performAsync(callback);
    }

    public void signInUser(final QBUser currentQbUser, final QBEntityCallback<QBUser> callback) {
        QBUsers.signIn(currentQbUser).performAsync(callback);
    }

    public void signOut (QBEntityCallback<Void> callback) {
        QBUsers.signOut().performAsync(callback);
    }

    public void deleteCurrentUser(int currentQbUserID, QBEntityCallback<Void> callback) {
        QBUsers.deleteUser(currentQbUserID).performAsync(callback);
    }

    public void loadLastUpdatedUsers(QBPagedRequestBuilder qbPagedRequestBuilder, final QBEntityCallback<ArrayList<QBUser>> callback) {
        QBUsers.getUsers(qbPagedRequestBuilder).performAsync(callback);
    }

    public void loadUsersByIds(final Collection<Integer> usersIDs, final QBEntityCallback<ArrayList<QBUser>> callback) {
        QBUsers.getUsersByIDs(usersIDs, null).performAsync(callback);
    }
}