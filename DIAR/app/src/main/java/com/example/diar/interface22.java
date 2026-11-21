
package com.example.diar;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class interface22 extends AppCompatActivity {

    private EditText edtNom, edtEmail;
    private EditText edtAncien, edtNouveau, edtConfirmer;

    private TextView tvNom, tvRole;
    private ImageView imgProfil;

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    private boolean proprietaire = false;
    private String emailActuel;

    private String tableActuelle;
    private String colonneNom;
    private String colonnePhoto;

    private String cheminPhoto = null;

    private final ActivityResultLauncher<String> pickPhoto =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            sauvegarderPhotoSQLite(uri);
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface22);

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        // Déterminer le type de compte
        String type = getIntent().getStringExtra("typeCompte");

        if (type == null) {
            type = getIntent().getStringExtra("typeUtilisateur");
        }

        proprietaire =
                "PROPRIETAIRE".equalsIgnoreCase(type)
                        || getIntent().getStringExtra("emailProprietaire") != null;

        // Déterminer la table et les colonnes
        if (proprietaire) {

            NavigationProprietaire.setup(this, R.id.nav_prop_profil);

            tableActuelle = "Proprietaire";
            colonneNom = "nom";
            colonnePhoto = "photoPath";

        } else {

            NavigationEtudiant.setup(this, 4);

            tableActuelle = "Etudiant";
            colonneNom = "nomComplet";
            colonnePhoto = "photoUrl";
        }

        // Liaison des vues
        edtNom = findViewById(R.id.edtNomProfil);
        edtEmail = findViewById(R.id.edtEmailProfil);

        tvNom = findViewById(R.id.tvNomProfil);
        tvRole = findViewById(R.id.tvRoleProfil);

        imgProfil = findViewById(R.id.imgProfilProprietaire);

        edtAncien = findViewById(R.id.edtAncienMotDePasse);
        edtNouveau = findViewById(R.id.edtNouveauMotDePasse);
        edtConfirmer = findViewById(R.id.edtConfirmerMotDePasse);

        // Récupérer l'email du compte
        if (proprietaire) {

            tvRole.setText("Propriétaire DIAR");

            emailActuel = getIntent().getStringExtra("emailProprietaire");

            if (emailActuel == null || emailActuel.isEmpty()) {
                emailActuel = sessionManager.getEmail();
            }

        } else {

            tvRole.setText("Étudiant DIAR");
            emailActuel = sessionManager.getEmail();
        }

        // Charger le profil
        chargerProfilSQLite();

        // Changer photo
        findViewById(R.id.btnChangerPhoto).setOnClickListener(v ->
                pickPhoto.launch("image/*")
        );

        imgProfil.setOnClickListener(v ->
                pickPhoto.launch("image/*")
        );

        // Modifier profil
        findViewById(R.id.btnModifierProfil).setOnClickListener(v -> {

            String nouveauNom = edtNom.getText().toString().trim();
            String nouvelEmail = edtEmail.getText().toString().trim();

            if (nouveauNom.isEmpty() || nouvelEmail.isEmpty()) {
                afficherMessage("Veuillez remplir le nom et l'email");
                return;
            }

            if (!nouvelEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                afficherMessage("Adresse email invalide");
                return;
            }

            modifierProfilSQLite(nouveauNom, nouvelEmail);
        });

        // Changer mot de passe
        findViewById(R.id.btnChangerMotDePasse).setOnClickListener(v -> {

            String ancien = edtAncien.getText().toString();
            String nouveau = edtNouveau.getText().toString();
            String confirmer = edtConfirmer.getText().toString();

            if (ancien.isEmpty() || nouveau.isEmpty() || confirmer.isEmpty()) {
                afficherMessage("Remplis les 3 champs");
                return;
            }

            if (!nouveau.equals(confirmer)) {
                afficherMessage("Les mots de passe ne correspondent pas");
                return;
            }

            if (nouveau.length() < 8) {
                afficherMessage("Minimum 8 caractères");
                return;
            }

            changerMotDePasseSQLite(ancien, nouveau);
        });

        // Déconnexion
        findViewById(R.id.btnDeconnexionProfil).setOnClickListener(v -> {

            sessionManager.logout();

            Intent intent = new Intent(interface22.this, Interface3.class);

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
            finish();
        });
    }

    // =========================================
    // 1. CHARGER LE PROFIL SQLITE
    // =========================================

    private void chargerProfilSQLite() {

        if (emailActuel == null || emailActuel.isEmpty()) {
            afficherMessage("Email introuvable");
            return;
        }

        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = null;

        try {

            // Pour étudiant : nomComplet
            // Pour propriétaire : nom
            cursor = db.query(
                    tableActuelle,
                    new String[]{
                            colonneNom,
                            "email",
                            "motDePasse",
                            colonnePhoto
                    },
                    "email = ?",
                    new String[]{emailActuel},
                    null,
                    null,
                    null
            );

            if (cursor.moveToFirst()) {

                String nom = cursor.getString(0);
                String email = cursor.getString(1);
                String photo = cursor.getString(3);

                edtNom.setText(nom);
                edtEmail.setText(email);
                tvNom.setText(nom);

                emailActuel = email;

                if (photo != null && !photo.isEmpty()) {

                    cheminPhoto = photo;

                    File fichier = new File(photo);

                    if (fichier.exists()) {
                        imgProfil.setImageURI(Uri.fromFile(fichier));
                    } else {
                        imgProfil.setImageResource(R.drawable.ic_person);
                    }

                } else {

                    imgProfil.setImageResource(R.drawable.ic_person);
                }

            } else {

                afficherMessage("Profil introuvable dans SQLite");
            }

        } catch (Exception e) {

            e.printStackTrace();
            afficherMessage("Erreur chargement profil : " + e.getMessage());

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // =========================================
    // 2. MODIFIER NOM ET EMAIL
    // =========================================

    private void modifierProfilSQLite(String nom, String nouvelEmail) {

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        try {

            ContentValues values = new ContentValues();

            values.put(colonneNom, nom);
            values.put("email", nouvelEmail);

            int resultat = db.update(
                    tableActuelle,
                    values,
                    "email = ?",
                    new String[]{emailActuel}
            );

            if (resultat > 0) {

                emailActuel = nouvelEmail;

                sessionManager.saveEmail(nouvelEmail);

                tvNom.setText(nom);
                edtNom.setText(nom);
                edtEmail.setText(nouvelEmail);

                afficherMessage("Profil modifié avec succès");

            } else {

                afficherMessage("Erreur : profil non modifié");
            }

        } catch (Exception e) {

            e.printStackTrace();
            afficherMessage("Erreur modification : " + e.getMessage());
        }
    }

    // =========================================
    // 3. CHANGER MOT DE PASSE
    // =========================================

    private void changerMotDePasseSQLite(String ancien, String nouveau) {

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        Cursor cursor = null;

        try {

            cursor = db.query(
                    tableActuelle,
                    new String[]{"motDePasse"},
                    "email = ?",
                    new String[]{emailActuel},
                    null,
                    null,
                    null
            );

            if (cursor.moveToFirst()) {

                String ancienEnregistre = cursor.getString(0);

                if (ancienEnregistre != null &&
                        ancienEnregistre.equals(ancien)) {

                    ContentValues values = new ContentValues();
                    values.put("motDePasse", nouveau);

                    int resultat = db.update(
                            tableActuelle,
                            values,
                            "email = ?",
                            new String[]{emailActuel}
                    );

                    if (resultat > 0) {

                        afficherMessage("Mot de passe changé avec succès");

                        edtAncien.setText("");
                        edtNouveau.setText("");
                        edtConfirmer.setText("");

                    } else {

                        afficherMessage("Erreur changement mot de passe");
                    }

                } else {

                    afficherMessage("Ancien mot de passe incorrect");
                }

            } else {

                afficherMessage("Compte introuvable");
            }

        } catch (Exception e) {

            e.printStackTrace();
            afficherMessage("Erreur : " + e.getMessage());

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // =========================================
    // 4. SAUVEGARDER PHOTO SQLITE
    // =========================================

    private void sauvegarderPhotoSQLite(Uri uri) {

        File fichier = null;

        try {

            File dossier = new File(getFilesDir(), "photos");

            if (!dossier.exists() && !dossier.mkdirs()) {
                afficherMessage("Impossible de créer le dossier photo");
                return;
            }

            String nomFichier = proprietaire
                    ? "profil_proprietaire_" + System.currentTimeMillis() + ".jpg"
                    : "profil_etudiant_" + System.currentTimeMillis() + ".jpg";

            fichier = new File(dossier, nomFichier);

            try (
                    InputStream input = getContentResolver().openInputStream(uri);
                    FileOutputStream output = new FileOutputStream(fichier)
            ) {

                if (input == null) {
                    afficherMessage("Impossible de lire la photo");
                    return;
                }

                byte[] buffer = new byte[4096];
                int longueur;

                while ((longueur = input.read(buffer)) != -1) {
                    output.write(buffer, 0, longueur);
                }
            }

            cheminPhoto = fichier.getAbsolutePath();

            SQLiteDatabase db = databaseHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(colonnePhoto, cheminPhoto);

            int resultat = db.update(
                    tableActuelle,
                    values,
                    "email = ?",
                    new String[]{emailActuel}
            );

            if (resultat > 0) {

                imgProfil.setImageURI(Uri.fromFile(fichier));

                afficherMessage("Photo enregistrée avec succès");

            } else {

                afficherMessage("Photo copiée mais profil introuvable");
            }

        } catch (Exception e) {

            e.printStackTrace();
            afficherMessage("Erreur lors de l'enregistrement de la photo");
        }
    }

    // =========================================
    // 5. MESSAGE
    // =========================================

    private void afficherMessage(String message) {

        Toast.makeText(
                interface22.this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }
}
