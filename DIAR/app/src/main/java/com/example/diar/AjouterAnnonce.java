package com.example.diar;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class AjouterAnnonce extends AppCompatActivity {

    private static final int MAX_PHOTOS = 10;

    private EditText etTitre, etDescription, etPrix;
    private EditText etNombrePersonnes;
    private ImageButton btnUploadPhoto;
    private Button btnNext;

    private DatabaseHelper databaseHelper;

    private final ArrayList<Uri> photosUris = new ArrayList<>();

    private String emailProprietaire = "";
    private String nomProprietaire = "";

    private double latitude = 0;
    private double longitude = 0;

    // Informations optionnelles retournées par CarteProp
    private String ville = "";
    private String adresse = "";
    private String quartier = "";
    private String typeLogement = "";
    private int nbChambres = 0;
    private double surface = 0;

    private final ActivityResultLauncher<String> photosLauncher =
            //photosLauncher : تسمح للمستخدم باختيار عدة صور من الهاتف.
            //e composant permet d'ouvrir le sélecteur de fichiers du téléphone et de récupérer les photos sélectionnées par l'utilisateur.
            registerForActivityResult(
                    new ActivityResultContracts.GetMultipleContents(),
                    uris -> {
                        if (uris != null && !uris.isEmpty()) {
                            photosUris.clear();

                            int nombre = Math.min(uris.size(), MAX_PHOTOS);

                            for (int i = 0; i < nombre; i++) {
                                photosUris.add(uris.get(i));
                            }

                            btnUploadPhoto.setImageURI(photosUris.get(0));

                            Toast.makeText(
                                    this,
                                    photosUris.size() + " photo(s) sélectionnée(s)",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );

    private final ActivityResultLauncher<Intent> carteLauncher =
            //carteLauncher : تسمح باستقبال الموقع الجغرافي ومعلومات السكن من واجهة الخريطة بعد ما المستخدم يختار
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK
                                && result.getData() != null) {

                            Intent data = result.getData();

                            latitude = data.getDoubleExtra("latitude", 0);
                            longitude = data.getDoubleExtra("longitude", 0);

                            ville = data.getStringExtra("ville");
                            adresse = data.getStringExtra("adresse");
                            quartier = data.getStringExtra("quartier");
                            typeLogement = data.getStringExtra("type");
                            nbChambres = data.getIntExtra("nbChambres", 0);
                            surface = data.getDoubleExtra("surface", 0);

                            if (latitude == 0 && longitude == 0) {
                                Toast.makeText(
                                        this,
                                        "Position invalide",
                                        Toast.LENGTH_SHORT
                                ).show();
                                return;
                            }

                            enregistrerAnnonce();

                        } else {
                            Toast.makeText(
                                    this,
                                    "Sélection de position annulée",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface19);

        databaseHelper = new DatabaseHelper(this);

        emailProprietaire =
                getIntent().getStringExtra("emailProprietaire");

        if (emailProprietaire == null || emailProprietaire.trim().isEmpty()) {
            emailProprietaire = new SessionManager(this).getEmail();
        }

        if (emailProprietaire == null || emailProprietaire.trim().isEmpty()) {
            Toast.makeText(
                    this,
                    "Email manquant, reconnectez-vous",
                    Toast.LENGTH_LONG
            ).show();
            finish();
            return;
        }

        chargerNomProprietaire();
        initialiserVues();

        btnUploadPhoto.setOnClickListener(
                v -> photosLauncher.launch("image/*")
        );

        btnNext.setOnClickListener(v -> {
            if (!validerFormulaire()) {
                return;
            }

            Intent intent = new Intent(
                    AjouterAnnonce.this,
                    CarteProp.class
            );

            carteLauncher.launch(intent);
        });
    }

    private void initialiserVues() {
        etTitre = findViewById(R.id.etTitre);
        etDescription = findViewById(R.id.etDescription);
        etPrix = findViewById(R.id.etPrix);
        etNombrePersonnes = findViewById(R.id.etNombrePersonnes);
        btnUploadPhoto = findViewById(R.id.btnUploadPhoto);
        btnNext = findViewById(R.id.btnNext);
    }

    private void chargerNomProprietaire() {

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor cursor = db.query(
                "Proprietaire",
                new String[]{"nom"},
                "email = ?",
                new String[]{emailProprietaire},
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            nomProprietaire = cursor.getString(0);
        }

        cursor.close();
    }

    private boolean validerFormulaire() {

        String titre = etTitre.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String prixText = etPrix.getText().toString().trim();
        String personnesText = etNombrePersonnes.getText().toString().trim();

        if (photosUris.isEmpty()) {
            Toast.makeText(
                    this,
                    "Veuillez choisir au moins une photo",
                    Toast.LENGTH_SHORT
            ).show();
            return false;
        }

        if (titre.isEmpty()) {
            etTitre.setError("Entrez le titre du logement");
            etTitre.requestFocus();
            return false;
        }

        if (description.isEmpty()) {
            etDescription.setError("Entrez la description");
            etDescription.requestFocus();
            return false;
        }

        if (prixText.isEmpty()) {
            etPrix.setError("Entrez le prix");
            etPrix.requestFocus();
            return false;
        }

        if (personnesText.isEmpty()) {
            etNombrePersonnes.setError("Entrez le nombre de personnes");
            etNombrePersonnes.requestFocus();
            return false;
        }

        try {
            double prix = Double.parseDouble(prixText);
            int personnes = Integer.parseInt(personnesText);

            if (prix <= 0) {
                etPrix.setError("Le prix doit être supérieur à 0");
                etPrix.requestFocus();
                return false;
            }

            if (personnes < 1 || personnes > 100) {
                etNombrePersonnes.setError(
                        "Le nombre doit être entre 1 et 100"
                );
                etNombrePersonnes.requestFocus();
                return false;
            }

        } catch (NumberFormatException e) {
            Toast.makeText(
                    this,
                    "Vérifiez le prix et le nombre de personnes",
                    Toast.LENGTH_SHORT
            ).show();
            return false;
        }

        return true;
    }

    // Copier une photo depuis la galerie vers le stockage privé
    private String copierPhoto(Uri uri, int index) throws Exception {

        File dossier = new File(getFilesDir(), "annonces");

        if (!dossier.exists() && !dossier.mkdirs()) {
            throw new Exception("Impossible de créer le dossier des photos");
        }

        File fichier = new File(
                dossier,
                "annonce_" + System.currentTimeMillis()
                        + "_" + index + ".jpg"
        );

        InputStream input = getContentResolver().openInputStream(uri);

        if (input == null) {
            throw new Exception("Impossible de lire une photo");
        }

        FileOutputStream output = new FileOutputStream(fichier);

        try {
            byte[] buffer = new byte[8192];
            int longueur;

            while ((longueur = input.read(buffer)) != -1) {
                output.write(buffer, 0, longueur);
            }

            output.flush();

        } finally {
            input.close();
            output.close();
        }

        return fichier.getAbsolutePath();
    }

    private void enregistrerAnnonce() {

        btnNext.setEnabled(false);
        btnNext.setText("Enregistrement...");

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ArrayList<String> cheminsPhotos = new ArrayList<>();

        boolean transactionReussie = false;

        try {
            // Copier les photos avant de commencer la transaction
            for (int i = 0; i < photosUris.size(); i++) {
                cheminsPhotos.add(copierPhoto(photosUris.get(i), i));
            }

            ContentValues annonce = new ContentValues();

            annonce.put(
                    "titre",
                    etTitre.getText().toString().trim()
            );

            annonce.put(
                    "description",
                    etDescription.getText().toString().trim()
            );

            annonce.put(
                    "prix",
                    Double.parseDouble(
                            etPrix.getText().toString().trim()
                    )
            );

            annonce.put(
                    "nbPersonnes",
                    Integer.parseInt(
                            etNombrePersonnes.getText().toString().trim()
                    )
            );

            annonce.put("latitude", latitude);
            annonce.put("longitude", longitude);

            annonce.put("emailProprietaire", emailProprietaire);
            annonce.put("nomProprietaire", nomProprietaire);

            annonce.put("ville", ville == null ? "" : ville);
            annonce.put("adresse", adresse == null ? "" : adresse);
            annonce.put("quartier", quartier == null ? "" : quartier);
            annonce.put("type", typeLogement == null ? "" : typeLogement);

            annonce.put("nbChambres", nbChambres);
            annonce.put("surface", surface);
            annonce.put("disponible", 1);

            db.beginTransaction();

            long annonceId = db.insertOrThrow(
                    "Annonce",
                    null,
                    annonce
            );

            // Enregistrer les chemins des photos dans AnnoncePhoto
            for (String chemin : cheminsPhotos) {

                ContentValues photoValues = new ContentValues();

                photoValues.put("annonce_id", annonceId);
                photoValues.put("photoPath", chemin);

                db.insertOrThrow(
                        "AnnoncePhoto",
                        null,
                        photoValues
                );
            }

            db.setTransactionSuccessful();
            transactionReussie = true;

            Toast.makeText(
                    this,
                    "Annonce enregistrée avec succès !",
                    Toast.LENGTH_LONG
            ).show();

            Intent intent = new Intent(
                    AjouterAnnonce.this,
                    AccueilProprietaire.class
            );

            intent.putExtra("emailProprietaire", emailProprietaire);
            intent.putExtra("nomProprietaire", nomProprietaire);

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            startActivity(intent);
            finish();

        } catch (Exception e) {

            e.printStackTrace();

            Toast.makeText(
                    this,
                    "Erreur d'enregistrement : " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();

        } finally {

            if (db.inTransaction()) {
                db.endTransaction();
            }

            // Si la transaction a échoué, supprimer les photos copiées
            if (!transactionReussie) {
                for (String chemin : cheminsPhotos) {
                    File fichier = new File(chemin);
                    if (fichier.exists()) {
                        fichier.delete();
                    }
                }
            }

            btnNext.setEnabled(true);
            btnNext.setText("Continuer");
        }
    }
}