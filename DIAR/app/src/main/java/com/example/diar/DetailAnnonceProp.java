package com.example.diar;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class DetailAnnonceProp extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    private ImageView imgDetailProp;
    private LinearLayout layoutPhotosProp;

    private TextView tvDetailTitreProp;
    private TextView tvDetailPrixProp;
    private TextView tvDetailPersonnesProp;
    private TextView tvDetailDescriptionProp;
    private TextView tvDetailLatitudeProp;
    private TextView tvDetailLongitudeProp;

    private MapView mapDetailProp;

    private Button btnSupprimerAnnonce;
    private Button btnRetourDetailProp;

    private long idAnnonce = -1;

    private double latitude = 0;
    private double longitude = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_annonce_prop);

        dbHelper = new DatabaseHelper(this);

        idAnnonce = getIntent().getLongExtra("idAnnonce", -1);

        if (idAnnonce <= 0) {
            idAnnonce = getIntent().getIntExtra("idAnnonce", -1);
        }

        if (idAnnonce <= 0) {
            Toast.makeText(this, "ID annonce introuvable",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        imgDetailProp = findViewById(R.id.imgDetailProp);
        layoutPhotosProp = findViewById(R.id.layoutPhotosProp);

        tvDetailTitreProp = findViewById(R.id.tvDetailTitreProp);
        tvDetailPrixProp = findViewById(R.id.tvDetailPrixProp);
        tvDetailPersonnesProp = findViewById(R.id.tvDetailPersonnesProp);
        tvDetailDescriptionProp = findViewById(R.id.tvDetailDescriptionProp);
        tvDetailLatitudeProp = findViewById(R.id.tvDetailLatitudeProp);
        tvDetailLongitudeProp = findViewById(R.id.tvDetailLongitudeProp);

        mapDetailProp = findViewById(R.id.mapDetailProp);

        btnSupprimerAnnonce = findViewById(R.id.btnSupprimerAnnonce);
        btnRetourDetailProp = findViewById(R.id.btnRetourDetailProp);

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue("DIAR/1.0 (Android; com.example.diar)");
        Configuration.getInstance().setTileDownloadThreads((short) 2);
        Configuration.getInstance().setTileDownloadMaxQueueSize((short) 20);
        Configuration.getInstance().setCacheMapTileCount((short) 200);

        // ✅ NOUVEAU - OSM Allemagne, sans clé API, sans 403
        mapDetailProp.setTileSource(new XYTileSource("OSM_DE",
                0, 19, 256, ".png",
                new String[]{"https://tile.openstreetmap.de/"}));
        mapDetailProp.setMultiTouchControls(true);

        btnRetourDetailProp.setOnClickListener(v -> finish());
        btnSupprimerAnnonce.setOnClickListener(v -> confirmerSuppression());

        chargerDetail();
    }

    private void chargerDetail() {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                "Annonce",
                null,
                "id=?",
                new String[]{String.valueOf(idAnnonce)},
                null, null, null
        );

        if (!cursor.moveToFirst()) {
            cursor.close();
            Toast.makeText(this, "Annonce introuvable", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String titre = cursor.getString(cursor.getColumnIndexOrThrow("titre"));
        String description = cursor.getString(
                cursor.getColumnIndexOrThrow("description")
        );

        double prix = cursor.getDouble(cursor.getColumnIndexOrThrow("prix"));
        int nbPersonnes = cursor.getInt(
                cursor.getColumnIndexOrThrow("nbPersonnes")
        );

        latitude = cursor.getDouble(cursor.getColumnIndexOrThrow("latitude"));
        longitude = cursor.getDouble(cursor.getColumnIndexOrThrow("longitude"));

        tvDetailTitreProp.setText(titre);
        tvDetailDescriptionProp.setText(
                description == null ? "Aucune description" : description
        );
        tvDetailPrixProp.setText(prix + " DT / mois");
        tvDetailPersonnesProp.setText("Nombre de personnes : " + nbPersonnes);

        tvDetailLatitudeProp.setText("Latitude : " + latitude);
        tvDetailLongitudeProp.setText("Longitude : " + longitude);

        cursor.close();

        afficherPhotos();

        if (latitude >= -90 && latitude <= 90
                && longitude >= -180 && longitude <= 180
                && (latitude != 0 || longitude != 0)) {
            afficherLocalisation(titre);
        }
    }

    private void afficherPhotos() {

        layoutPhotosProp.removeAllViews();
        imgDetailProp.setImageDrawable(null);

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
                    cursor.getColumnIndexOrThrow("photoPath")
            );

            if (chemin == null || chemin.trim().isEmpty()) continue;

            Bitmap bitmap = BitmapFactory.decodeFile(chemin);

            if (bitmap == null) continue;

            if (index == 0) {
                imgDetailProp.setImageBitmap(bitmap);
            }

            ajouterPhotoGalerie(bitmap);
            index++;
        }

        cursor.close();

        if (index == 0) {
            imgDetailProp.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    private void ajouterPhotoGalerie(Bitmap bitmap) {

        ImageView img = new ImageView(this);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(dp(100), dp(100));

        params.setMargins(0, 0, dp(8), 0);

        img.setLayoutParams(params);
        img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        img.setImageBitmap(bitmap);

        img.setOnClickListener(v -> imgDetailProp.setImageBitmap(bitmap));

        layoutPhotosProp.addView(img);
    }

    private void afficherLocalisation(String titre) {

        GeoPoint point = new GeoPoint(latitude, longitude);

        mapDetailProp.getController().setZoom(15.0);
        mapDetailProp.getController().setCenter(point);

        Marker marker = new Marker(mapDetailProp);
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle(titre);

        mapDetailProp.getOverlays().clear();
        mapDetailProp.getOverlays().add(marker);

        mapDetailProp.invalidate();
    }

    private void confirmerSuppression() {

        new AlertDialog.Builder(this)
                .setTitle("Supprimer cette annonce ?")
                .setMessage("Cette annonce et ses photos seront supprimées.")
                .setPositiveButton("Oui, supprimer",
                        (dialog, which) -> supprimerAnnonce())
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void supprimerAnnonce() {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.beginTransaction();

        try {
            String annonceId = String.valueOf(idAnnonce);

            // Supprimer les photos liées à l'annonce
            db.delete(
                    "AnnoncePhoto",
                    "annonce_id=?",
                    new String[]{annonceId}
            );

            // Supprimer les favoris liés à l'annonce
            db.delete(
                    "Favoris",
                    "annonce_id=?",
                    new String[]{annonceId}
            );

            // Supprimer les demandes de visite liées à l'annonce
            db.delete(
                    "Reservation",
                    "annonce_id=?",
                    new String[]{annonceId}
            );

            // Supprimer l'annonce
            int lignes = db.delete(
                    "Annonce",
                    "id=?",
                    new String[]{annonceId}
            );

            if (lignes > 0) {
                db.setTransactionSuccessful();

                Toast.makeText(this, "Annonce supprimée",
                        Toast.LENGTH_SHORT).show();

                finish();
            } else {
                Toast.makeText(this, "Annonce introuvable",
                        Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this,
                    "Erreur suppression : " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        } finally {
            db.endTransaction();
        }
    }

    private int dp(int valeur) {
        return (int) (valeur * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapDetailProp != null) mapDetailProp.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapDetailProp != null) mapDetailProp.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mapDetailProp != null) mapDetailProp.onDetach();
        super.onDestroy();
    }
}