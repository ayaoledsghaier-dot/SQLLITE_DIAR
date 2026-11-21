
package com.example.diar;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class interface_creer_compte_proprietaire extends AppCompatActivity {

    private ImageView imageView;

    private TextInputEditText etFullNameProp;
    private TextInputEditText etEmailProp;
    private TextInputEditText etPasswordProp;
    private TextInputEditText etPhoneProp;

    private Uri imageUri;

    private DatabaseHelper databaseHelper;

    private ActivityResultLauncher<String> galerieLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface_creer_compte_prop);

        databaseHelper = new DatabaseHelper(this);

        imageView = findViewById(R.id.imageView);
        etFullNameProp = findViewById(R.id.etFullNameProp);
        etEmailProp = findViewById(R.id.etEmailProp);
        etPasswordProp = findViewById(R.id.etPasswordProp);
        etPhoneProp = findViewById(R.id.etPhoneProp);

        galerieLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imageUri = uri;
                        imageView.setImageURI(uri);
                    }
                }
        );

        imageView.setOnClickListener(v ->
                galerieLauncher.launch("image/*")
        );

        findViewById(R.id.btnBack).setOnClickListener(v ->
                finish()
        );

        findViewById(R.id.btnSubmitProp).setOnClickListener(v ->
                inscrireProprietaire()
        );
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() == null
                ? ""
                : editText.getText().toString().trim();
    }

    private void inscrireProprietaire() {

        String nom = getText(etFullNameProp);
        String email = getText(etEmailProp).toLowerCase();
        String motDePasse = getText(etPasswordProp);
        String telephone = getText(etPhoneProp);

        if (nom.isEmpty() || email.isEmpty()
                || motDePasse.isEmpty() || telephone.isEmpty()) {
            afficherMessage("Veuillez remplir tous les champs");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            afficherMessage("Adresse email invalide");
            return;
        }

        if (motDePasse.length() < 8) {
            afficherMessage("Le mot de passe doit contenir au moins 8 caractères");
            return;
        }

        findViewById(R.id.btnSubmitProp).setEnabled(false);

        String cheminPhoto = "";

        try {
            if (imageUri != null) {
                cheminPhoto = copierPhoto(imageUri);
            }

            SQLiteDatabase db = databaseHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("nom", nom);
            values.put("email", email);
            values.put("motDePasse", motDePasse);
            values.put("photoPath", cheminPhoto);

            long resultat = db.insertOrThrow(
                    "Proprietaire",
                    null,
                    values
            );

            if (resultat != -1) {

                // Sauvegarder l'email dans la session
                SessionManager sessionManager = new SessionManager(this);
                sessionManager.saveEmail(email);

                afficherMessage("Compte propriétaire créé avec succès !");

                Intent intent = new Intent(
                        interface_creer_compte_proprietaire.this,
                        Interface3.class
                );

                startActivity(intent);
                finish();
            }

        } catch (SQLiteConstraintException e) {
            afficherMessage("Cet email est déjà utilisé");

        } catch (Exception e) {
            e.printStackTrace();
            afficherMessage("Erreur inscription : " + e.getMessage());

        } finally {
            findViewById(R.id.btnSubmitProp).setEnabled(true);
        }
    }

    // Copier la photo dans le stockage privé de l'application
    private String copierPhoto(Uri uri) throws Exception {

        File dossier = new File(getFilesDir(), "photos");

        if (!dossier.exists()) {
            dossier.mkdirs();
        }

        File fichier = new File(
                dossier,
                "proprietaire_" + System.currentTimeMillis() + ".jpg"
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

    private void afficherMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
