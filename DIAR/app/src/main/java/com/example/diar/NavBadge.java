package com.example.diar;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * Badge rouge (nombre de messages non lus) sur l'icône Chat / Messages
 * de la barre de navigation.
 *
 * Il se met à jour tout seul à chaque retour sur la page (onResume).
 */
public class NavBadge {

    public static void lier(final Activity activity, final int badgeId) {

        if (activity == null) return;

        final Application application = activity.getApplication();

        actualiser(activity, badgeId);

        application.registerActivityLifecycleCallbacks(
                new Application.ActivityLifecycleCallbacks() {

                    @Override
                    public void onActivityResumed(Activity a) {
                        if (a == activity) {
                            actualiser(activity, badgeId);
                        }
                    }

                    @Override
                    public void onActivityDestroyed(Activity a) {
                        if (a == activity) {
                            application.unregisterActivityLifecycleCallbacks(this);
                        }
                    }

                    @Override
                    public void onActivityCreated(Activity a, Bundle b) {
                    }

                    @Override
                    public void onActivityStarted(Activity a) {
                    }

                    @Override
                    public void onActivityPaused(Activity a) {
                    }

                    @Override
                    public void onActivityStopped(Activity a) {
                    }

                    @Override
                    public void onActivitySaveInstanceState(Activity a, Bundle b) {
                    }
                }
        );
    }

    public static void actualiser(Activity activity, int badgeId) {

        TextView badge = activity.findViewById(badgeId);

        if (badge == null) return;

        String email = new SessionManager(activity).getEmail();

        int nombre = 0;

        if (email != null && !email.trim().isEmpty()) {

            DatabaseHelper db = new DatabaseHelper(activity);

            try {
                nombre = db.compterTotalMessagesNonLus(email);
            } catch (Exception e) {
                nombre = 0;
            } finally {
                db.close();
            }
        }

        if (nombre > 0) {
            badge.setText(nombre > 9 ? "9+" : String.valueOf(nombre));
            badge.setVisibility(View.VISIBLE);
        } else {
            badge.setVisibility(View.GONE);
        }
    }
}