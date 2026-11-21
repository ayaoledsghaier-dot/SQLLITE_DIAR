
package com.example.diar;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Interface7 extends AppCompatActivity {

    private static final String TAG = "DIAR_SQLITE";

    private TextView tvBonjour, tvUniversite;
    private ImageView ivUserPhoto;

    private EditText etSearch;
    private RecyclerView rvAnnonces;

    private ImageView ivColoc1, ivColoc2;
    private LinearLayout layoutColocataires;

    private TextView tvSeeAllColoc;
    private TextView tvColoc1Nom, tvColoc1Pct;
    private TextView tvColoc2Nom, tvColoc2Pct;

    private View cardColoc1, cardColoc2;

    private ImageView ivFavoriteAppart;

    private AnnonceAdapter annonceAdapter;
    private DatabaseHelper databaseHelper;

    private String userEmail;

    private JSONObject etuColoc1;
    private JSONObject etuColoc2;

    private int pctColoc1 = 0;
    private int pctColoc2 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface7);

        databaseHelper = new DatabaseHelper(this);

        // Navigation du bas
        NavigationEtudiant.setup(this, 0);

        // Profil étudiant
        tvBonjour = findViewById(R.id.tvBonjour);
        tvUniversite = findViewById(R.id.tvUniversite);
        ivUserPhoto = findViewById(R.id.ivUserPhoto);

        // Recherche et annonces
        etSearch = findViewById(R.id.etSearch);
        rvAnnonces = findViewById(R.id.rvAnnonces);

        // Colocataires
        ivColoc1 = findViewById(R.id.ivColoc1);
        ivColoc2 = findViewById(R.id.ivColoc2);

        layoutColocataires = findViewById(R.id.layoutColocataires);

        tvSeeAllColoc = findViewById(R.id.tvSeeAllColoc);

        tvColoc1Nom = findViewById(R.id.tvColoc1Nom);
        tvColoc1Pct = findViewById(R.id.tvColoc1Pct);

        tvColoc2Nom = findViewById(R.id.tvColoc2Nom);
        tvColoc2Pct = findViewById(R.id.tvColoc2Pct);

        cardColoc1 = findViewById(R.id.cardColoc1);
        cardColoc2 = findViewById(R.id.cardColoc2);

        // Favoris
        ivFavoriteAppart = findViewById(R.id.ivFavoriteAppart);

        // RecyclerView
        rvAnnonces.setLayoutManager(new LinearLayoutManager(this));
        rvAnnonces.setHasFixedSize(false);
        rvAnnonces.setNestedScrollingEnabled(false);

        annonceAdapter = new AnnonceAdapter(this, "");
        rvAnnonces.setAdapter(annonceAdapter);

        // Récupérer l'email de l'étudiant connecté
        SessionManager sessionManager = new SessionManager(this);
        userEmail = sessionManager.getEmail();

        if (userEmail == null || userEmail.trim().isEmpty()) {
            userEmail = getIntent().getStringExtra("USER_EMAIL");
        }

        if (userEmail == null || userEmail.trim().isEmpty()) {
            Toast.makeText(this,
                    "Veuillez vous connecter.",
                    Toast.LENGTH_LONG).show();

            tvBonjour.setText("Bonjour 👋");
            return;
        }

        userEmail = userEmail.trim();

        // Recherche des annonces
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {
                annonceAdapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });

        // Voir tous les colocataires
        if (tvSeeAllColoc != null) {
            tvSeeAllColoc.setOnClickListener(v -> {
                Intent intent = new Intent(
                        Interface7.this,
                        Interface12.class
                );

                intent.putExtra("USER_EMAIL", userEmail);
                startActivity(intent);
            });
        }

        // Premier colocataire
        if (cardColoc1 != null) {
            cardColoc1.setOnClickListener(v -> {
                if (etuColoc1 != null) {
                    ouvrirDetail(etuColoc1, pctColoc1);
                }
            });
        }

        // Deuxième colocataire
        if (cardColoc2 != null) {
            cardColoc2.setOnClickListener(v -> {
                if (etuColoc2 != null) {
                    ouvrirDetail(etuColoc2, pctColoc2);
                }
            });
        }

        // Favoris des logements
        if (ivFavoriteAppart != null) {
            ivFavoriteAppart.setOnClickListener(v -> {
                ouvrirFavoris();
            });
        }

        // Bouton voir toutes les annonces recommandées
        View voirAnnonces = findViewById(R.id.tvSeeAllRecommanded);

        if (voirAnnonces != null) {
            voirAnnonces.setOnClickListener(v -> {
                if (rvAnnonces != null) {
                    rvAnnonces.smoothScrollToPosition(0);
                }
            });
        }
    }

    // =========================================
    // ACTUALISATION DES DONNEES
    // =========================================

    @Override
    protected void onResume() {
        super.onResume();

        if (databaseHelper != null
                && userEmail != null
                && !userEmail.trim().isEmpty()) {

            chargerProfil();
            chargerAnnonces();
            verifierEtAfficherColocs();
        }
    }

    // =========================================
    // PROFIL ETUDIANT SQLITE
    // =========================================

    private void chargerProfil() {

        Cursor cursor = null;

        try {
            cursor = databaseHelper.getEtudiant(userEmail);

            if (cursor != null && cursor.moveToFirst()) {

                String nom = getStringValue(cursor, "nomComplet");
                String universite = getStringValue(cursor, "universite");
                String photoPath = getStringValue(cursor, "photoUrl");

                tvBonjour.setText(
                        "Bonjour " +
                                (nom.isEmpty() ? "Étudiant" : nom) +
                                " 👋"
                );

                tvUniversite.setText(universite);

                afficherPhoto(ivUserPhoto, photoPath);

            } else {
                tvBonjour.setText("Bonjour 👋");
                tvUniversite.setText("");

                Log.e(TAG, "Étudiant introuvable : " + userEmail);
            }

        } catch (Exception e) {
            Log.e(TAG, "Erreur chargement profil", e);

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // =========================================
    // AFFICHER PHOTO LOCALE
    // =========================================

    private void afficherPhoto(ImageView imageView, String photoPath) {

        if (imageView == null) return;

        if (photoPath == null
                || photoPath.trim().isEmpty()
                || photoPath.equals("null")) {
            return;
        }

        try {
            Bitmap bitmap = BitmapFactory.decodeFile(photoPath);

            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                Log.e(TAG, "Photo introuvable : " + photoPath);
            }

        } catch (Exception e) {
            Log.e(TAG, "Erreur affichage photo", e);
        }
    }

    // =========================================
    // CHARGER ANNONCES DEPUIS SQLITE
    // =========================================

    private void chargerAnnonces() {

        Cursor cursor = null;

        ArrayList<JSONObject> annonces = new ArrayList<>();

        try {
            cursor = databaseHelper.getToutesLesAnnonces();

            if (cursor != null) {

                while (cursor.moveToNext()) {

                    JSONObject annonce = new JSONObject();

                    long id = getLongValue(cursor, "id");

                    annonce.put("id", id);
                    annonce.put("titre", getStringValue(cursor, "titre"));
                    annonce.put("description", getStringValue(cursor, "description"));
                    annonce.put("ville", getStringValue(cursor, "ville"));
                    annonce.put("adresse", getStringValue(cursor, "adresse"));
                    annonce.put("quartier", getStringValue(cursor, "quartier"));
                    annonce.put("nomProprietaire",
                            getStringValue(cursor, "nomProprietaire"));
                    annonce.put("type", getStringValue(cursor, "type"));

                    annonce.put("prix", getDoubleValue(cursor, "prix"));
                    annonce.put("nbPersonnes", getIntValue(cursor, "nbPersonnes"));
                    annonce.put("nbChambres", getIntValue(cursor, "nbChambres"));
                    annonce.put("surface", getDoubleValue(cursor, "surface"));

                    annonce.put("latitude", getDoubleValue(cursor, "latitude"));
                    annonce.put("longitude", getDoubleValue(cursor, "longitude"));

                    annonce.put("emailProprietaire",
                            getStringValue(cursor, "emailProprietaire"));

                    // Photos de l'annonce
                    JSONArray photos = new JSONArray();

                    Cursor photosCursor = null;

                    try {
                        photosCursor = databaseHelper.getPhotosAnnonce(id);

                        if (photosCursor != null) {
                            while (photosCursor.moveToNext()) {

                                String path = getStringValue(
                                        photosCursor,
                                        "photoPath"
                                );

                                if (!path.isEmpty()) {
                                    photos.put(path);
                                }
                            }
                        }

                    } finally {
                        if (photosCursor != null) {
                            photosCursor.close();
                        }
                    }

                    annonce.put("photos", photos);

                    annonces.add(annonce);
                }
            }

            annonceAdapter.setAnnonces(annonces);

            Log.d(TAG,
                    "Nombre annonces SQLite = " + annonces.size());

        } catch (Exception e) {
            Log.e(TAG, "Erreur chargement annonces SQLite", e);

            Toast.makeText(this,
                    "Erreur de lecture des annonces.",
                    Toast.LENGTH_SHORT).show();

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // =========================================
    // VERIFIER SI ETUDIANT CHERCHE COLOCATION
    // =========================================

    private void verifierEtAfficherColocs() {

        Cursor cursor = null;

        try {
            cursor = databaseHelper.getEtudiant(userEmail);

            if (cursor != null && cursor.moveToFirst()) {

                String locationType =
                        getStringValue(cursor, "locationType");

                if ("Colocation".equalsIgnoreCase(locationType)) {

                    if (layoutColocataires != null) {
                        layoutColocataires.setVisibility(View.VISIBLE);
                    }

                    charger2PremiersColocs();

                } else {
                    if (layoutColocataires != null) {
                        layoutColocataires.setVisibility(View.GONE);
                    }
                }

            } else {
                if (layoutColocataires != null) {
                    layoutColocataires.setVisibility(View.GONE);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Erreur vérification colocation", e);

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // =========================================
    // CHARGER LES COLOCATAIRES DEPUIS SQLITE
    // =========================================

    private void charger2PremiersColocs() {

        Cursor cursor = null;

        ArrayList<JSONObject> candidats = new ArrayList<>();

        Cursor profilConnecte = null;

        try {
            profilConnecte = databaseHelper.getEtudiant(userEmail);

            if (profilConnecte == null
                    || !profilConnecte.moveToFirst()) {
                return;
            }

            JSONObject profil = cursorToJson(profilConnecte);

            SQLiteDatabase db = databaseHelper.getReadableDatabase();

            cursor = db.rawQuery(
                    "SELECT * FROM Etudiant WHERE email != ?",
                    new String[]{userEmail}
            );

            while (cursor.moveToNext()) {

                JSONObject candidat = cursorToJson(cursor);

                String type = candidat.optString("locationType", "");

                if (!"Colocation".equalsIgnoreCase(type)) {
                    continue;
                }

                int score = calculerCompatibilite(profil, candidat);

                candidat.put("pourcentageCompatibilite", score);

                candidats.add(candidat);
            }

            // Trier du plus compatible au moins compatible
            Collections.sort(candidats, new Comparator<JSONObject>() {
                @Override
                public int compare(JSONObject a, JSONObject b) {
                    return Integer.compare(
                            b.optInt("pourcentageCompatibilite", 0),
                            a.optInt("pourcentageCompatibilite", 0)
                    );
                }
            });

            afficherDeuxColocs(candidats);

        } catch (Exception e) {
            Log.e(TAG, "Erreur chargement colocataires SQLite", e);

        } finally {
            if (cursor != null) {
                cursor.close();
            }

            if (profilConnecte != null) {
                profilConnecte.close();
            }
        }
    }

    // =========================================
    // CONVERTIR CURSOR EN JSON
    // =========================================

    private JSONObject cursorToJson(Cursor cursor) throws Exception {

        JSONObject obj = new JSONObject();

        obj.put("id", getLongValue(cursor, "id"));
        obj.put("nomComplet", getStringValue(cursor, "nomComplet"));
        obj.put("email", getStringValue(cursor, "email"));
        obj.put("universite", getStringValue(cursor, "universite"));
        obj.put("ville", getStringValue(cursor, "ville"));
        obj.put("quartier", getStringValue(cursor, "quartier"));
        obj.put("locationType", getStringValue(cursor, "locationType"));
        obj.put("typeLogement", getStringValue(cursor, "typeLogement"));

        obj.put("budgetMax", getDoubleValue(cursor, "budgetMax"));

        obj.put("modeDeVie", getStringValue(cursor, "modeDeVie"));
        obj.put("nbColocataires", getStringValue(cursor, "nbColocataires"));
        obj.put("animauxAcceptes", getStringValue(cursor, "animauxAcceptes"));
        obj.put("proprete", getStringValue(cursor, "proprete"));

        obj.put("photoUrl", getStringValue(cursor, "photoUrl"));

        // Age n'existe pas forcément dans les anciennes bases
        obj.put("age", getIntValue(cursor, "age"));

        return obj;
    }

    // =========================================
    // CALCUL COMPATIBILITE LOCALE
    // =========================================

    private int calculerCompatibilite(
            JSONObject a,
            JSONObject b) {

        int score = 0;
        int total = 0;

        // Même université : 20 points
        score += comparerTexte(
                a.optString("universite"),
                b.optString("universite"),
                20
        );
        total += 20;

        // Même ville : 20 points
        score += comparerTexte(
                a.optString("ville"),
                b.optString("ville"),
                20
        );
        total += 20;

        // Même quartier : 15 points
        score += comparerTexte(
                a.optString("quartier"),
                b.optString("quartier"),
                15
        );
        total += 15;

        // Même mode de vie : 15 points
        score += comparerTexte(
                a.optString("modeDeVie"),
                b.optString("modeDeVie"),
                15
        );
        total += 15;

        // Même préférence de propreté : 10 points
        score += comparerTexte(
                a.optString("proprete"),
                b.optString("proprete"),
                10
        );
        total += 10;

        // Même préférence concernant les animaux : 10 points
        score += comparerTexte(
                a.optString("animauxAcceptes"),
                b.optString("animauxAcceptes"),
                10
        );
        total += 10;

        // Budgets proches : 10 points
        double budgetA = a.optDouble("budgetMax", 0);
        double budgetB = b.optDouble("budgetMax", 0);

        if (budgetA > 0 && budgetB > 0) {

            double difference =
                    Math.abs(budgetA - budgetB);

            double moyenne =
                    (budgetA + budgetB) / 2.0;

            if (moyenne > 0) {
                double ecartPourcentage =
                        (difference / moyenne) * 100;

                if (ecartPourcentage <= 10) {
                    score += 10;
                } else if (ecartPourcentage <= 25) {
                    score += 7;
                } else if (ecartPourcentage <= 50) {
                    score += 4;
                }
            }
        }

        total += 10;

        if (total == 0) return 0;

        return Math.round((score * 100f) / total);
    }

    private int comparerTexte(
            String valeurA,
            String valeurB,
            int points) {

        if (valeurA == null || valeurB == null
                || valeurA.trim().isEmpty()
                || valeurB.trim().isEmpty()) {
            return 0;
        }

        return valeurA.trim().equalsIgnoreCase(valeurB.trim())
                ? points : 0;
    }

    // =========================================
    // AFFICHER LES DEUX MEILLEURS COLOCATAIRES
    // =========================================

    private void afficherDeuxColocs(ArrayList<JSONObject> candidats) {

        etuColoc1 = null;
        etuColoc2 = null;

        if (candidats.isEmpty()) {
            if (layoutColocataires != null) {
                layoutColocataires.setVisibility(View.GONE);
            }
            return;
        }

        if (layoutColocataires != null) {
            layoutColocataires.setVisibility(View.VISIBLE);
        }

        // Premier candidat
        etuColoc1 = candidats.get(0);

        pctColoc1 = etuColoc1.optInt(
                "pourcentageCompatibilite", 0
        );

        String nom1 = etuColoc1.optString(
                "nomComplet", "Étudiant"
        );

        tvColoc1Nom.setText(premierNom(nom1));
        tvColoc1Pct.setText(pctColoc1 + "%");

        afficherPhoto(
                ivColoc1,
                etuColoc1.optString("photoUrl", "")
        );

        if (cardColoc1 != null) {
            cardColoc1.setVisibility(View.VISIBLE);
        }

        // Deuxième candidat
        if (candidats.size() > 1) {

            etuColoc2 = candidats.get(1);

            pctColoc2 = etuColoc2.optInt(
                    "pourcentageCompatibilite", 0
            );

            String nom2 = etuColoc2.optString(
                    "nomComplet", "Étudiant"
            );

            tvColoc2Nom.setText(premierNom(nom2));
            tvColoc2Pct.setText(pctColoc2 + "%");

            afficherPhoto(
                    ivColoc2,
                    etuColoc2.optString("photoUrl", "")
            );

            if (cardColoc2 != null) {
                cardColoc2.setVisibility(View.VISIBLE);
            }

        } else {
            etuColoc2 = null;

            if (cardColoc2 != null) {
                cardColoc2.setVisibility(View.GONE);
            }
        }
    }

    private String premierNom(String nomComplet) {

        if (nomComplet == null || nomComplet.trim().isEmpty()) {
            return "Étudiant";
        }

        String[] parties = nomComplet.trim().split("\\s+");

        return parties[0];
    }

    // =========================================
    // OUVRIR DETAIL COLOCATAIRE
    // =========================================

    private void ouvrirDetail(JSONObject etu, int pourcentage) {

        if (etu == null) return;

        Intent intent = new Intent(
                Interface7.this,
                Interface13.class
        );

        intent.putExtra("USER_EMAIL", userEmail);
        intent.putExtra("COLOC_ID", etu.optInt("id", -1));

        intent.putExtra(
                "NOM",
                etu.optString("nomComplet", "")
        );

        intent.putExtra("POURCENTAGE", (double) pourcentage);

        intent.putExtra(
                "FAC",
                etu.optString("universite", "")
        );

        intent.putExtra("AGE", etu.optInt("age", 0));

        intent.putExtra(
                "BUDGET",
                etu.optDouble("budgetMax", 0)
        );

        String ville = etu.optString("ville", "");

        if (ville.isEmpty()) {
            ville = etu.optString("quartier", "");
        }

        intent.putExtra("VILLE", ville);

        intent.putExtra(
                "MODE",
                etu.optString("modeDeVie", "")
        );

        intent.putExtra(
                "COLOCS",
                etu.optString("nbColocataires", "")
        );

        intent.putExtra(
                "ANIMAUX",
                etu.optString("animauxAcceptes", "")
        );

        intent.putExtra(
                "PROPRETE",
                etu.optString("proprete", "")
        );

        intent.putExtra(
                "PHOTO",
                etu.optString("photoUrl", "")
        );

        startActivity(intent);
    }

    // =========================================
    // OUVRIR FAVORIS
    // =========================================

    private void ouvrirFavoris() {

        Intent intent = new Intent(
                Interface7.this,
                Interface11.class
        );

        intent.putExtra("USER_EMAIL", userEmail);

        startActivity(intent);
    }

    // =========================================
    // OUTILS POUR LIRE LES COLONNES SQLITE
    // =========================================

    private String getStringValue(
            Cursor cursor,
            String column) {

        int index = cursor.getColumnIndex(column);

        if (index == -1 || cursor.isNull(index)) {
            return "";
        }

        String value = cursor.getString(index);

        return value == null ? "" : value;
    }

    private int getIntValue(
            Cursor cursor,
            String column) {

        int index = cursor.getColumnIndex(column);

        if (index == -1 || cursor.isNull(index)) {
            return 0;
        }

        return cursor.getInt(index);
    }

    private long getLongValue(
            Cursor cursor,
            String column) {

        int index = cursor.getColumnIndex(column);

        if (index == -1 || cursor.isNull(index)) {
            return 0;
        }

        return cursor.getLong(index);
    }

    private double getDoubleValue(
            Cursor cursor,
            String column) {

        int index = cursor.getColumnIndex(column);

        if (index == -1 || cursor.isNull(index)) {
            return 0;
        }

        return cursor.getDouble(index);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}
