
package com.example.diar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

public class Interfaceverification extends AppCompatActivity {

    private MaterialCardView cardUploadBox;
    private ImageView ivPreview;
    private TextView btnBack;
    private MaterialButton btnVerify;

    private String userEmail;

    private Uri documentUri = null;

    private DatabaseHelper databaseHelper;

    // اختيار صورة attestation
    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {

                        if (uri == null) {
                            return;
                        }

                        try {

                            String mimeType =
                                    getContentResolver().getType(uri);

                            if (mimeType == null ||
                                    !mimeType.startsWith("image/")) {

                                Toast.makeText(
                                        this,
                                        "Choisissez une image seulement",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            InputStream input =
                                    getContentResolver()
                                            .openInputStream(uri);

                            Bitmap bitmap =
                                    BitmapFactory.decodeStream(input);

                            if (input != null) {
                                input.close();
                            }

                            if (bitmap == null) {
                                Toast.makeText(
                                        this,
                                        "Image invalide",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            documentUri = uri;

                            ivPreview.setImageBitmap(bitmap);
                            ivPreview.setVisibility(ImageView.VISIBLE);

                            cardUploadBox.setCardBackgroundColor(
                                    android.graphics.Color.parseColor(
                                            "#C8E6C9"
                                    )
                            );

                            Toast.makeText(
                                    this,
                                    "Attestation sélectionnée !",
                                    Toast.LENGTH_SHORT
                            ).show();

                        } catch (Exception e) {

                            e.printStackTrace();

                            Toast.makeText(
                                    this,
                                    "Erreur lors de la lecture de l'image",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.interface_verif_etudiant);

        // Récupérer email étudiant
        userEmail = getIntent().getStringExtra("USER_EMAIL");

        if (userEmail == null || userEmail.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Email introuvable !",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        // Initialiser SQLite
        databaseHelper = new DatabaseHelper(this);

        // Relier XML
        cardUploadBox = findViewById(R.id.cardUploadBox);
        ivPreview = findViewById(R.id.ivPreview);
        btnVerify = findViewById(R.id.btnVerify);
        btnBack = findViewById(R.id.btnBack);

        // Retour
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Choisir image
        cardUploadBox.setOnClickListener(v ->
                galleryLauncher.launch("image/*")
        );

        // Enregistrer attestation dans SQLite
        btnVerify.setOnClickListener(v -> {

            if (documentUri == null) {

                Toast.makeText(
                        this,
                        "Veuillez importer votre attestation",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            btnVerify.setEnabled(false);
            btnVerify.setText("Enregistrement...");

            enregistrerAttestationLocale();
        });
    }

    private void enregistrerAttestationLocale() {

        try {

            // Créer dossier privé pour les attestations
            File dossier = new File(
                    getFilesDir(),
                    "attestations"
            );

            if (!dossier.exists()) {
                dossier.mkdirs();
            }

            // Créer fichier permanent
            String nomFichier =
                    "attestation_" + System.currentTimeMillis() + ".img";

            File fichier = new File(dossier, nomFichier);

            // Copier image du téléphone vers stockage privé
            try (
                    InputStream input =
                            getContentResolver().openInputStream(documentUri);

                    FileOutputStream output =
                            new FileOutputStream(fichier)
            ) {

                if (input == null) {
                    throw new Exception("Impossible de lire l'image");
                }

                byte[] buffer = new byte[4096];

                int longueur;

                while ((longueur = input.read(buffer)) != -1) {
                    output.write(buffer, 0, longueur);
                }

                output.flush();
            }

            // Enregistrer chemin dans SQLite
            boolean success =
                    databaseHelper.enregistrerAttestation(
                            userEmail,
                            fichier.getAbsolutePath()
                    );

            if (success) {

                Toast.makeText(
                        this,
                        "Attestation enregistrée avec succès !",
                        Toast.LENGTH_LONG
                ).show();

                btnVerify.setText("Enregistrée");

                // Ouvrir profil étudiant
                Intent intent = new Intent(
                        Interfaceverification.this,
                        Interfaceprofilecoloc.class
                );

                intent.putExtra("USER_EMAIL", userEmail);

                intent.setFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                );

                startActivity(intent);

                finish();

            } else {

                // Supprimer fichier si enregistrement SQLite échoue
                fichier.delete();

                btnVerify.setEnabled(true);
                btnVerify.setText("Vérifier");

                Toast.makeText(
                        this,
                        "Étudiant introuvable dans SQLite !",
                        Toast.LENGTH_LONG
                ).show();
            }

        } catch (Exception e) {

            e.printStackTrace();

            btnVerify.setEnabled(true);
            btnVerify.setText("Vérifier");

            Toast.makeText(
                    this,
                    "Erreur : " + e.getMessage(),
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
