
package com.example.diar;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class DetailAnnonce extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    private ImageView imgDetailAnnonce;
    private TextView tvDetailTitre, tvDetailPrix, tvDetailDescription;
    private TextView tvDetailPersonnes, tvDetailLatitude, tvDetailLongitude;

    private LinearLayout layoutPhotos;
    private MapView mapDetailAnnonce;

    private Button btnContacter, btnDemanderVisite;

    private long idAnnonce = -1;

    private String etudiantEmail = "";
    private String etudiantNom = "";

    private String proprietaireEmail = "";
    private String proprietaireNom = "";

    private double latitude = 0;
    private double longitude = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_annonce);

        dbHelper = new DatabaseHelper(this);

        Configuration.getInstance().setUserAgentValue(getPackageName());

        imgDetailAnnonce = findViewById(R.id.imgDetailAnnonce);
        tvDetailTitre = findViewById(R.id.tvDetailTitre);
        tvDetailPrix = findViewById(R.id.tvDetailPrix);
        tvDetailDescription = findViewById(R.id.tvDetailDescription);
        tvDetailPersonnes = findViewById(R.id.tvDetailPersonnes);
        tvDetailLatitude = findViewById(R.id.tvDetailLatitude);
        tvDetailLongitude = findViewById(R.id.tvDetailLongitude);

        layoutPhotos = findViewById(R.id.layoutPhotos);
        mapDetailAnnonce = findViewById(R.id.mapDetailAnnonce);

        btnContacter = findViewById(R.id.btnContacter);
        btnDemanderVisite = findViewById(R.id.btnDemanderVisite);

        Button btnRetour = findViewById(R.id.btnRetourDetail);
        btnRetour.setOnClickListener(v -> finish());

        mapDetailAnnonce.setTileSource(TileSourceFactory.MAPNIK);
        mapDetailAnnonce.setMultiTouchControls(true);

        // Récupération de la session étudiant
        SessionManager session = new SessionManager(this);
        etudiantEmail = session.getEmail();

        etudiantNom = getIntent().getStringExtra("etudiantNom");

        if (etudiantNom == null || etudiantNom.trim().isEmpty()) {
            etudiantNom = "Étudiant";
        }

        // Récupération ID annonce
        idAnnonce = getIntent().getLongExtra("idAnnonce", -1);

        if (idAnnonce <= 0) {
            idAnnonce = getIntent().getIntExtra("idAnnonce", -1);
        }

        if (idAnnonce <= 0) {
            Toast.makeText(this,
                    "ID annonce introuvable",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btnContacter.setEnabled(false);

        // Bouton Chat
        btnContacter.setOnClickListener(v -> ouvrirChat());

        // Bouton Demande de visite
        btnDemanderVisite.setOnClickListener(v -> ouvrirInterface15());

        // Charger les informations
        chargerDetailAnnonce();
    }

    // =========================================
    // CHARGER DETAIL ANNONCE
    // =========================================

    private void chargerDetailAnnonce() {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = null;

        try {

            cursor = db.query(
                    "Annonce",
                    null,
                    "id=?",
                    new String[]{String.valueOf(idAnnonce)},
                    null, null, null
            );

            if (!cursor.moveToFirst()) {

                Toast.makeText(this,
                        "Annonce introuvable",
                        Toast.LENGTH_LONG).show();

                finish();
                return;
            }

            String titre = cursor.getString(
                    cursor.getColumnIndexOrThrow("titre"));

            String description = cursor.getString(
                    cursor.getColumnIndexOrThrow("description"));

            double prix = cursor.getDouble(
                    cursor.getColumnIndexOrThrow("prix"));

            int nbPersonnes = cursor.getInt(
                    cursor.getColumnIndexOrThrow("nbPersonnes"));

            latitude = cursor.getDouble(
                    cursor.getColumnIndexOrThrow("latitude"));

            longitude = cursor.getDouble(
                    cursor.getColumnIndexOrThrow("longitude"));

            proprietaireEmail = cursor.getString(
                    cursor.getColumnIndexOrThrow("emailProprietaire"));

            proprietaireNom = cursor.getString(
                    cursor.getColumnIndexOrThrow("nomProprietaire"));

            tvDetailTitre.setText(titre);

            tvDetailDescription.setText(
                    description == null ? "Aucune description" : description
            );

            tvDetailPrix.setText(prix + " DT / mois");

            tvDetailPersonnes.setText(
                    "Nombre de personnes : " + nbPersonnes
            );

            tvDetailLatitude.setText("Latitude : " + latitude);
            tvDetailLongitude.setText("Longitude : " + longitude);

            if (proprietaireNom == null || proprietaireNom.trim().isEmpty()
                    || proprietaireEmail == null || proprietaireEmail.trim().isEmpty()) {

                recupererProprietaire();
            }

            afficherToutesLesPhotos();

            if (latitude >= -90 && latitude <= 90
                    && longitude >= -180 && longitude <= 180
                    && (latitude != 0 || longitude != 0)) {

                afficherLocalisation(latitude, longitude);
            }

            btnContacter.setEnabled(true);

        } catch (Exception e) {

            Toast.makeText(this,
                    "Erreur : " + e.getMessage(),
                    Toast.LENGTH_LONG).show();

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // =========================================
    // RECUPERER PROPRIETAIRE
    // =========================================

    private void recupererProprietaire() {

        if (proprietaireEmail == null) proprietaireEmail = "";
        if (proprietaireNom == null) proprietaireNom = "";

        if (!proprietaireEmail.isEmpty() && !proprietaireNom.isEmpty()) {
            return;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                "Proprietaire",
                new String[]{"nom", "email"},
                "email=?",
                new String[]{proprietaireEmail},
                null, null, null
        );

        if (cursor.moveToFirst()) {

            proprietaireNom = cursor.getString(
                    cursor.getColumnIndexOrThrow("nom"));

            proprietaireEmail = cursor.getString(
                    cursor.getColumnIndexOrThrow("email"));
        }

        cursor.close();
    }

    // =========================================
    // AFFICHER PHOTOS
    // =========================================

    private void afficherToutesLesPhotos() {

        layoutPhotos.removeAllViews();
        imgDetailAnnonce.setImageDrawable(null);

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                "AnnoncePhoto",
                new String[]{"photoPath"},
                "annonce_id=?",
                new String[]{String.valueOf(idAnnonce)},
                null, null, "id ASC"
        );

        int index = 0;

        while (cursor.moveToNext()) {

            String chemin = cursor.getString(
                    cursor.getColumnIndexOrThrow("photoPath"));

            if (chemin == null || chemin.trim().isEmpty()) {
                continue;
            }

            Bitmap bitmap = BitmapFactory.decodeFile(chemin);

            if (bitmap == null) {
                continue;
            }

            if (index == 0) {
                imgDetailAnnonce.setImageBitmap(bitmap);
            }

            ImageView miniature = new ImageView(this);

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            dpToPx(100),
                            dpToPx(90)
                    );

            params.setMargins(
                    dpToPx(5),
                    dpToPx(5),
                    dpToPx(5),
                    dpToPx(5)
            );

            miniature.setLayoutParams(params);

            miniature.setScaleType(ImageView.ScaleType.CENTER_CROP);

            miniature.setImageBitmap(bitmap);

            miniature.setOnClickListener(v ->
                    imgDetailAnnonce.setImageBitmap(bitmap)
            );

            layoutPhotos.addView(miniature);

            index++;
        }

        cursor.close();

        if (index == 0) {
            imgDetailAnnonce.setImageResource(
                    android.R.drawable.ic_menu_gallery
            );
        }
    }

    // =========================================
    // AFFICHER LOCALISATION
    // =========================================

    private void afficherLocalisation(double lat, double lon) {

        GeoPoint position = new GeoPoint(lat, lon);

        mapDetailAnnonce.getController().setZoom(15.0);
        mapDetailAnnonce.getController().setCenter(position);

        Marker marker = new Marker(mapDetailAnnonce);

        marker.setPosition(position);

        marker.setTitle("Localisation de l'annonce");

        marker.setAnchor(
                Marker.ANCHOR_CENTER,
                Marker.ANCHOR_BOTTOM
        );

        mapDetailAnnonce.getOverlays().clear();

        mapDetailAnnonce.getOverlays().add(marker);

        mapDetailAnnonce.invalidate();
    }

    // =========================================
    // OUVRIR CHAT
    // =========================================

    private void ouvrirChat() {

        if (etudiantEmail == null || etudiantEmail.trim().isEmpty()) {

            Toast.makeText(this,
                    "Veuillez vous connecter",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (proprietaireEmail == null || proprietaireEmail.trim().isEmpty()) {

            Toast.makeText(this,
                    "Email du propriétaire introuvable",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (etudiantEmail.equalsIgnoreCase(proprietaireEmail)) {

            Toast.makeText(this,
                    "Vous ne pouvez pas contacter votre propre annonce",
                    Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(
                DetailAnnonce.this,
                ChatEtudiant.class
        );

        intent.putExtra("etudiantEmail", etudiantEmail);
        intent.putExtra("etudiantNom", etudiantNom);
        intent.putExtra("proprietaireEmail", proprietaireEmail);

        intent.putExtra(
                "proprietaireNom",
                proprietaireNom == null || proprietaireNom.isEmpty()
                        ? "Propriétaire" : proprietaireNom
        );

        intent.putExtra("annonceId", idAnnonce);

        startActivity(intent);
    }

    // =========================================
    // OUVRIR INTERFACE15
    // =========================================

    private void ouvrirInterface15() {

        if (etudiantEmail == null || etudiantEmail.trim().isEmpty()) {

            Toast.makeText(this,
                    "Veuillez vous connecter",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (proprietaireEmail == null || proprietaireEmail.trim().isEmpty()) {

            Toast.makeText(this,
                    "Propriétaire introuvable",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (etudiantEmail.equalsIgnoreCase(proprietaireEmail)) {

            Toast.makeText(this,
                    "Vous ne pouvez pas réserver votre propre annonce",
                    Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(
                DetailAnnonce.this,
                Interface15.class
        );

        intent.putExtra("idAnnonce", idAnnonce);
        intent.putExtra("etudiantEmail", etudiantEmail);
        intent.putExtra("etudiantNom", etudiantNom);
        intent.putExtra("proprietaireEmail", proprietaireEmail);
        intent.putExtra("proprietaireNom", proprietaireNom);

        startActivity(intent);
    }

    // =========================================
    // DP TO PX
    // =========================================

    private int dpToPx(int dp) {

        return (int) (
                dp * getResources().getDisplayMetrics().density
        );
    }

    // =========================================
    // MAP LIFECYCLE
    // =========================================

    @Override
    protected void onResume() {
        super.onResume();

        if (mapDetailAnnonce != null) {
            mapDetailAnnonce.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mapDetailAnnonce != null) {
            mapDetailAnnonce.onPause();
        }
    }

    @Override
    protected void onDestroy() {

        if (mapDetailAnnonce != null) {
            mapDetailAnnonce.onDetach();
        }

        super.onDestroy();
    }
}
