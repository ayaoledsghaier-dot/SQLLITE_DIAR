
package com.example.diar;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class Interface12 extends AppCompatActivity {

    private RecyclerView recyclerViewProfils;
    private ProfilAdapter profilAdapter;
    private List<Etudiant> profilList;
    private ImageButton btnBack;
    private DatabaseHelper databaseHelper;

    private String emailConnecte = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface12);

        recyclerViewProfils = findViewById(R.id.recyclerViewProfils);
        btnBack = findViewById(R.id.btnBack);

        // Récupérer l'email de l'utilisateur connecté
        emailConnecte = getIntent().getStringExtra("USER_EMAIL");

        if (emailConnecte == null || emailConnecte.trim().isEmpty()) {
            SessionManager session = new SessionManager(this);
            emailConnecte = session.getEmail();
        }

        if (emailConnecte == null) {
            emailConnecte = "";
        }

        recyclerViewProfils.setLayoutManager(
                new LinearLayoutManager(this)
        );

        profilList = new ArrayList<>();

        profilAdapter = new ProfilAdapter(
                profilList,
                emailConnecte,
                etudiant -> {
                    // Action عند الضغط على profil
                    Toast.makeText(
                            Interface12.this,
                            "Profil sélectionné",
                            Toast.LENGTH_SHORT
                    ).show();
                }
        );

        recyclerViewProfils.setAdapter(profilAdapter);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        databaseHelper = new DatabaseHelper(this);

        chargerProfilsCompatibles();
    }

    private void chargerProfilsCompatibles() {

        profilList.clear();

        Cursor cursor = null;

        try {
            cursor = databaseHelper.getReadableDatabase().query(
                    "Etudiant",
                    null,
                    null,
                    null,
                    null,
                    null,
                    "id DESC"
            );

            while (cursor.moveToNext()) {

                String email = getString(cursor, "email");

                // Ne pas afficher le profil connecté
                if (email.equalsIgnoreCase(emailConnecte)) {
                    continue;
                }

                String nom = getString(cursor, "nomComplet");
                String universite = getString(cursor, "universite");
                String quartier = getString(cursor, "quartier");
                String modeDeVie = getString(cursor, "modeDeVie");
                String nbColocataires = getString(cursor, "nbColocataires");
                String proprete = getString(cursor, "proprete");
                String animaux = getString(cursor, "animauxAcceptes");
                String photoPath = getString(cursor, "photoUrl");

                int age = getInt(cursor, "age", 21);
                double budget = getDouble(cursor, "budgetMax", 0);

                int pourcentage = calculerCompatibilite(
                        quartier,
                        budget,
                        modeDeVie,
                        emailConnecte
                );

                Etudiant etudiant = new Etudiant(
                        nom,
                        email,
                        universite,
                        age,
                        budget,
                        modeDeVie,
                        nbColocataires,
                        proprete,
                        animaux
                );

                etudiant.setPourcentage(pourcentage);
                etudiant.setQuartier(quartier);
                etudiant.setPhotoUrl(photoPath);

                profilList.add(etudiant);
            }

            profilAdapter.notifyDataSetChanged();

            if (profilList.isEmpty()) {
                Toast.makeText(
                        this,
                        "Aucun profil étudiant disponible",
                        Toast.LENGTH_LONG
                ).show();
            }

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Erreur SQLite : " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private int calculerCompatibilite(
            String quartier,
            double budget,
            String modeDeVie,
            String emailConnecte) {

        int score = 50;
        Cursor cursor = null;

        try {
            cursor = databaseHelper.getReadableDatabase().query(
                    "Etudiant",
                    new String[]{"quartier", "budgetMax", "modeDeVie"},
                    "email = ?",
                    new String[]{emailConnecte},
                    null,
                    null,
                    null
            );

            if (cursor.moveToFirst()) {

                String monQuartier = getString(cursor, "quartier");
                double monBudget = getDouble(cursor, "budgetMax", 0);
                String monModeDeVie = getString(cursor, "modeDeVie");

                if (!quartier.isEmpty() &&
                        quartier.equalsIgnoreCase(monQuartier)) {
                    score += 20;
                }

                if (budget > 0 && monBudget > 0) {

                    double difference = Math.abs(budget - monBudget);
                    double maxBudget = Math.max(budget, monBudget);

                    if (difference <= maxBudget * 0.10) {
                        score += 20;
                    } else if (difference <= maxBudget * 0.25) {
                        score += 10;
                    }
                }

                if (!modeDeVie.isEmpty() &&
                        modeDeVie.equalsIgnoreCase(monModeDeVie)) {
                    score += 10;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return Math.min(score, 100);
    }

    private String getString(Cursor cursor, String column) {
        int index = cursor.getColumnIndex(column);

        if (index == -1 || cursor.isNull(index)) {
            return "";
        }

        return cursor.getString(index);
    }

    private int getInt(Cursor cursor, String column, int defaultValue) {
        int index = cursor.getColumnIndex(column);

        if (index == -1 || cursor.isNull(index)) {
            return defaultValue;
        }

        return cursor.getInt(index);
    }

    private double getDouble(
            Cursor cursor,
            String column,
            double defaultValue) {

        int index = cursor.getColumnIndex(column);

        if (index == -1 || cursor.isNull(index)) {
            return defaultValue;
        }

        return cursor.getDouble(index);
    }

    @Override
    protected void onDestroy() {
        if (databaseHelper != null) {
            databaseHelper.close();
        }

        super.onDestroy();
    }
}
