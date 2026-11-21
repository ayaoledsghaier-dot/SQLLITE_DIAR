
package com.example.diar;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class Interfacecreercompteetudiant extends AppCompatActivity {

    private EditText etNomComplet;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;

    private MaterialButton btnCreerCompte;
    private ImageView imageView;

    private Uri selectedImageUri;

    private DatabaseHelper databaseHelper;

    private ActivityResultLauncher<String> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface_creer_compte_etudiant);

        databaseHelper = new DatabaseHelper(this);

        etNomComplet = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnCreerCompte = findViewById(R.id.btnSubmit);
        imageView = findViewById(R.id.imageView);

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        imageView.setImageURI(uri);
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    }
                }
        );

        imageView.setOnClickListener(v ->
                galleryLauncher.launch("image/*")
        );

        ImageView btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        btnCreerCompte.setOnClickListener(v ->
                validerInscription()
        );
    }

    // =========================================
    // VALIDATION ET INSCRIPTION
    // =========================================

    private void validerInscription() {

        String nom = etNomComplet.getText().toString().trim();
        String email = etEmail.getText().toString().trim().toLowerCase();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        resetFieldColors();

        if (nom.isEmpty() || email.isEmpty()
                || password.isEmpty() || confirmPassword.isEmpty()) {

            afficherMessage("Veuillez remplir tous les champs");

            if (nom.isEmpty()) setFieldError(etNomComplet);
            if (email.isEmpty()) setFieldError(etEmail);
            if (password.isEmpty()) setFieldError(etPassword);
            if (confirmPassword.isEmpty()) setFieldError(etConfirmPassword);

            return;
        }

        // Vérifier le nom
        int espaceCount = 0;

        for (int i = 0; i < nom.length(); i++) {

            char c = nom.charAt(i);

            if (c == ' ') {
                espaceCount++;
            } else if (!Character.isLetter(c)) {
                afficherMessage("Le nom doit contenir uniquement des lettres");
                setFieldError(etNomComplet);
                etNomComplet.requestFocus();
                return;
            }
        }

        if (espaceCount < 1 || espaceCount > 2) {
            afficherMessage("Format du nom incorrect : 1 ou 2 espaces autorisés");
            setFieldError(etNomComplet);
            etNomComplet.requestFocus();
            return;
        }

        setFieldSuccess(etNomComplet);

        // Vérifier l'email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            afficherMessage("Format d'email invalide");
            setFieldError(etEmail);
            etEmail.requestFocus();
            return;
        }

        setFieldSuccess(etEmail);

        // Vérifier le mot de passe
        if (password.length() < 6) {
            afficherMessage("Minimum 6 caractères");
            setFieldError(etPassword);
            etPassword.requestFocus();
            return;
        }

        int chiffreCount = 0;

        for (int i = 0; i < password.length(); i++) {
            if (Character.isDigit(password.charAt(i))) {
                chiffreCount++;
            }
        }

        if (chiffreCount < 3) {
            afficherMessage("Le mot de passe doit contenir au moins 3 chiffres");
            setFieldError(etPassword);
            etPassword.requestFocus();
            return;
        }

        setFieldSuccess(etPassword);

        // Confirmer le mot de passe
        if (!password.equals(confirmPassword)) {
            afficherMessage("Les mots de passe ne correspondent pas");
            setFieldError(etConfirmPassword);
            etConfirmPassword.requestFocus();
            return;
        }

        setFieldSuccess(etConfirmPassword);

        // Vérifier la photo
        if (selectedImageUri == null) {
            afficherMessage("Veuillez choisir une photo de profil");
            return;
        }

        // Inscription SQLite
        btnCreerCompte.setEnabled(false);

        inscrireEtudiantSQLite(nom, email, password);
    }

    // =========================================
    // INSERTION DANS SQLITE
    // =========================================

    private void inscrireEtudiantSQLite(
            String nom,
            String email,
            String password) {

        try {

            String cheminPhoto = copierPhoto(selectedImageUri);

            SQLiteDatabase db = databaseHelper.getWritableDatabase();

            ContentValues values = new ContentValues();

            values.put("nomComplet", nom);
            values.put("email", email);
            values.put("motDePasse", password);
            values.put("photoUrl", cheminPhoto);

            long resultat = db.insertOrThrow(
                    "Etudiant",
                    null,
                    values
            );

            if (resultat != -1) {

                SessionManager sessionManager = new SessionManager(this);

                sessionManager.sauvegarderSession(email, nom);

                afficherMessage("Compte étudiant créé avec succès !");

                Intent intent = new Intent(
                        Interfacecreercompteetudiant.this,
                        Interface5.class
                );

                intent.putExtra("USER_EMAIL", email);
                intent.putExtra("USER_NAME", nom);

                startActivity(intent);
                finish();
            }

        } catch (SQLiteConstraintException e) {

            afficherMessage("Cet email est déjà utilisé");

        } catch (Exception e) {

            e.printStackTrace();

            afficherMessage("Erreur inscription : " + e.getMessage());

        } finally {
            btnCreerCompte.setEnabled(true);
        }
    }

    // =========================================
    // COPIER PHOTO DANS LE TELEPHONE
    // =========================================

    private String copierPhoto(Uri uri) throws Exception {

        File dossier = new File(getFilesDir(), "photos");

        if (!dossier.exists()) {
            dossier.mkdirs();
        }

        File fichier = new File(
                dossier,
                "etudiant_" + System.currentTimeMillis() + ".jpg"
        );

        InputStream input = getContentResolver().openInputStream(uri);

        if (input == null) {
            throw new Exception("Impossible de lire la photo");
        }

        FileOutputStream output = new FileOutputStream(fichier);

        byte[] buffer = new byte[4096];
        int longueur;

        while ((longueur = input.read(buffer)) != -1) {
            output.write(buffer, 0, longueur);
        }

        input.close();
        output.close();

        return fichier.getAbsolutePath();
    }

    // =========================================
    // COULEURS DES CHAMPS
    // =========================================

    private void setFieldError(EditText editText) {
        editText.setBackgroundColor(Color.parseColor("#FFCDD2"));
    }

    private void setFieldSuccess(EditText editText) {
        editText.setBackgroundColor(Color.parseColor("#C8E6C9"));
    }

    private void resetFieldColors() {
        setNeutralField(etNomComplet);
        setNeutralField(etEmail);
        setNeutralField(etPassword);
        setNeutralField(etConfirmPassword);
    }

    private void setNeutralField(EditText editText) {
        editText.setBackgroundColor(Color.TRANSPARENT);
    }

    private void afficherMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
