
package com.example.diar;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Interface15 extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    private ImageButton btnBack;
    private MaterialButton btnSubmit;

    private MaterialCardView[] dateCards;
    private MaterialCardView[] heureCards;

    private String dateSelectionnee = "";
    private String heureSelectionnee = "14:00";

    private long annonceId = -1;
    private String emailProprietaire = "";

    private final int vert = Color.rgb(0, 168, 107);
    private final int gris = Color.rgb(226, 232, 240);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface15);

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        btnBack = findViewById(R.id.btnBack);
        btnSubmit = findViewById(R.id.btnSubmit);

        Intent intent = getIntent();

        annonceId = intent.getLongExtra("annonceId", -1);

        // Récupérer l'email du propriétaire depuis l'annonce SQLite
        emailProprietaire = recupererEmailProprietaire(annonceId);

        dateCards = new MaterialCardView[]{
                findViewById(R.id.cardDate1),
                findViewById(R.id.cardDate2),
                findViewById(R.id.cardDate3),
                findViewById(R.id.cardDate4)
        };

        heureCards = new MaterialCardView[]{
                findViewById(R.id.cardHeure1),
                findViewById(R.id.cardHeure2),
                findViewById(R.id.cardHeure3)
        };

        btnBack.setOnClickListener(v -> finish());

        for (int i = 0; i < dateCards.length; i++) {
            final int index = i;

            dateCards[i].setOnClickListener(v ->
                    ouvrirCalendrier(index)
            );
        }

        String[] heures = {"10:00", "14:00", "16:00"};

        for (int i = 0; i < heureCards.length; i++) {
            final int index = i;

            heureCards[i].setOnClickListener(v -> {
                heureSelectionnee = heures[index];
                selectionnerHeure(index);
            });
        }

        selectionnerHeure(1);

        btnSubmit.setOnClickListener(v -> envoyerDemande());

        NavigationEtudiant.setup(this, 2);
    }

    // =====================================
    // RECUPERER EMAIL PROPRIETAIRE
    // =====================================

    private String recupererEmailProprietaire(long id) {

        if (id <= 0) {
            return "";
        }

        Cursor cursor = databaseHelper.getAnnonce(id);

        if (cursor == null) {
            return "";
        }

        String email = "";

        try {
            if (cursor.moveToFirst()) {

                int index = cursor.getColumnIndex("emailProprietaire");

                if (index != -1) {
                    email = cursor.getString(index);
                }
            }
        } finally {
            cursor.close();
        }

        if (email == null) {
            return "";
        }

        return email.trim().toLowerCase();
    }

    // =====================================
    // CALENDRIER
    // =====================================

    private void ouvrirCalendrier(int index) {

        Calendar aujourdHui = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {

                    Calendar choix = Calendar.getInstance();
                    choix.set(year, month, day);

                    SimpleDateFormat format =
                            new SimpleDateFormat(
                                    "yyyy-MM-dd",
                                    Locale.getDefault()
                            );

                    dateSelectionnee = format.format(choix.getTime());

                    selectionnerDate(index, choix);

                },
                aujourdHui.get(Calendar.YEAR),
                aujourdHui.get(Calendar.MONTH),
                aujourdHui.get(Calendar.DAY_OF_MONTH)
        );

        dialog.getDatePicker().setMinDate(
                System.currentTimeMillis() - 1000
        );

        dialog.show();
    }

    // =====================================
    // SELECTION DATE
    // =====================================

    private void selectionnerDate(int index, Calendar choix) {

        for (MaterialCardView card : dateCards) {
            card.setStrokeColor(gris);
            card.setStrokeWidth(1);
            card.setCardBackgroundColor(Color.WHITE);
        }

        dateCards[index].setStrokeColor(vert);
        dateCards[index].setStrokeWidth(3);
        dateCards[index].setCardBackgroundColor(
                Color.rgb(240, 253, 244)
        );

        if (dateCards[index].getChildAt(0) instanceof LinearLayout) {

            LinearLayout contenu =
                    (LinearLayout) dateCards[index].getChildAt(0);

            if (contenu.getChildCount() >= 2 &&
                    contenu.getChildAt(0) instanceof TextView &&
                    contenu.getChildAt(1) instanceof TextView) {

                TextView jour =
                        (TextView) contenu.getChildAt(0);

                TextView numero =
                        (TextView) contenu.getChildAt(1);

                SimpleDateFormat jourFormat =
                        new SimpleDateFormat("EEE", Locale.FRENCH);

                SimpleDateFormat dateFormat =
                        new SimpleDateFormat("dd/MM", Locale.getDefault());

                jour.setText(jourFormat.format(choix.getTime()));
                numero.setText(dateFormat.format(choix.getTime()));
            }
        }
    }

    // =====================================
    // SELECTION HEURE
    // =====================================

    private void selectionnerHeure(int index) {

        for (MaterialCardView card : heureCards) {

            card.setStrokeColor(gris);
            card.setStrokeWidth(1);
            card.setCardBackgroundColor(Color.WHITE);

            if (card.getChildAt(0) instanceof TextView) {

                TextView tv = (TextView) card.getChildAt(0);

                tv.setTextColor(Color.rgb(30, 41, 59));
                tv.setTypeface(null, android.graphics.Typeface.NORMAL);
            }
        }

        heureCards[index].setStrokeColor(vert);
        heureCards[index].setStrokeWidth(3);
        heureCards[index].setCardBackgroundColor(
                Color.rgb(240, 253, 244)
        );

        if (heureCards[index].getChildAt(0) instanceof TextView) {

            TextView selected =
                    (TextView) heureCards[index].getChildAt(0);

            selected.setTextColor(vert);
            selected.setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
            );
        }
    }

    // =====================================
    // ENVOYER DEMANDE
    // =====================================

    private void envoyerDemande() {

        String emailEtudiant =
                sessionManager.getEmail().trim().toLowerCase();

        // Vérifier connexion
        if (!sessionManager.isLoggedIn() ||
                emailEtudiant.isEmpty()) {

            Toast.makeText(
                    this,
                    "Veuillez vous connecter avec votre compte étudiant.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // Vérifier type de compte
        String typeCompte = sessionManager.getTypeCompte();

        if (!typeCompte.isEmpty() &&
                !typeCompte.equalsIgnoreCase("ETUDIANT")) {

            Toast.makeText(
                    this,
                    "Connectez-vous avec un compte étudiant.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        Log.d("DIAR_ID", "annonceId = " + annonceId);

        if (annonceId <= 0) {
            Toast.makeText(
                    this,
                    "Annonce introuvable. ID = " + annonceId,
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        if (emailProprietaire == null ||
                emailProprietaire.isEmpty()) {

            Toast.makeText(
                    this,
                    "Email du propriétaire introuvable.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        if (emailEtudiant.equals(emailProprietaire)) {

            Toast.makeText(
                    this,
                    "Vous ne pouvez pas réserver votre propre annonce.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        if (dateSelectionnee.isEmpty()) {

            Toast.makeText(
                    this,
                    "Veuillez choisir une date.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (heureSelectionnee.isEmpty()) {

            Toast.makeText(
                    this,
                    "Veuillez choisir une heure.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String dateReservation =
                dateSelectionnee + " " + heureSelectionnee;

        // Enregistrer dans SQLite
        long resultat = databaseHelper.ajouterReservation(
                annonceId,
                emailEtudiant,
                emailProprietaire,
                dateReservation
        );

        Log.d(
                "DIAR_RESERVATION",
                "ID=" + resultat +
                        " | Etudiant=" + emailEtudiant +
                        " | Proprietaire=" + emailProprietaire +
                        " | Annonce=" + annonceId +
                        " | Date=" + dateReservation
        );

        if (resultat != -1) {

            Toast.makeText(
                    this,
                    "Votre demande a été envoyée avec succès !",
                    Toast.LENGTH_LONG
            ).show();

            finish();

        } else {

            Toast.makeText(
                    this,
                    "Erreur lors de l'envoi. Vérifiez l'annonce et son propriétaire.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // =====================================
    // FERMETURE
    // =====================================

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}
