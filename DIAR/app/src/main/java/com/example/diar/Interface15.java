
package com.example.diar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Interface15 extends AppCompatActivity {

    private static final String TAG = "DIAR_RESERVATION";

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    private ImageButton btnBack;
    private MaterialButton btnSubmit;

    private TextView tvDateSelectionnee;
    private TextView tvHeureSelectionnee;

    private String dateSelectionnee = "";
    private String heureSelectionnee = "";

    private long annonceId = -1;
    private String emailProprietaire = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface15);

        // Initialisation
        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        // Récupération des vues
        btnBack = findViewById(R.id.btnBack);
        btnSubmit = findViewById(R.id.btnSubmit);

        tvDateSelectionnee =
                findViewById(R.id.tvDateSelectionnee);

        tvHeureSelectionnee =
                findViewById(R.id.tvHeureSelectionnee);

        // Récupération ID annonce
        annonceId = getIntent().getLongExtra("annonceId", -1);

        Log.d(TAG, "Annonce ID = " + annonceId);

        // Récupération email propriétaire
        emailProprietaire =
                recupererEmailProprietaire(annonceId);

        Log.d(TAG, "Email propriétaire = " + emailProprietaire);

        // Bouton retour
        btnBack.setOnClickListener(v -> finish());

        // Choisir date
        tvDateSelectionnee.setOnClickListener(v ->
                ouvrirCalendrier()
        );

        // Choisir heure
        tvHeureSelectionnee.setOnClickListener(v ->
                ouvrirHorloge()
        );

        // Envoyer demande
        btnSubmit.setOnClickListener(v ->
                envoyerDemande()
        );

        // Navigation
        NavigationEtudiant.setup(this, 2);
    }

    // =========================================
    // 1. RECUPERER EMAIL PROPRIETAIRE
    // =========================================

    private String recupererEmailProprietaire(long id) {

        if (id <= 0) {
            return "";
        }

        Cursor cursor = null;

        try {

            cursor = databaseHelper.getAnnonce(id);

            if (cursor != null && cursor.moveToFirst()) {

                int index = cursor.getColumnIndex(
                        "emailProprietaire"
                );

                // Compatibilité avec une ancienne colonne
                if (index == -1) {
                    index = cursor.getColumnIndex("email");
                }

                if (index != -1) {

                    String email = cursor.getString(index);

                    if (email != null) {
                        return email.trim().toLowerCase(Locale.ROOT);
                    }
                }
            }

        } catch (Exception e) {

            Log.e(TAG, "Erreur email propriétaire", e);

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }

        return "";
    }

    // =========================================
    // 2. OUVRIR CALENDRIER
    // =========================================

    private void ouvrirCalendrier() {

        Calendar aujourdHui = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                this,

                (view, year, month, day) -> {

                    Calendar choix = Calendar.getInstance();

                    choix.clear();

                    choix.set(
                            year,
                            month,
                            day,
                            0,
                            0,
                            0
                    );

                    SimpleDateFormat format =
                            new SimpleDateFormat(
                                    "yyyy-MM-dd",
                                    Locale.getDefault()
                            );

                    dateSelectionnee =
                            format.format(choix.getTime());

                    // Affichage date utilisateur
                    SimpleDateFormat affichage =
                            new SimpleDateFormat(
                                    "dd/MM/yyyy",
                                    Locale.getDefault()
                            );

                    tvDateSelectionnee.setText(
                            "📅 " + affichage.format(choix.getTime())
                    );

                    // Obliger à choisir une nouvelle heure
                    heureSelectionnee = "";

                    tvHeureSelectionnee.setText(
                            "🕒 Choisir une heure"
                    );

                    Log.d(TAG,
                            "Date choisie = " + dateSelectionnee
                    );
                },

                aujourdHui.get(Calendar.YEAR),
                aujourdHui.get(Calendar.MONTH),
                aujourdHui.get(Calendar.DAY_OF_MONTH)
        );

        // Interdire les dates passées
        dialog.getDatePicker().setMinDate(
                System.currentTimeMillis()
        );

        dialog.show();
    }

    // =========================================
    // 3. OUVRIR HORLOGE
    // =========================================

    private void ouvrirHorloge() {

        if (dateSelectionnee.isEmpty()) {

            Toast.makeText(
                    this,
                    "Veuillez choisir une date d'abord.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Calendar maintenant = Calendar.getInstance();

        Calendar dateChoisie = Calendar.getInstance();

        try {

            SimpleDateFormat format =
                    new SimpleDateFormat(
                            "yyyy-MM-dd",
                            Locale.getDefault()
                    );

            format.setLenient(false);

            dateChoisie.setTime(
                    format.parse(dateSelectionnee)
            );

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Date invalide.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Vérifier si la date choisie est aujourd'hui
        boolean memeJour =
                maintenant.get(Calendar.YEAR)
                        == dateChoisie.get(Calendar.YEAR)
                        &&
                        maintenant.get(Calendar.DAY_OF_YEAR)
                                == dateChoisie.get(Calendar.DAY_OF_YEAR);

        int heureInitiale;

        int minuteInitiale;

        if (memeJour) {

            heureInitiale =
                    maintenant.get(Calendar.HOUR_OF_DAY);

            minuteInitiale =
                    maintenant.get(Calendar.MINUTE);

        } else {

            heureInitiale = 10;
            minuteInitiale = 0;
        }

        TimePickerDialog dialog = new TimePickerDialog(
                this,

                (view, hourOfDay, minute) -> {

                    Calendar choix =
                            (Calendar) dateChoisie.clone();

                    choix.set(
                            Calendar.HOUR_OF_DAY,
                            hourOfDay
                    );

                    choix.set(
                            Calendar.MINUTE,
                            minute
                    );

                    choix.set(Calendar.SECOND, 0);

                    choix.set(Calendar.MILLISECOND, 0);

                    // Interdire les heures passées
                    if (choix.getTimeInMillis()
                            <= System.currentTimeMillis()) {

                        Toast.makeText(
                                this,
                                "Cette heure est déjà passée. Choisissez une autre heure.",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    heureSelectionnee = String.format(
                            Locale.getDefault(),
                            "%02d:%02d",
                            hourOfDay,
                            minute
                    );

                    tvHeureSelectionnee.setText(
                            "🕒 " + heureSelectionnee
                    );

                    Log.d(TAG,
                            "Heure choisie = " + heureSelectionnee
                    );
                },

                heureInitiale,
                minuteInitiale,
                true
        );

        dialog.show();
    }

    // =========================================
    // 4. ENVOYER DEMANDE
    // =========================================

    private void envoyerDemande() {

        // Vérifier connexion
        if (!sessionManager.isLoggedIn()) {

            Toast.makeText(
                    this,
                    "Veuillez vous connecter avec votre compte étudiant.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // Email étudiant
        String emailEtudiant = sessionManager.getEmail();

        if (emailEtudiant == null ||
                emailEtudiant.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Email étudiant introuvable.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        emailEtudiant =
                emailEtudiant.trim().toLowerCase(Locale.ROOT);

        // Vérifier type de compte
        String typeCompte = sessionManager.getTypeCompte();

        if (typeCompte != null &&
                !typeCompte.trim().isEmpty() &&
                !typeCompte.equalsIgnoreCase("ETUDIANT")) {

            Toast.makeText(
                    this,
                    "Connectez-vous avec un compte étudiant.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // Vérifier ID annonce
        if (annonceId <= 0) {

            Toast.makeText(
                    this,
                    "Annonce introuvable. ID invalide.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // Vérifier propriétaire
        if (emailProprietaire == null ||
                emailProprietaire.isEmpty()) {

            Toast.makeText(
                    this,
                    "Email du propriétaire introuvable.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // Empêcher réservation de sa propre annonce
        if (emailEtudiant.equalsIgnoreCase(emailProprietaire)) {

            Toast.makeText(
                    this,
                    "Vous ne pouvez pas réserver votre propre annonce.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // Vérifier date
        if (dateSelectionnee.isEmpty()) {

            Toast.makeText(
                    this,
                    "Veuillez choisir une date.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Vérifier heure
        if (heureSelectionnee.isEmpty()) {

            Toast.makeText(
                    this,
                    "Veuillez choisir une heure.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Vérification finale date + heure
        String dateReservation =
                dateSelectionnee + " " + heureSelectionnee;

        try {

            SimpleDateFormat format =
                    new SimpleDateFormat(
                            "yyyy-MM-dd HH:mm",
                            Locale.getDefault()
                    );

            format.setLenient(false);

            Calendar reservation = Calendar.getInstance();

            reservation.setTime(
                    format.parse(dateReservation)
            );

            if (reservation.getTimeInMillis()
                    <= System.currentTimeMillis()) {

                Toast.makeText(
                        this,
                        "La date et l'heure doivent être dans le futur.",
                        Toast.LENGTH_LONG
                ).show();

                return;
            }

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Date ou heure invalide.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Enregistrer dans SQLite
        try {

            long resultat = databaseHelper.ajouterReservation(
                    annonceId,
                    emailEtudiant,
                    emailProprietaire,
                    dateReservation
            );

            Log.d(
                    TAG,
                    "Résultat = " + resultat
                            + " | Annonce = " + annonceId
                            + " | Etudiant = " + emailEtudiant
                            + " | Proprietaire = " + emailProprietaire
                            + " | Date = " + dateReservation
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
                        "Erreur lors de l'enregistrement SQLite.",
                        Toast.LENGTH_LONG
                ).show();
            }

        } catch (Exception e) {

            Log.e(TAG, "Erreur réservation", e);

            Toast.makeText(
                    this,
                    "Erreur : " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // =========================================
    // 5. FERMETURE
    // =========================================

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}
