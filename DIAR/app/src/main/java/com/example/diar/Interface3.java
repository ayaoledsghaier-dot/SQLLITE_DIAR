
package com.example.diar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.Locale;

public class Interface3 extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;

    private MaterialButton btnLogin;
    private MaterialButton btnRegister;

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface3);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(
                    Interface3.this,
                    Interface4.class
            );
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> connecter());
    }

    private void connecter() {

        String email = etEmail.getText()
                .toString()
                .trim()
                .toLowerCase(Locale.ROOT);

        String motDePasse = etPassword.getText()
                .toString();

        if (email.isEmpty() || motDePasse.isEmpty()) {
            Toast.makeText(
                    this,
                    "Veuillez remplir tous les champs.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        btnLogin.setEnabled(false);

        Cursor cursorEtudiant = null;
        Cursor cursorProprietaire = null;

        try {

            SQLiteDatabase db =
                    databaseHelper.getReadableDatabase();

            // Recherche étudiant
            cursorEtudiant = db.rawQuery(
                    "SELECT nomComplet, email FROM Etudiant " +
                            "WHERE email = ? COLLATE NOCASE AND motDePasse = ?",
                    new String[]{email, motDePasse}
            );

            // Recherche propriétaire
            cursorProprietaire = db.rawQuery(
                    "SELECT nom, email FROM Proprietaire " +
                            "WHERE email = ? COLLATE NOCASE AND motDePasse = ?",
                    new String[]{email, motDePasse}
            );

            boolean estEtudiant = cursorEtudiant.moveToFirst();
            boolean estProprietaire = cursorProprietaire.moveToFirst();

            if (estEtudiant && estProprietaire) {

                Toast.makeText(
                        this,
                        "Compte ambigu : contactez l'administrateur.",
                        Toast.LENGTH_LONG
                ).show();

                return;
            }

            // ==================================
            // CONNEXION ETUDIANT
            // ==================================

            if (estEtudiant) {

                String nom = cursorEtudiant.getString(0);

                String emailRetour = cursorEtudiant.getString(1)
                        .trim()
                        .toLowerCase(Locale.ROOT);

                // Sauvegarder session étudiant
                sessionManager.sauvegarderSession(
                        emailRetour,
                        nom,
                        "ETUDIANT"
                );

                // Compatibilité avec les anciennes interfaces
                SharedPreferences prefs =
                        getSharedPreferences(
                                "DIAR_PREFS",
                                MODE_PRIVATE
                        );

                prefs.edit()
                        .putString("email_etudiant", emailRetour)
                        .putString("nom_etudiant", nom)
                        .putString("role", "ETUDIANT")
                        .apply();

                Toast.makeText(
                        this,
                        "Bienvenue " + nom,
                        Toast.LENGTH_SHORT
                ).show();

                Intent intent = new Intent(
                        Interface3.this,
                        Interface7.class
                );

                intent.putExtra("typeCompte", "ETUDIANT");
                intent.putExtra("nomEtudiant", nom);
                intent.putExtra("emailEtudiant", emailRetour);

                startActivity(intent);
                finish();

            }

            // ==================================
            // CONNEXION PROPRIETAIRE
            // ==================================

            else if (estProprietaire) {

                String nom = cursorProprietaire.getString(0);

                String emailRetour = cursorProprietaire.getString(1)
                        .trim()
                        .toLowerCase(Locale.ROOT);

                // Sauvegarder session propriétaire
                sessionManager.sauvegarderSession(
                        emailRetour,
                        nom,
                        "PROPRIETAIRE"
                );

                SharedPreferences prefs =
                        getSharedPreferences(
                                "DIAR_PREFS",
                                MODE_PRIVATE
                        );

                prefs.edit()
                        .putString("email_proprietaire", emailRetour)
                        .putString("nom_proprietaire", nom)
                        .putString("role", "PROPRIETAIRE")
                        .apply();

                Toast.makeText(
                        this,
                        "Bienvenue " + nom,
                        Toast.LENGTH_SHORT
                ).show();

                Intent intent = new Intent(
                        Interface3.this,
                        AccueilProprietaire.class
                );

                intent.putExtra("typeCompte", "PROPRIETAIRE");
                intent.putExtra("nomProprietaire", nom);
                intent.putExtra("emailProprietaire", emailRetour);

                startActivity(intent);
                finish();

            }

            // ==================================
            // IDENTIFIANTS INCORRECTS
            // ==================================

            else {

                Toast.makeText(
                        this,
                        "Email ou mot de passe incorrect.",
                        Toast.LENGTH_LONG
                ).show();
            }

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Erreur SQLite : " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();

            android.util.Log.e(
                    "DIAR_LOGIN",
                    "Erreur connexion",
                    e
            );

        } finally {

            if (cursorEtudiant != null) {
                cursorEtudiant.close();
            }

            if (cursorProprietaire != null) {
                cursorProprietaire.close();
            }

            btnLogin.setEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {

        if (databaseHelper != null) {
            databaseHelper.close();
        }

        super.onDestroy();
    }
}
