
package com.example.diar;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
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
    private String heureSelectionnee = "";

    private long annonceId;
    private String emailProprietaire;

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

        emailProprietaire =
                intent.getStringExtra("emailProprietaire");

        if (emailProprietaire == null) {
            emailProprietaire = intent.getStringExtra("email");
        }

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

        // Heure sélectionnée par défaut
        heureSelectionnee = "14:00";
        selectionnerHeure(1);

        btnSubmit.setOnClickListener(v -> envoyerDemande());

        NavigationEtudiant.setup(this, 2);
    }

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

                    dateSelectionnee = format.format(
                            choix.getTime()
                    );

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

    private void selectionnerDate(int index, Calendar choix) {

        for (int i = 0; i < dateCards.length; i++) {
            dateCards[i].setStrokeColor(gris);
            dateCards[i].setStrokeWidth(1);
            dateCards[i].setCardBackgroundColor(Color.WHITE);
        }

        dateCards[index].setStrokeColor(vert);
        dateCards[index].setStrokeWidth(3);
        dateCards[index].setCardBackgroundColor(
                Color.rgb(240, 253, 244)
        );

        TextView jour = (TextView)
                ((android.widget.LinearLayout)
                        dateCards[index].getChildAt(0))
                        .getChildAt(0);

        TextView numero = (TextView)
                ((android.widget.LinearLayout)
                        dateCards[index].getChildAt(0))
                        .getChildAt(1);

        SimpleDateFormat jourFormat =
                new SimpleDateFormat("EEE", Locale.FRENCH);

        SimpleDateFormat dateFormat =
                new SimpleDateFormat("dd/MM", Locale.getDefault());

        jour.setText(jourFormat.format(choix.getTime()));
        numero.setText(dateFormat.format(choix.getTime()));
    }

    private void selectionnerHeure(int index) {

        for (int i = 0; i < heureCards.length; i++) {
            heureCards[i].setStrokeColor(gris);
            heureCards[i].setStrokeWidth(1);
            heureCards[i].setCardBackgroundColor(Color.WHITE);

            TextView tv = (TextView)
                    heureCards[i].getChildAt(0);

            tv.setTextColor(Color.rgb(30, 41, 59));
            tv.setTypeface(null, android.graphics.Typeface.NORMAL);
        }

        heureCards[index].setStrokeColor(vert);
        heureCards[index].setStrokeWidth(3);
        heureCards[index].setCardBackgroundColor(
                Color.rgb(240, 253, 244)
        );

        TextView selected = (TextView)
                heureCards[index].getChildAt(0);

        selected.setTextColor(vert);
        selected.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );
    }

    private void envoyerDemande() {

        String emailEtudiant = sessionManager.getEmail();

        if (emailEtudiant == null ||
                emailEtudiant.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Veuillez vous connecter",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        if (annonceId <= 0) {
            Toast.makeText(
                    this,
                    "Annonce introuvable",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        if (emailProprietaire == null ||
                emailProprietaire.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Email du propriétaire introuvable",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        if (dateSelectionnee.isEmpty()) {
            Toast.makeText(
                    this,
                    "Veuillez choisir une date",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (heureSelectionnee.isEmpty()) {
            Toast.makeText(
                    this,
                    "Veuillez choisir une heure",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String dateReservation =
                dateSelectionnee + " " + heureSelectionnee;

        long resultat = databaseHelper.ajouterReservation(
                annonceId,
                emailEtudiant,
                emailProprietaire,
                dateReservation
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
                    "Erreur lors de l'envoi de la demande",
                    Toast.LENGTH_LONG
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
