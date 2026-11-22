package com.example.diar;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

/**
 * Outils pour afficher les messages non lus dans la liste des conversations :
 * - un badge rouge rond avec le nombre de messages
 * - une carte légèrement colorée quand il y a des messages non lus
 */
public class UnreadBadge {

    private static int dp(Context context, float valeur) {
        return (int) (valeur * context.getResources()
                .getDisplayMetrics().density);
    }

    // Cercle rouge avec le nombre de messages non lus
    public static TextView creer(Context context, int nombre) {

        TextView badge = new TextView(context);

        badge.setText(nombre > 99 ? "99+" : String.valueOf(nombre));
        badge.setTextColor(Color.WHITE);
        badge.setTextSize(12);
        badge.setTypeface(null, Typeface.BOLD);
        badge.setGravity(Gravity.CENTER);
        badge.setMinWidth(dp(context, 24));
        badge.setMinHeight(dp(context, 24));
        badge.setPadding(dp(context, 6), 0, dp(context, 6), 0);

        GradientDrawable fond = new GradientDrawable();
        fond.setColor(Color.parseColor("#EF4444"));
        fond.setCornerRadius(dp(context, 12));

        badge.setBackground(fond);

        return badge;
    }

    // Carte colorée (rouge très clair + bordure rouge) si messages non lus,
    // sinon carte blanche normale
    public static void colorerCarte(
            View carte,
            int nonLus,
            int rayonDp) {

        Context context = carte.getContext();

        GradientDrawable fond = new GradientDrawable();
        fond.setCornerRadius(dp(context, rayonDp));

        if (nonLus > 0) {
            fond.setColor(Color.parseColor("#FEF2F2"));
            fond.setStroke(dp(context, 1), Color.parseColor("#FCA5A5"));
        } else {
            fond.setColor(Color.WHITE);
            fond.setStroke(dp(context, 1), Color.parseColor("#E8EDF3"));
        }

        carte.setBackground(fond);
    }
}