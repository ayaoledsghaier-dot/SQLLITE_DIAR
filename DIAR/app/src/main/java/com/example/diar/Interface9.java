package com.example.diar;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class Interface9 extends AppCompatActivity {

    private static final String TAG = "DIAR_INTERFACE9";
    private static final int LOCATION_PERMISSION_CODE = 100;

    private MapView mapView;
    private ImageView imgAppartement;
    private TextView tvTitre, tvPrix, tvDistance, tvNote;
    private TextView tvFacInfo;
    private Spinner spinnerRayon;
    private ImageButton btnBack;

    private DatabaseHelper databaseHelper;

    private final List<JSONObject> annonces = new ArrayList<>();

    // Position de la FAC - par défaut Tunis, on la remplace par la vraie fac
    private double latitudeFac = 36.8065;
    private double longitudeFac = 10.1815;
    private String nomFac = "Ma Faculté";
    private String emailEtudiant = "";

    // Rayon en km - l'étudiant peut changer : 2, 5, 10 km
    private double rayonKm = 5.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(
                getApplicationContext(),
                getSharedPreferences("osmdroid", MODE_PRIVATE)
        );

        Configuration.getInstance().setUserAgentValue(
                "DIAR/1.0 (Android; com.example.diar)"
        );
        Configuration.getInstance().setTileDownloadThreads((short) 2);
        Configuration.getInstance().setTileDownloadMaxQueueSize((short) 20);
        Configuration.getInstance().setCacheMapTileCount((short) 200);

        setContentView(R.layout.interface9);

        NavigationEtudiant.setup(this, 1);

        mapView = findViewById(R.id.imageView5);
        imgAppartement = findViewById(R.id.imageView13);

        tvTitre = findViewById(R.id.textView13);
        tvPrix = findViewById(R.id.textView14);
        tvDistance = findViewById(R.id.textView15);
        tvNote = findViewById(R.id.textView16);

        tvFacInfo = findViewById(R.id.tvFacInfo);
        spinnerRayon = findViewById(R.id.spinnerRayon);

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        databaseHelper = new DatabaseHelper(this);

        // ✅ OSM Allemagne - sans 403, sans API KEY
        mapView.setTileSource(new XYTileSource("OSM_DE",
                0, 19, 256, ".png",
                new String[]{"https://tile.openstreetmap.de/"}));
        mapView.setMultiTouchControls(true);
        mapView.setUseDataConnection(true);
        mapView.getController().setZoom(13.0);

        GeoPoint tunis = new GeoPoint(36.8065, 10.1815);
        mapView.getController().setCenter(tunis);

        // --- Spinner rayon : 2km / 5km / 10km / Tous ---
        String[] rayons = {
                "2 km autour de ma fac",
                "5 km autour de ma fac",
                "10 km autour de ma fac",
                "Tous les logements"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                rayons
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRayon.setAdapter(adapter);
        spinnerRayon.setSelection(1); // 5 km par défaut
        spinnerRayon.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) rayonKm = 2.0;
                else if (position == 1) rayonKm = 5.0;
                else if (position == 2) rayonKm = 10.0;
                else rayonKm = 1000.0; // Tous

                if (tvFacInfo != null) {
                    if (rayonKm >= 1000) {
                        tvFacInfo.setText("🎓 " + nomFac + " • Tous les logements");
                    } else {
                        tvFacInfo.setText("🎓 " + nomFac + " • Logements à moins de " + (int) rayonKm + " km");
                    }
                }
                chargerAnnoncesDepuisSQLite();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // 1. On récupère l'email de l'étudiant connecté
        emailEtudiant = getEmailEtudiantConnecte();

        // 2. On cherche sa fac dans la BDD, puis on charge les annonces
        chargerFaculteEtudiant();
    }

    private String getEmailEtudiantConnecte() {
        // 👉 BRO : adapte cette ligne selon comment tu sauves l'email à la connexion
        String emailIntent = getIntent().getStringExtra("emailEtudiant");
        if (emailIntent != null && !emailIntent.isEmpty()) return emailIntent;

        String emailIntent2 = getIntent().getStringExtra("email");
        if (emailIntent2 != null && !emailIntent2.isEmpty()) return emailIntent2;

        SharedPreferences prefs = getSharedPreferences("DIAR_SESSION", MODE_PRIVATE);
        String emailPrefs = prefs.getString("emailEtudiant", "");
        if (!emailPrefs.isEmpty()) return emailPrefs;

        SharedPreferences prefs2 = getSharedPreferences("session", MODE_PRIVATE);
        return prefs2.getString("email", "");
    }

    private void chargerFaculteEtudiant() {
        if (emailEtudiant == null || emailEtudiant.isEmpty()) {
            Toast.makeText(this, "Fac non trouvée, affichage par défaut", Toast.LENGTH_SHORT).show();
            chargerAnnoncesDepuisSQLite();
            return;
        }

        new Thread(() -> {
            String faculte = "";
            String universite = "";
            String ville = "";

            Cursor cursor = null;
            try {
                cursor = databaseHelper.getEtudiant(emailEtudiant);
                if (cursor != null && cursor.moveToFirst()) {
                    faculte = getString(cursor, "faculte");
                    universite = getString(cursor, "universite");
                    ville = getString(cursor, "ville");
                }
            } catch (Exception e) {
                Log.e(TAG, "Erreur lecture etudiant", e);
            } finally {
                if (cursor != null) cursor.close();
            }

            String nomCompletFac = (faculte + " " + universite + " " + ville).trim();
            if (nomCompletFac.isEmpty()) nomCompletFac = "Tunis";
            nomFac = !faculte.isEmpty() ? faculte : nomCompletFac;

            // On convertit le nom de la fac en GPS avec Geocoder
            GeoPoint pointFac = geocoderFac(nomCompletFac);

            runOnUiThread(() -> {
                if (pointFac != null) {
                    latitudeFac = pointFac.getLatitude();
                    longitudeFac = pointFac.getLongitude();
                    mapView.getController().setZoom(13.0);
                    mapView.getController().setCenter(pointFac);
                    Toast.makeText(this, "Fac : " + nomFac, Toast.LENGTH_SHORT).show();
                    if (tvFacInfo != null) {
                        tvFacInfo.setText("🎓 " + nomFac + " • Logements à moins de " + (int) rayonKm + " km");
                    }
                }
                chargerAnnoncesDepuisSQLite();
            });

        }).start();
    }

    private GeoPoint geocoderFac(String nomFacComplet) {
        if (!Geocoder.isPresent()) return null;
        try {
            Geocoder geocoder = new Geocoder(this, Locale.FRANCE);
            List<Address> list = geocoder.getFromLocationName(nomFacComplet + ", Tunisie", 1);
            if (list != null && !list.isEmpty()) {
                Address a = list.get(0);
                return new GeoPoint(a.getLatitude(), a.getLongitude());
            }
            // 2ème essai : juste le nom
            List<Address> list2 = geocoder.getFromLocationName(nomFacComplet, 1);
            if (list2 != null && !list2.isEmpty()) {
                Address a = list2.get(0);
                return new GeoPoint(a.getLatitude(), a.getLongitude());
            }
        } catch (Exception e) {
            Log.e(TAG, "Geocoder fac erreur", e);
        }
        return null;
    }

    private void chargerAnnoncesDepuisSQLite() {
        annonces.clear();
        Cursor cursor = null;
        try {
            cursor = databaseHelper.getReadableDatabase().query(
                    "Annonce", null, "disponible = ?",
                    new String[]{"1"}, null, null, "id DESC"
            );

            while (cursor.moveToNext()) {
                JSONObject annonce = new JSONObject();
                int id = getInt(cursor, "id", -1);
                double latitude = getDouble(cursor, "latitude", 0);
                double longitude = getDouble(cursor, "longitude", 0);

                annonce.put("id", id);
                annonce.put("titre", getString(cursor, "titre"));
                annonce.put("description", getString(cursor, "description"));
                annonce.put("ville", getString(cursor, "ville"));
                annonce.put("quartier", getString(cursor, "quartier"));
                annonce.put("prix", getDouble(cursor, "prix", 0));
                annonce.put("latitude", latitude);
                annonce.put("longitude", longitude);

                Cursor photos = databaseHelper.getReadableDatabase().query(
                        "AnnoncePhoto", new String[]{"photoPath"},
                        "annonce_id = ?", new String[]{String.valueOf(id)},
                        null, null, "id ASC"
                );
                String photoPath = "";
                try {
                    if (photos.moveToFirst()) {
                        photoPath = photos.getString(photos.getColumnIndexOrThrow("photoPath"));
                    }
                } finally {
                    photos.close();
                }
                annonce.put("photo", photoPath);

                // ✅ Distance FAC -> LOGEMENT (pas GPS -> logement)
                if (latitude != 0 && longitude != 0) {
                    float[] resultat = new float[1];
                    Location.distanceBetween(latitudeFac, longitudeFac, latitude, longitude, resultat);
                    double distanceKm = resultat[0] / 1000.0;
                    annonce.put("distanceKm", distanceKm);

                    // ✅ FILTRE : on garde que ceux dans le rayon
                    if (distanceKm <= rayonKm) {
                        annonces.add(annonce);
                    }
                }
            }

            // ✅ Tri : le plus proche de la fac d'abord
            Collections.sort(annonces, (a, b) -> Double.compare(a.optDouble("distanceKm", 999), b.optDouble("distanceKm", 999)));

            afficherMarkers();
            if (!annonces.isEmpty()) afficherAnnonce(annonces.get(0));
            else afficherAucuneAnnonce();

        } catch (Exception e) {
            Log.e(TAG, "Erreur chargement SQLite", e);
            Toast.makeText(this, "Erreur lors du chargement des annonces", Toast.LENGTH_LONG).show();
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void afficherMarkers() {
        mapView.getOverlays().clear();

        // 🔴 1. PIN DE LA FAC - au centre
        Marker markerFac = new Marker(mapView);
        markerFac.setPosition(new GeoPoint(latitudeFac, longitudeFac));
        markerFac.setTitle("🎓 " + nomFac);
        markerFac.setSnippet("Votre faculté");
        markerFac.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        try {
            markerFac.setIcon(getResources().getDrawable(R.drawable.ic_pin_fac, getTheme()));
        } catch (Exception ignored) {
        }
        mapView.getOverlays().add(markerFac);

        // ⭕ 2. CERCLE autour de la fac
        if (rayonKm < 100) {
            Polygon cercle = new Polygon(mapView);
            ArrayList<GeoPoint> points = Polygon.pointsAsCircle(new GeoPoint(latitudeFac, longitudeFac), rayonKm * 1000.0);
            cercle.setPoints(points);
            cercle.setFillColor(0x2200FF00);
            cercle.setStrokeColor(0xFF00AA00);
            cercle.setStrokeWidth(2f);
            mapView.getOverlays().add(cercle);
        }

        // 🟢 3. PINS DES LOGEMENTS PROCHES
        for (JSONObject annonce : annonces) {
            double lat = annonce.optDouble("latitude", 0);
            double lon = annonce.optDouble("longitude", 0);
            if (lat == 0 || lon == 0) continue;

            Marker marker = new Marker(mapView);
            marker.setPosition(new GeoPoint(lat, lon));
            double dist = annonce.optDouble("distanceKm", 0);
            marker.setTitle(annonce.optString("titre", "Appartement"));
            marker.setSnippet(String.format(Locale.getDefault(), "%.0f DT/mois • 📍 %.2f km de %s", annonce.optDouble("prix", 0), dist, nomFac));
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setOnMarkerClickListener((m, map) -> {
                afficherAnnonce(annonce);
                map.getController().animateTo(m.getPosition());
                return true;
            });
            mapView.getOverlays().add(marker);
        }

        mapView.getOverlays().add(new CopyrightOverlay(this));
        mapView.invalidate();
    }

    private void afficherAnnonce(JSONObject annonce) {
        tvTitre.setText(annonce.optString("titre", "Appartement"));
        double prix = annonce.optDouble("prix", 0);
        tvPrix.setText(String.format(Locale.getDefault(), "%.0f DT/mois", prix));
        double distance = annonce.optDouble("distanceKm", 0);
        int minutesPied = (int) (distance / 5.0 * 60); // 5 km/h à pied
        int minutesVoiture = (int) (distance / 30.0 * 60); // 30 km/h en voiture
        tvDistance.setText(String.format(Locale.getDefault(), "📍 %.2f km de %s • 🚶 %d min • 🚗 %d min", distance, nomFac, minutesPied, minutesVoiture));
        tvNote.setText("Quartier : " + annonce.optString("quartier", "") + " • Ville : " + annonce.optString("ville", ""));
        afficherPhoto(annonce.optString("photo", ""));
    }

    private void afficherPhoto(String photoPath) {
        imgAppartement.setImageResource(R.drawable.appart);
        if (photoPath == null || photoPath.trim().isEmpty() || "null".equals(photoPath)) return;
        try {
            File file = new File(photoPath);
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                if (bitmap != null) imgAppartement.setImageBitmap(bitmap);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur affichage photo", e);
        }
    }

    private void afficherAucuneAnnonce() {
        if (rayonKm >= 1000) {
            tvTitre.setText("Aucun logement disponible");
        } else {
            tvTitre.setText("Aucun logement à moins de " + (int) rayonKm + " km de " + nomFac);
        }
        tvPrix.setText("-- DT/mois");
        tvDistance.setText("Essayez d'augmenter le rayon");
        tvNote.setText("");
        imgAppartement.setImageResource(R.drawable.appart);
    }

    private String getString(Cursor cursor, String column) {
        int index = cursor.getColumnIndex(column);
        if (index == -1 || cursor.isNull(index)) return "";
        return cursor.getString(index);
    }

    private int getInt(Cursor cursor, String column, int defaultValue) {
        int index = cursor.getColumnIndex(column);
        if (index == -1 || cursor.isNull(index)) return defaultValue;
        return cursor.getInt(index);
    }

    private double getDouble(Cursor cursor, String column, double defaultValue) {
        int index = cursor.getColumnIndex(column);
        if (index == -1 || cursor.isNull(index)) return defaultValue;
        return cursor.getDouble(index);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    protected void onPause() {
        if (mapView != null) mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mapView != null) mapView.onDetach();
        if (databaseHelper != null) databaseHelper.close();
        super.onDestroy();
    }
}