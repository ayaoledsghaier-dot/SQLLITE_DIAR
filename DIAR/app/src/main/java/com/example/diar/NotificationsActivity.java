package com.example.diar;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificationsActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private String userEmail;
    private LinearLayout layoutNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        databaseHelper = new DatabaseHelper(this);

        SessionManager sessionManager = new SessionManager(this);
        userEmail = sessionManager.getEmail();

        if (userEmail == null || userEmail.trim().isEmpty()) {
            userEmail = getIntent().getStringExtra("USER_EMAIL");
        }

        construireInterface();
        chargerNotifications();
    }
//********************************************************************************
    private int dp(int value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density);
    }

    private void construireInterface() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(247, 249, 252));
        root.setPadding(dp(18), dp(18), dp(18), dp(10));

        TextView retour = new TextView(this);
        retour.setText("‹  Retour");
        retour.setTextSize(18);
        retour.setTextColor(Color.rgb(30, 42, 90));
        retour.setPadding(0, dp(5), 0, dp(15));
        retour.setOnClickListener(v -> finish());

        root.addView(retour);

        TextView titre = new TextView(this);
        titre.setText("Mes notifications 🔔");
        titre.setTextSize(23);
        titre.setTypeface(null, Typeface.BOLD);
        titre.setTextColor(Color.rgb(30, 42, 90));
        titre.setPadding(0, 0, 0, dp(15));

        root.addView(titre);

        ScrollView scrollView = new ScrollView(this);

        layoutNotifications = new LinearLayout(this);
        layoutNotifications.setOrientation(LinearLayout.VERTICAL);

        scrollView.addView(layoutNotifications);

        root.addView(
                scrollView,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1
                )
        );

        setContentView(root);
    }

    private void chargerNotifications() {

        layoutNotifications.removeAllViews();

        if (userEmail == null || userEmail.trim().isEmpty()) {
            afficherMessage("Veuillez vous connecter pour voir vos notifications.");
            return;
        }

        Cursor cursor = databaseHelper.getNotificationsEtudiant(userEmail);

        if (cursor == null) {
            afficherMessage("Impossible de charger les notifications.");
            return;
        }

        int nombre = 0;

        try {
            while (cursor.moveToNext()) {

                long id = cursor.getLong(
                        cursor.getColumnIndexOrThrow("id")
                );

                String titre = cursor.getString(
                        cursor.getColumnIndexOrThrow("titre")
                );

                String message = cursor.getString(
                        cursor.getColumnIndexOrThrow("message")
                );

                int lu = cursor.getInt(
                        cursor.getColumnIndexOrThrow("lu")
                );

                long timestamp = cursor.getLong(
                        cursor.getColumnIndexOrThrow("dateCreation")
                );

                ajouterCarteNotification(
                        id,
                        titre,
                        message,
                        timestamp,
                        lu == 0
                );

                nombre++;
            }

        } finally {
            cursor.close();
        }

        if (nombre == 0) {
            afficherMessage("Tu n'as pas encore de notifications.");
        }

        // Les notifications affichées deviennent lues.
        databaseHelper.marquerToutesNotificationsLues(userEmail);
    }

    private void ajouterCarteNotification(
            long id,
            String titre,
            String message,
            long timestamp,
            boolean nonLue) {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(15), dp(16), dp(15));

        GradientDrawable fond = new GradientDrawable();
        fond.setColor(Color.WHITE);
        fond.setCornerRadius(dp(15));
        fond.setStroke(
                dp(1),
                nonLue
                        ? Color.rgb(27, 138, 90)
                        : Color.rgb(230, 235, 241)
        );

        card.setBackground(fond);
        card.setElevation(dp(2));

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(0, 0, 0, dp(12));
        card.setLayoutParams(params);

        TextView tvTitre = new TextView(this);
        tvTitre.setText((nonLue ? "●  " : "") + titre);
        tvTitre.setTextSize(16);
        tvTitre.setTextColor(Color.rgb(30, 42, 90));
        tvTitre.setTypeface(null, Typeface.BOLD);

        card.addView(tvTitre);

        TextView tvMessage = new TextView(this);
        tvMessage.setText(message);
        tvMessage.setTextSize(14);
        tvMessage.setTextColor(Color.rgb(75, 85, 99));
        tvMessage.setPadding(0, dp(8), 0, dp(8));

        card.addView(tvMessage);

        TextView tvDate = new TextView(this);
        tvDate.setText(formaterDate(timestamp));
        tvDate.setTextSize(11);
        tvDate.setTextColor(Color.GRAY);

        card.addView(tvDate);

        card.setOnClickListener(v -> {
            databaseHelper.marquerNotificationLue(id);
            chargerNotifications();
        });

        layoutNotifications.addView(card);
    }

    private String formaterDate(long timestamp) {

        SimpleDateFormat format = new SimpleDateFormat(
                "dd/MM/yyyy à HH:mm",
                Locale.getDefault()
        );

        return format.format(new Date(timestamp));
    }

    private void afficherMessage(String message) {

        TextView tv = new TextView(this);
        tv.setText(message);
        tv.setTextSize(15);
        tv.setTextColor(Color.GRAY);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(dp(15), dp(40), dp(15), dp(40));

        layoutNotifications.addView(tv);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}