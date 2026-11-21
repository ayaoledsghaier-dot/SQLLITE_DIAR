
package com.example.diar;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.CopyrightOverlay;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Interface9 extends AppCompatActivity {

    private static final String TAG = "DIAR_INTERFACE9";
    private static final int LOCATION_PERMISSION_CODE = 100;

    private MapView mapView;
    private ImageView imgAppartement;
    private TextView tvTitre, tvPrix, tvDistance, tvNote;
    private ImageButton btnBack;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private DatabaseHelper databaseHelper;

    private final List<JSONObject> annonces = new ArrayList<>();

    private double latitudeEtudiant = 36.8065;
    private double longitudeEtudiant = 10.1815;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configuration osmdroid
        Configuration.getInstance().load(
                getApplicationContext(),
                getSharedPreferences("osmdroid", MODE_PRIVATE)
        );

        // Identification de l'application DIAR
        Configuration.getInstance().setUserAgentValue(
                "DIAR/1.0 (Android; com.example.diar)"
        );

        setContentView(R.layout.interface9);

        NavigationEtudiant.setup(this, 1);

        mapView = findViewById(R.id.imageView5);
        imgAppartement = findViewById(R.id.imageView13);

        tvTitre = findViewById(R.id.textView13);
        tvPrix = findViewById(R.id.textView14);
        tvDistance = findViewById(R.id.textView15);
        tvNote = findViewById(R.id.textView16);

        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        databaseHelper = new DatabaseHelper(this);

        // Configuration de la carte
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setUseDataConnection(true);

        mapView.getController().setZoom(13.0);

        GeoPoint tunis = new GeoPoint(36.8065, 10.1815);
        mapView.getController().setCenter(tunis);

        locationManager = (LocationManager)
                getSystemService(LOCATION_SERVICE);

        // Charger les annonces depuis SQLite
        chargerAnnoncesDepuisSQLite();

        // Vérifier permission GPS
        verifierPermissionGPS();
    }

    private boolean permissionGPSAccordee() {

        return ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

                || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void verifierPermissionGPS() {

        if (!permissionGPSAccordee()) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_CODE
            );

            return;
        }

        obtenirPositionGPS();
    }

    private void obtenirPositionGPS() {

        if (!permissionGPSAccordee()) return;

        try {

            Location position = null;

            if (locationManager.isProviderEnabled(
                    LocationManager.GPS_PROVIDER)) {

                position = locationManager.getLastKnownLocation(
                        LocationManager.GPS_PROVIDER
                );
            }

            if (position == null &&
                    locationManager.isProviderEnabled(
                            LocationManager.NETWORK_PROVIDER)) {

                position = locationManager.getLastKnownLocation(
                        LocationManager.NETWORK_PROVIDER
                );
            }

            if (position != null) {

                mettreAJourPosition(position);
                return;
            }

            Toast.makeText(
                    this,
                    "Recherche de votre position...",
                    Toast.LENGTH_SHORT
            ).show();

            locationListener = new LocationListener() {

                @Override
                public void onLocationChanged(
                        @NonNull Location location) {

                    mettreAJourPosition(location);
                    arreterGPS();
                }
            };

            if (locationManager.isProviderEnabled(
                    LocationManager.NETWORK_PROVIDER)) {

                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        1000,
                        5,
                        locationListener
                );

            } else if (locationManager.isProviderEnabled(
                    LocationManager.GPS_PROVIDER)) {

                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        1000,
                        5,
                        locationListener
                );

            } else {

                Toast.makeText(
                        this,
                        "Activez le GPS de votre téléphone",
                        Toast.LENGTH_LONG
                ).show();
            }

        } catch (SecurityException e) {

            Log.e(TAG, "Erreur permission GPS", e);
        }
    }

    private void mettreAJourPosition(Location position) {

        latitudeEtudiant = position.getLatitude();
        longitudeEtudiant = position.getLongitude();

        GeoPoint point = new GeoPoint(
                latitudeEtudiant,
                longitudeEtudiant
        );

        mapView.getController().animateTo(point);
        mapView.getController().setZoom(14.0);

        chargerAnnoncesDepuisSQLite();
    }

    private void chargerAnnoncesDepuisSQLite() {

        annonces.clear();

        Cursor cursor = null;

        try {

            cursor = databaseHelper.getReadableDatabase().query(
                    "Annonce",
                    null,
                    "disponible = ?",
                    new String[]{"1"},
                    null,
                    null,
                    "id DESC"
            );

            while (cursor.moveToNext()) {

                JSONObject annonce = new JSONObject();

                int id = getInt(cursor, "id", -1);

                double latitude = getDouble(
                        cursor, "latitude", 0
                );

                double longitude = getDouble(
                        cursor, "longitude", 0
                );

                annonce.put("id", id);
                annonce.put("titre", getString(cursor, "titre"));
                annonce.put("description", getString(cursor, "description"));
                annonce.put("ville", getString(cursor, "ville"));
                annonce.put("quartier", getString(cursor, "quartier"));
                annonce.put("prix", getDouble(cursor, "prix", 0));
                annonce.put("latitude", latitude);
                annonce.put("longitude", longitude);

                // Récupérer la première photo
                Cursor photos = databaseHelper.getReadableDatabase().query(
                        "AnnoncePhoto",
                        new String[]{"photoPath"},
                        "annonce_id = ?",
                        new String[]{String.valueOf(id)},
                        null,
                        null,
                        "id ASC"
                );

                String photoPath = "";

                try {

                    if (photos.moveToFirst()) {

                        photoPath = photos.getString(
                                photos.getColumnIndexOrThrow("photoPath")
                        );
                    }

                } finally {

                    photos.close();
                }

                annonce.put("photo", photoPath);

                // Calcul distance
                if (latitude != 0 && longitude != 0) {

                    float[] resultat = new float[1];

                    Location.distanceBetween(
                            latitudeEtudiant,
                            longitudeEtudiant,
                            latitude,
                            longitude,
                            resultat
                    );

                    double distanceKm = resultat[0] / 1000.0;

                    annonce.put("distanceKm", distanceKm);

                } else {

                    annonce.put("distanceKm", 0);
                }

                annonces.add(annonce);
            }

            afficherMarkers();

            if (!annonces.isEmpty()) {

                afficherAnnonce(annonces.get(0));

            } else {

                afficherAucuneAnnonce();
            }

        } catch (Exception e) {

            Log.e(TAG, "Erreur chargement SQLite", e);

            Toast.makeText(
                    this,
                    "Erreur lors du chargement des annonces",
                    Toast.LENGTH_LONG
            ).show();

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void afficherMarkers() {

        mapView.getOverlays().clear();

        // Position étudiant
        Marker markerEtudiant = new Marker(mapView);

        markerEtudiant.setPosition(
                new GeoPoint(
                        latitudeEtudiant,
                        longitudeEtudiant
                )
        );

        markerEtudiant.setTitle("Ma position");

        markerEtudiant.setAnchor(
                Marker.ANCHOR_CENTER,
                Marker.ANCHOR_BOTTOM
        );

        mapView.getOverlays().add(markerEtudiant);

        // Annonces
        for (JSONObject annonce : annonces) {

            double lat = annonce.optDouble("latitude", 0);
            double lon = annonce.optDouble("longitude", 0);

            if (lat == 0 || lon == 0) continue;

            Marker marker = new Marker(mapView);

            marker.setPosition(new GeoPoint(lat, lon));

            marker.setTitle(
                    annonce.optString("titre", "Appartement")
            );

            marker.setSnippet(
                    annonce.optString("prix", "0") + " DT/mois"
            );

            marker.setAnchor(
                    Marker.ANCHOR_CENTER,
                    Marker.ANCHOR_BOTTOM
            );

            marker.setOnMarkerClickListener((m, map) -> {

                afficherAnnonce(annonce);

                map.getController().animateTo(
                        m.getPosition()
                );

                return true;
            });

            mapView.getOverlays().add(marker);
        }

        // Copyright OpenStreetMap
        mapView.getOverlays().add(
                new CopyrightOverlay(this)
        );

        mapView.invalidate();
    }

    private void afficherAnnonce(JSONObject annonce) {

        tvTitre.setText(
                annonce.optString("titre", "Appartement")
        );

        double prix = annonce.optDouble("prix", 0);

        tvPrix.setText(
                String.format(
                        Locale.getDefault(),
                        "%.0f DT/mois",
                        prix
                )
        );

        double distance = annonce.optDouble("distanceKm", 0);

        tvDistance.setText(
                String.format(
                        Locale.getDefault(),
                        "📍 %.2f km",
                        distance
                )
        );

        tvNote.setText("");

        afficherPhoto(
                annonce.optString("photo", "")
        );
    }

    private void afficherPhoto(String photoPath) {

        imgAppartement.setImageResource(R.drawable.appart);

        if (photoPath == null ||
                photoPath.trim().isEmpty() ||
                "null".equals(photoPath)) {

            return;
        }

        try {

            File file = new File(photoPath);

            if (file.exists()) {

                Bitmap bitmap = BitmapFactory.decodeFile(
                        file.getAbsolutePath()
                );

                if (bitmap != null) {
                    imgAppartement.setImageBitmap(bitmap);
                }
            }

        } catch (Exception e) {

            Log.e(TAG, "Erreur affichage photo", e);
        }
    }

    private void afficherAucuneAnnonce() {

        tvTitre.setText("Aucune annonce disponible");
        tvPrix.setText("-- DT/mois");
        tvDistance.setText("Aucune annonce proche");
        tvNote.setText("");

        imgAppartement.setImageResource(R.drawable.appart);
    }

    private String getString(Cursor cursor, String column) {

        int index = cursor.getColumnIndex(column);

        if (index == -1 || cursor.isNull(index)) {
            return "";
        }

        return cursor.getString(index);
    }

    private int getInt(
            Cursor cursor,
            String column,
            int defaultValue) {

        int index = cursor.getColumnIndex(column);

        if (index == -1 || cursor.isNull(index)) {
            return defaultValue;
        }

        return cursor.getInt(index);
    }

    private double getDouble(
            Cursor cursor,
            String column,
            double defaultValue) {

        int index = cursor.getColumnIndex(column);

        if (index == -1 || cursor.isNull(index)) {
            return defaultValue;
        }

        return cursor.getDouble(index);
    }

    private void arreterGPS() {

        if (locationManager != null &&
                locationListener != null) {

            try {

                locationManager.removeUpdates(locationListener);

            } catch (SecurityException e) {

                Log.e(TAG, "Impossible d'arrêter GPS", e);
            }

            locationListener = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == LOCATION_PERMISSION_CODE) {

            boolean autorise = false;

            for (int result : grantResults) {

                if (result == PackageManager.PERMISSION_GRANTED) {

                    autorise = true;
                    break;
                }
            }

            if (autorise) {

                obtenirPositionGPS();

            } else {

                Toast.makeText(
                        this,
                        "Autorisez la localisation pour voir votre position",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    @Override
    protected void onResume() {

        super.onResume();

        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    protected void onPause() {

        arreterGPS();

        if (mapView != null) {
            mapView.onPause();
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {

        arreterGPS();

        if (mapView != null) {
            mapView.onDetach();
        }

        if (databaseHelper != null) {
            databaseHelper.close();
        }

        super.onDestroy();
    }
}
