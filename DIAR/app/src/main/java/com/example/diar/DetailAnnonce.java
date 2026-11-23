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
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;
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

        // ✅ Configuration OSMDroid - OBLIGATOIRE avant setContentView pour la carte
        Configuration.getInstance().load(
                getApplicationContext(),
                getSharedPreferences("osmdroid", MODE_PRIVATE)
        );
        Configuration.getInstance().setUserAgentValue("DIAR/1.0 (Android; com.example.diar)");
        Configuration.getInstance().setTileDownloadThreads((short) 2);
        Configuration.getInstance().setTileDownloadMaxQueueSize((short) 20);
        Configuration.getInstance().setCacheMapTileCount((short) 200);

        setContentView(R.layout.detail_annonce);

        dbHelper = new DatabaseHelper(this);

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
        if (btnRetour != null) btnRetour.setOnClickListener(v -> finish());

        // ✅ OSM Allemagne - sans 403, comme Interface9
        if (mapDetailAnnonce != null) {
            mapDetailAnnonce.setTileSource(new XYTileSource("OSM_DE",
                    0, 19, 256, ".png",
                    new String[]{"https://tile.openstreetmap.de/"}));
            mapDetailAnnonce.setMultiTouchControls(true);
            mapDetailAnnonce.setUseDataConnection(true);
        }

        // Récupération de la session étudiant
        SessionManager session = new SessionManager(this);
        etudiantEmail = session.getEmail();
        if (etudiantEmail == null) etudiantEmail = "";
        if (etudiantEmail.isEmpty()) {
            etudiantEmail = getIntent().getStringExtra("USER_EMAIL");
            if (etudiantEmail == null) etudiantEmail = "";
        }

        etudiantNom = getIntent().getStringExtra("etudiantNom");
        if (etudiantNom == null || etudiantNom.trim().isEmpty()) {
            etudiantNom = "Étudiant";
        }

        // ✅ Récupération ID annonce - compatible INT et LONG
        // AnnonceAdapter envoie un INT, donc on lit INT d'abord
        if (getIntent().hasExtra("idAnnonce")) {
            try {
                idAnnonce = getIntent().getIntExtra("idAnnonce", -1);
            } catch (Exception e) {
                idAnnonce = -1;
            }
            if (idAnnonce <= 0) {
                try {
                    idAnnonce = getIntent().getLongExtra("idAnnonce", -1);
                } catch (Exception e) {
                    idAnnonce = -1;
                }
            }
        }
        // Ancien nom, au cas où
        if (idAnnonce <= 0 && getIntent().hasExtra("annonceId")) {
            idAnnonce = getIntent().getLongExtra("annonceId", -1);
        }

        if (idAnnonce <= 0) {
            Toast.makeText(this, "ID annonce introuvable", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (btnContacter != null) btnContacter.setEnabled(false);

        // Bouton Chat
        if (btnContacter != null) btnContacter.setOnClickListener(v -> ouvrirChat());

        // Bouton Demande de visite
        if (btnDemanderVisite != null) btnDemanderVisite.setOnClickListener(v -> ouvrirInterface15());

        // Charger les informations
        chargerDetailAnnonce();
    }

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

            if (cursor == null || !cursor.moveToFirst()) {
                Toast.makeText(this, "Annonce introuvable", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            String titre = getString(cursor, "titre");
            String description = getString(cursor, "description");
            double prix = getDouble(cursor, "prix");
            int nbPersonnes = getInt(cursor, "nbPersonnes");
            int nbChambres = getInt(cursor, "nbChambres");
            double surface = getDouble(cursor, "surface");
            String ville = getString(cursor, "ville");
            String quartier = getString(cursor, "quartier");
            String adresse = getString(cursor, "adresse");

            latitude = getDouble(cursor, "latitude");
            longitude = getDouble(cursor, "longitude");

            proprietaireEmail = getString(cursor, "emailProprietaire");
            proprietaireNom = getString(cursor, "nomProprietaire");

            tvDetailTitre.setText(titre.isEmpty() ? "Sans titre" : titre);
            tvDetailDescription.setText(
                    (description == null || description.isEmpty()) ? "Aucune description" : description
            );
            tvDetailPrix.setText(prix + " DT / mois");
            tvDetailPersonnes.setText(
                    "Nombre de personnes : " + nbPersonnes +
                            " • Chambres : " + nbChambres +
                            " • Surface : " + surface + " m²" +
                            "\n📍 " + quartier + ", " + ville +
                            (adresse.isEmpty() ? "" : "\n" + adresse)
            );

            tvDetailLatitude.setText("Latitude : " + latitude);
            tvDetailLongitude.setText("Longitude : " + longitude);

            // ✅ Si le proprio est vide, on ne cherche pas avec un email vide
            // On garde juste "Propriétaire" comme nom par défaut
            if (proprietaireNom == null || proprietaireNom.trim().isEmpty()) {
                proprietaireNom = "Propriétaire";
            }

            afficherToutesLesPhotos();

            if (latitude >= -90 && latitude <= 90
                    && longitude >= -180 && longitude <= 180
                    && (latitude != 0 || longitude != 0)) {
                afficherLocalisation(latitude, longitude);
            } else {
                if (tvDetailLatitude != null) tvDetailLatitude.setText("Localisation non disponible sur la carte");
            }

            if (btnContacter != null) btnContacter.setEnabled(true);

        } catch (Exception e) {
            Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void afficherToutesLesPhotos() {
        if (layoutPhotos == null) return;
        layoutPhotos.removeAllViews();
        if (imgDetailAnnonce != null) imgDetailAnnonce.setImageDrawable(null);

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
            String chemin = cursor.getString(cursor.getColumnIndexOrThrow("photoPath"));
            if (chemin == null || chemin.trim().isEmpty()) continue;
            Bitmap bitmap = BitmapFactory.decodeFile(chemin);
            if (bitmap == null) continue;

            if (index == 0 && imgDetailAnnonce != null) {
                imgDetailAnnonce.setImageBitmap(bitmap);
            }

            ImageView miniature = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(100), dpToPx(90));
            params.setMargins(dpToPx(5), dpToPx(5), dpToPx(5), dpToPx(5));
            miniature.setLayoutParams(params);
            miniature.setScaleType(ImageView.ScaleType.CENTER_CROP);
            miniature.setImageBitmap(bitmap);
            miniature.setOnClickListener(v -> {
                if (imgDetailAnnonce != null) imgDetailAnnonce.setImageBitmap(bitmap);
            });
            layoutPhotos.addView(miniature);
            index++;
        }
        cursor.close();

        if (index == 0 && imgDetailAnnonce != null) {
            imgDetailAnnonce.setImageResource(R.drawable.appart);
        }
    }

    private void afficherLocalisation(double lat, double lon) {
        if (mapDetailAnnonce == null) return;
        GeoPoint position = new GeoPoint(lat, lon);
        mapDetailAnnonce.getController().setZoom(15.0);
        mapDetailAnnonce.getController().setCenter(position);

        Marker marker = new Marker(mapDetailAnnonce);
        marker.setPosition(position);
        marker.setTitle("Localisation de l'annonce");
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        mapDetailAnnonce.getOverlays().clear();
        mapDetailAnnonce.getOverlays().add(marker);
        mapDetailAnnonce.getOverlays().add(new CopyrightOverlay(this));
        mapDetailAnnonce.invalidate();
    }

    private void ouvrirChat() {
        if (etudiantEmail == null || etudiantEmail.trim().isEmpty()) {
            Toast.makeText(this, "Veuillez vous connecter", Toast.LENGTH_LONG).show();
            return;
        }
        if (proprietaireEmail == null || proprietaireEmail.trim().isEmpty()) {
            Toast.makeText(this, "Email du propriétaire introuvable", Toast.LENGTH_LONG).show();
            return;
        }
        if (etudiantEmail.equalsIgnoreCase(proprietaireEmail)) {
            Toast.makeText(this, "Vous ne pouvez pas contacter votre propre annonce", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(DetailAnnonce.this, ChatEtudiant.class);
        intent.putExtra("etudiantEmail", etudiantEmail);
        intent.putExtra("etudiantNom", etudiantNom);
        intent.putExtra("proprietaireEmail", proprietaireEmail);
        intent.putExtra("proprietaireNom", proprietaireNom == null || proprietaireNom.isEmpty() ? "Propriétaire" : proprietaireNom);
        intent.putExtra("annonceId", idAnnonce);
        startActivity(intent);
    }

    private void ouvrirInterface15() {
        if (etudiantEmail == null || etudiantEmail.trim().isEmpty()) {
            Toast.makeText(this, "Veuillez vous connecter", Toast.LENGTH_LONG).show();
            return;
        }
        if (proprietaireEmail == null || proprietaireEmail.trim().isEmpty()) {
            Toast.makeText(this, "Propriétaire introuvable", Toast.LENGTH_LONG).show();
            return;
        }
        if (etudiantEmail.equalsIgnoreCase(proprietaireEmail)) {
            Toast.makeText(this, "Vous ne pouvez pas réserver votre propre annonce", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(DetailAnnonce.this, Interface15.class);
        intent.putExtra("idAnnonce", idAnnonce);
        intent.putExtra("etudiantEmail", etudiantEmail);
        intent.putExtra("etudiantNom", etudiantNom);
        intent.putExtra("proprietaireEmail", proprietaireEmail);
        intent.putExtra("proprietaireNom", proprietaireNom);
        startActivity(intent);
    }

    private String getString(Cursor cursor, String column) {
        int idx = cursor.getColumnIndex(column);
        if (idx == -1 || cursor.isNull(idx)) return "";
        String v = cursor.getString(idx);
        return v == null ? "" : v;
    }

    private int getInt(Cursor cursor, String column) {
        int idx = cursor.getColumnIndex(column);
        if (idx == -1 || cursor.isNull(idx)) return 0;
        return cursor.getInt(idx);
    }

    private double getDouble(Cursor cursor, String column) {
        int idx = cursor.getColumnIndex(column);
        if (idx == -1 || cursor.isNull(idx)) return 0;
        return cursor.getDouble(idx);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapDetailAnnonce != null) mapDetailAnnonce.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapDetailAnnonce != null) mapDetailAnnonce.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mapDetailAnnonce != null) mapDetailAnnonce.onDetach();
        super.onDestroy();
    }
}