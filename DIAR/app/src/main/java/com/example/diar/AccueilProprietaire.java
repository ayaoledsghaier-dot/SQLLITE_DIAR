package com.example.diar;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.File;

public class AccueilProprietaire extends AppCompatActivity {

    private ShapeableImageView imgOwnerProfile;
    private TextView tvOwnerGreeting;
    private TextView tvListingsCount;
    private TextView tvViewsCount;
    private TextView tvRequestsCount;
    private TextView tvVisitsCount;

    private LinearLayout layoutEmptyListings;
    private LinearLayout layoutListings;

    private DatabaseHelper databaseHelper;

    private String nomProprietaire;
    private String emailProprietaire;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface18);

        NavigationProprietaire.setup(this, R.id.nav_prop_accueil);

        databaseHelper = new DatabaseHelper(this);

        imgOwnerProfile = findViewById(R.id.imgOwnerProfile);
        tvOwnerGreeting = findViewById(R.id.tvOwnerGreeting);

        tvListingsCount = findViewById(R.id.tvListingsCount);
        tvViewsCount = findViewById(R.id.tvViewsCount);
        tvRequestsCount = findViewById(R.id.tvRequestsCount);
        tvVisitsCount = findViewById(R.id.tvVisitsCount);

        layoutEmptyListings = findViewById(R.id.layoutEmptyListings);
        layoutListings = findViewById(R.id.layoutListings);

        emailProprietaire = getIntent().getStringExtra("emailProprietaire");
        nomProprietaire = getIntent().getStringExtra("nomProprietaire");

        if (emailProprietaire == null || emailProprietaire.trim().isEmpty()) {
            emailProprietaire = new SessionManager(this).getEmail();
        }

        if (emailProprietaire == null || emailProprietaire.trim().isEmpty()) {
            Toast.makeText(this, "Email introuvable, reconnectez-vous", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        chargerInformationsProprietaire();

        Button btnAddListing = findViewById(R.id.btnAddListing);

        if (btnAddListing != null) {
            btnAddListing.setOnClickListener(v -> {
                Intent intent = new Intent(
                        AccueilProprietaire.this,
                        AjouterAnnonce.class
                );

                intent.putExtra("nomProprietaire", nomProprietaire);
                intent.putExtra("emailProprietaire", emailProprietaire);
                intent.putExtra("email", emailProprietaire);

                startActivity(intent);
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (databaseHelper != null && emailProprietaire != null) {
            chargerAnnonces();
            chargerStatistiques();
        }
    }

    private void chargerInformationsProprietaire() {

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor cursor = db.query(
                "Proprietaire",
                new String[]{"nom", "email", "photoPath"},
                "email = ?",
                new String[]{emailProprietaire},
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {

            nomProprietaire = cursor.getString(
                    cursor.getColumnIndexOrThrow("nom")
            );

            String photoPath = cursor.getString(
                    cursor.getColumnIndexOrThrow("photoPath")
            );

            if (photoPath != null && !photoPath.isEmpty()) {
                File photo = new File(photoPath);

                if (photo.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(photoPath);

                    if (bitmap != null) {
                        imgOwnerProfile.setImageBitmap(bitmap);
                    }
                }
            }
        }

        cursor.close();

        if (nomProprietaire != null && !nomProprietaire.trim().isEmpty()) {
            tvOwnerGreeting.setText("Bonjour " + nomProprietaire + " 👋");
        } else {
            tvOwnerGreeting.setText("Bonjour 👋");
        }
    }

    private void chargerAnnonces() {

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor cursor = db.query(
                "Annonce",
                new String[]{
                        "id",
                        "titre",
                        "description",
                        "prix",
                        "ville",
                        "adresse"
                },
                "emailProprietaire = ?",
                new String[]{emailProprietaire},
                null,
                null,
                "id DESC"
        );

        layoutListings.removeAllViews();

        int nombreAnnonces = cursor.getCount();

        tvListingsCount.setText(String.valueOf(nombreAnnonces));

        if (nombreAnnonces == 0) {
            layoutEmptyListings.setVisibility(View.VISIBLE);
            layoutListings.setVisibility(View.GONE);
            cursor.close();
            return;
        }

        layoutEmptyListings.setVisibility(View.GONE);
        layoutListings.setVisibility(View.VISIBLE);

        while (cursor.moveToNext()) {

            long id = cursor.getLong(
                    cursor.getColumnIndexOrThrow("id")
            );

            String titre = cursor.getString(
                    cursor.getColumnIndexOrThrow("titre")
            );

            String description = cursor.getString(
                    cursor.getColumnIndexOrThrow("description")
            );

            String prix = cursor.getString(
                    cursor.getColumnIndexOrThrow("prix")
            );

            String ville = cursor.getString(
                    cursor.getColumnIndexOrThrow("ville")
            );

            String adresse = cursor.getString(
                    cursor.getColumnIndexOrThrow("adresse")
            );

            String photoPath = recupererPremierePhoto(db, id);

            ajouterCarteAnnonce(
                    id,
                    titre,
                    description,
                    prix,
                    ville,
                    adresse,
                    photoPath
            );
        }

        cursor.close();
    }

    private String recupererPremierePhoto(SQLiteDatabase db, long annonceId) {

        Cursor cursor = db.query(
                "AnnoncePhoto",
                new String[]{"photoPath"},
                "annonce_id = ?",
                new String[]{String.valueOf(annonceId)},
                null,
                null,
                "id ASC",
                "1"
        );

        String chemin = null;

        if (cursor.moveToFirst()) {
            chemin = cursor.getString(
                    cursor.getColumnIndexOrThrow("photoPath")
            );
        }

        cursor.close();

        return chemin;
    }

    private void ajouterCarteAnnonce(
            long id,
            String titre,
            String description,
            String prix,
            String ville,
            String adresse,
            String photoPath
    ) {

        MaterialCardView carte = new MaterialCardView(this);

        LinearLayout.LayoutParams carteParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        carteParams.setMargins(0, 0, 0, dp(14));

        carte.setLayoutParams(carteParams);
        carte.setRadius(dp(16));
        carte.setCardElevation(dp(3));
        carte.setCardBackgroundColor(Color.WHITE);

        LinearLayout contenu = new LinearLayout(this);
        contenu.setOrientation(LinearLayout.VERTICAL);
        contenu.setPadding(dp(14), dp(14), dp(14), dp(14));

        if (photoPath != null && !photoPath.trim().isEmpty()) {

            File fichier = new File(photoPath);

            if (fichier.exists()) {

                Bitmap bitmap = BitmapFactory.decodeFile(photoPath);

                if (bitmap != null) {
                    ImageView image = new ImageView(this);

                    LinearLayout.LayoutParams imageParams =
                            new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    dp(180)
                            );

                    image.setLayoutParams(imageParams);
                    image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    image.setImageBitmap(bitmap);

                    contenu.addView(image);
                }
            }
        }

        TextView tvTitre = creerTexte(titre, 18, true, "#0F172A");
        tvTitre.setPadding(0, dp(10), 0, dp(5));
        contenu.addView(tvTitre);

        TextView tvPrix = creerTexte(
                prix + " DT / mois",
                16,
                true,
                "#0D9488"
        );

        contenu.addView(tvPrix);

        TextView tvDescription = creerTexte(
                description,
                14,
                false,
                "#475569"
        );

        tvDescription.setPadding(0, dp(8), 0, dp(8));
        contenu.addView(tvDescription);

        TextView tvAdresse = creerTexte(
                "📍 " + (adresse == null ? "" : adresse)
                        + " " + (ville == null ? "" : ville),
                14,
                false,
                "#64748B"
        );

        contenu.addView(tvAdresse);

        carte.addView(contenu);

        carte.setClickable(true);
        carte.setFocusable(true);

        carte.setOnClickListener(v -> {
            Intent intent = new Intent(
                    AccueilProprietaire.this,
                    DetailAnnonceProp.class
            );

            intent.putExtra("idAnnonce", id);
            startActivity(intent);
        });

        layoutListings.addView(carte);
    }

    private void chargerStatistiques() {

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        // Nombre de demandes de réservation
        Cursor demandes = db.rawQuery(
                "SELECT COUNT(*) FROM Reservation WHERE emailProprietaire = ?",
                new String[]{emailProprietaire}
        );

        int nombreDemandes = 0;

        if (demandes.moveToFirst()) {
            nombreDemandes = demandes.getInt(0);
        }

        demandes.close();

        // Nombre de visites enregistrées comme réservation avec statut Visite
        Cursor visites = db.rawQuery(
                "SELECT COUNT(*) FROM Reservation " +
                        "WHERE emailProprietaire = ? AND statut = ?",
                new String[]{emailProprietaire, "Visite"}
        );

        int nombreVisites = 0;

        if (visites.moveToFirst()) {
            nombreVisites = visites.getInt(0);
        }

        visites.close();

        tvRequestsCount.setText(String.valueOf(nombreDemandes));
        tvVisitsCount.setText(String.valueOf(nombreVisites));

        // Le schéma actuel ne contient pas de compteur de vues.
        tvViewsCount.setText("0");
    }

    private TextView creerTexte(
            String texte,
            int taille,
            boolean gras,
            String couleur
    ) {

        TextView textView = new TextView(this);

        textView.setText(texte == null ? "" : texte);
        textView.setTextSize(taille);
        textView.setTextColor(Color.parseColor(couleur));

        if (gras) {
            textView.setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
            );
        }

        return textView;
    }

    private int dp(int valeur) {
        return (int) (
                valeur * getResources().getDisplayMetrics().density
        );
    }
}