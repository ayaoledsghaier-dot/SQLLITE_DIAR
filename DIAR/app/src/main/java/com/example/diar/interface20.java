
package com.example.diar;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class interface20 extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    private LinearLayout layoutReservations;
    private TextView tvEmpty;

    private String emailProprietaire;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface20);

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        emailProprietaire = sessionManager.getEmail();

        layoutReservations = findViewById(R.id.layoutReservations);
        tvEmpty = findViewById(R.id.tvEmpty);

        NavigationProprietaire.setup(
                this,
                R.id.nav_prop_annonces
        );

        afficherReservations();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (databaseHelper != null &&
                layoutReservations != null) {
            afficherReservations();
        }
    }

    private void afficherReservations() {

        layoutReservations.removeAllViews();

        if (emailProprietaire == null ||
                emailProprietaire.trim().isEmpty()) {

            tvEmpty.setText("Veuillez vous connecter.");
            tvEmpty.setVisibility(TextView.VISIBLE);
            return;
        }

        Cursor cursor =
                databaseHelper.getReservationsProprietaire(
                        emailProprietaire
                );

        if (cursor == null || cursor.getCount() == 0) {

            tvEmpty.setText("Aucune demande de visite pour le moment.");
            tvEmpty.setVisibility(TextView.VISIBLE);

            if (cursor != null) {
                cursor.close();
            }

            return;
        }

        tvEmpty.setVisibility(TextView.GONE);

        try {
            while (cursor.moveToNext()) {

                long reservationId =
                        cursor.getLong(
                                cursor.getColumnIndexOrThrow("id")
                        );

                long annonceId =
                        cursor.getLong(
                                cursor.getColumnIndexOrThrow("annonce_id")
                        );

                String emailEtudiant =
                        cursor.getString(
                                cursor.getColumnIndexOrThrow("emailEtudiant")
                        );

                String date =
                        cursor.getString(
                                cursor.getColumnIndexOrThrow("dateReservation")
                        );

                String statut =
                        cursor.getString(
                                cursor.getColumnIndexOrThrow("statut")
                        );

                ajouterCarteReservation(
                        reservationId,
                        annonceId,
                        emailEtudiant,
                        date,
                        statut
                );
            }

        } finally {
            cursor.close();
        }
    }

    private void ajouterCarteReservation(
            long reservationId,
            long annonceId,
            String emailEtudiant,
            String date,
            String statut) {

        LinearLayout card = new LinearLayout(this);

        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(35, 30, 35, 30);
        card.setBackgroundColor(Color.WHITE);
        card.setElevation(5);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(16, 12, 16, 12);
        card.setLayoutParams(cardParams);

        TextView tvTitre = creerTextView(
                "Demande de visite",
                19,
                Color.BLACK
        );

        tvTitre.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView tvAnnonce = creerTextView(
                "Annonce N° : " + annonceId,
                15,
                Color.DKGRAY
        );

        TextView tvEtudiant = creerTextView(
                "Email étudiant : " + emailEtudiant,
                15,
                Color.DKGRAY
        );

        TextView tvDate = creerTextView(
                "Date de visite : " + date,
                15,
                Color.DKGRAY
        );

        TextView tvStatut = creerTextView(
                "Statut : " + statut,
                16,
                Color.rgb(230, 140, 20)
        );

        tvStatut.setTypeface(null, android.graphics.Typeface.BOLD);

        card.addView(tvTitre);
        card.addView(tvAnnonce);
        card.addView(tvEtudiant);
        card.addView(tvDate);
        card.addView(tvStatut);

        if ("En attente".equalsIgnoreCase(statut)) {

            LinearLayout buttonsLayout = new LinearLayout(this);

            buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);
            buttonsLayout.setGravity(Gravity.CENTER);
            buttonsLayout.setPadding(0, 20, 0, 0);

            Button btnAccepter = new Button(this);
            btnAccepter.setText("Accepter");
            btnAccepter.setTextColor(Color.WHITE);
            btnAccepter.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            Color.rgb(46, 160, 90)
                    )
            );

            Button btnRefuser = new Button(this);
            btnRefuser.setText("Refuser");
            btnRefuser.setTextColor(Color.WHITE);
            btnRefuser.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            Color.rgb(210, 65, 65)
                    )
            );

            LinearLayout.LayoutParams buttonParams =
                    new LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1
                    );

            buttonParams.setMargins(5, 0, 5, 0);

            buttonsLayout.addView(btnAccepter, buttonParams);
            buttonsLayout.addView(btnRefuser, buttonParams);

            card.addView(buttonsLayout);

            btnAccepter.setOnClickListener(v ->
                    modifierStatut(
                            reservationId,
                            "Acceptée"
                    )
            );

            btnRefuser.setOnClickListener(v ->
                    modifierStatut(
                            reservationId,
                            "Refusée"
                    )
            );
        }

        layoutReservations.addView(card);
    }

    private TextView creerTextView(
            String texte,
            int taille,
            int couleur) {

        TextView textView = new TextView(this);

        textView.setText(texte);
        textView.setTextSize(taille);
        textView.setTextColor(couleur);
        textView.setPadding(0, 8, 0, 8);

        return textView;
    }

    private void modifierStatut(
            long reservationId,
            String nouveauStatut) {

        boolean resultat =
                databaseHelper.modifierStatutReservation(
                        reservationId,
                        nouveauStatut
                );

        if (resultat) {

            Toast.makeText(
                    this,
                    "Demande " + nouveauStatut.toLowerCase() +
                            " avec succès",
                    Toast.LENGTH_SHORT
            ).show();

            afficherReservations();

        } else {

            Toast.makeText(
                    this,
                    "Erreur lors de la modification",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}
