package com.example.diar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Interface11 extends AppCompatActivity {

    private String emailConnecte;
    private DatabaseHelper db;

    private RecyclerView recyclerView;
    private ProfilAdapter adapter;

    private List<Etudiant> favorisList = new ArrayList<>();

    private LinearLayout tabLogements, tabColocataires;

    private TextView tvTabLogements, tvTabColocataires;

    private View underlineLogements, underlineColocataires;
    private View scrollLogements;
    private View layoutColocsFavoris;
    private View layoutEmptyColocs;

    // Liste dynamique des logements favoris (créée par le code)
    private LinearLayout listeLogements;

    private final int NAVY = Color.parseColor("#1E2A5A");
    private final int GREEN = Color.parseColor("#1B8A5A");
    private final int GREY = Color.parseColor("#64748B");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface11);

        // Navigation étudiant
        NavigationEtudiant.setup(this, 2);

        db = new DatabaseHelper(this);

        // Recuperer email depuis Intent
        emailConnecte = getIntent().getStringExtra("emailEtudiant");

        if (emailConnecte == null || emailConnecte.trim().isEmpty()) {
            emailConnecte = getIntent().getStringExtra("USER_EMAIL");
        }

        // Recuperer email depuis SessionManager
        if (emailConnecte == null || emailConnecte.trim().isEmpty()) {
            SessionManager session = new SessionManager(this);
            emailConnecte = session.getEmail();
        }

        // Recuperer email depuis SharedPreferences
        if (emailConnecte == null || emailConnecte.trim().isEmpty()) {

            SharedPreferences prefs =
                    getSharedPreferences("DIAR_PREFS", MODE_PRIVATE);

            emailConnecte = prefs.getString("email_etudiant", "");

            if (emailConnecte == null || emailConnecte.trim().isEmpty()) {
                emailConnecte = prefs.getString("USER_EMAIL", "");
            }
        }

        if (emailConnecte == null || emailConnecte.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Email étudiant introuvable. Reconnectez-vous.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        emailConnecte = emailConnecte.trim().toLowerCase();

        Log.d("FAVORIS", "Email connecté : " + emailConnecte);

        initialiserVues();

        if (recyclerView == null ||
                tabLogements == null ||
                tabColocataires == null ||
                tvTabLogements == null ||
                tvTabColocataires == null ||
                underlineLogements == null ||
                underlineColocataires == null ||
                scrollLogements == null ||
                layoutColocsFavoris == null ||
                layoutEmptyColocs == null) {

            Log.e("FAVORIS", "Une ou plusieurs vues sont introuvables");

            Toast.makeText(
                    this,
                    "Erreur dans interface11.xml",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        preparerListeLogements();

        recyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        adapter = new ProfilAdapter(
                favorisList,
                emailConnecte,
                etudiant -> ouvrirProfil(etudiant)
        );

        recyclerView.setAdapter(adapter);

        View btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        tabLogements.setOnClickListener(v -> afficherLogements());

        tabColocataires.setOnClickListener(v -> afficherColocataires());

        afficherLogements();
    }

    private void initialiserVues() {

        tabLogements = findViewById(R.id.tabLogements);
        tabColocataires = findViewById(R.id.tabColocataires);

        tvTabLogements = findViewById(R.id.tvTabLogements);
        tvTabColocataires = findViewById(R.id.tvTabColocataires);

        underlineLogements = findViewById(R.id.underlineLogements);
        underlineColocataires = findViewById(R.id.underlineColocataires);

        scrollLogements = findViewById(R.id.scrollLogements);

        layoutColocsFavoris = findViewById(R.id.laylocsoutCoFavoris);

        layoutEmptyColocs = findViewById(R.id.layoutEmptyColocs);

        recyclerView = findViewById(R.id.recyclerViewFavorisColocs);
    }

    private int dp(float valeur) {
        return (int) (valeur * getResources()
                .getDisplayMetrics().density);
    }

    private void afficherLogements() {

        tvTabLogements.setTextColor(Color.parseColor("#00796B"));
        tvTabLogements.setTypeface(null, Typeface.BOLD);

        tvTabColocataires.setTextColor(Color.parseColor("#A0AEC0"));
        tvTabColocataires.setTypeface(null, Typeface.NORMAL);

        underlineLogements.setBackgroundColor(Color.parseColor("#00796B"));
        underlineColocataires.setBackgroundColor(Color.WHITE);

        scrollLogements.setVisibility(View.VISIBLE);
        layoutColocsFavoris.setVisibility(View.GONE);

        chargerLogementsFavoris();
    }

    private void afficherColocataires() {

        tvTabColocataires.setTextColor(Color.parseColor("#00796B"));
        tvTabColocataires.setTypeface(null, Typeface.BOLD);

        tvTabLogements.setTextColor(Color.parseColor("#A0AEC0"));
        tvTabLogements.setTypeface(null, Typeface.NORMAL);

        underlineColocataires.setBackgroundColor(Color.parseColor("#00796B"));
        underlineLogements.setBackgroundColor(Color.WHITE);

        scrollLogements.setVisibility(View.GONE);
        layoutColocsFavoris.setVisibility(View.VISIBLE);

        chargerFavoris();
    }

    // =========================================
    // LOGEMENTS FAVORIS (coeur ❤️)
    // =========================================

    // Remplace le contenu de la zone "Logements" par une liste dynamique
    private void preparerListeLogements() {

        listeLogements = new LinearLayout(this);
        listeLogements.setOrientation(LinearLayout.VERTICAL);
        listeLogements.setPadding(dp(16), dp(12), dp(16), dp(16));

        if (scrollLogements instanceof ViewGroup) {

            ViewGroup zone = (ViewGroup) scrollLogements;

            zone.removeAllViews();

            zone.addView(
                    listeLogements,
                    new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    )
            );
        }
    }

    private void chargerLogementsFavoris() {

        if (listeLogements == null) return;

        listeLogements.removeAllViews();

        Cursor cursor = null;
        int nombre = 0;

        try {

            cursor = db.getReadableDatabase().rawQuery(
                    "SELECT Annonce.* FROM Annonce " +
                            "INNER JOIN Favoris " +
                            "ON Annonce.id = Favoris.annonce_id " +
                            "WHERE LOWER(TRIM(Favoris.emailEtudiant)) = ? " +
                            "ORDER BY Favoris.id DESC",
                    new String[]{emailConnecte}
            );

            while (cursor.moveToNext()) {

                ajouterCarteLogement(
                        lireLong(cursor, "id"),
                        lireTexte(cursor, "titre"),
                        lireTexte(cursor, "ville"),
                        lireTexte(cursor, "quartier"),
                        lireTexte(cursor, "type"),
                        lireDouble(cursor, "prix"),
                        (int) lireLong(cursor, "nbChambres"),
                        lireDouble(cursor, "surface")
                );

                nombre++;
            }

        } catch (Exception e) {

            Log.e("FAVORIS", "Erreur chargement logements favoris", e);

            Toast.makeText(
                    this,
                    "Erreur SQLite : " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }

        if (nombre == 0) {
            afficherAucunLogement();
        }

        Log.d("FAVORIS", "Logements favoris : " + nombre);
    }

    private void afficherAucunLogement() {

        TextView vide = new TextView(this);

        vide.setText(
                "💔\n\nAucun logement favori pour le moment.\n"
                        + "Appuie sur le cœur d'un logement pour l'ajouter ici."
        );

        vide.setTextColor(GREY);
        vide.setTextSize(15);
        vide.setGravity(Gravity.CENTER);
        vide.setPadding(dp(20), dp(70), dp(20), dp(20));

        listeLogements.addView(vide);
    }

    private void ajouterCarteLogement(
            final long annonceId,
            String titre,
            String ville,
            String quartier,
            String type,
            double prix,
            int nbChambres,
            double surface) {

        final LinearLayout carte = new LinearLayout(this);
        carte.setOrientation(LinearLayout.HORIZONTAL);
        carte.setGravity(Gravity.CENTER_VERTICAL);
        carte.setPadding(dp(12), dp(12), dp(12), dp(12));
        carte.setElevation(dp(2));

        GradientDrawable fondCarte = new GradientDrawable();
        fondCarte.setColor(Color.WHITE);
        fondCarte.setCornerRadius(dp(18));
        fondCarte.setStroke(dp(1), Color.parseColor("#E8EDF3"));
        carte.setBackground(fondCarte);

        LinearLayout.LayoutParams carteParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        carteParams.setMargins(dp(2), dp(7), dp(2), dp(7));
        carte.setLayoutParams(carteParams);

        // ----- Photo -----
        ImageView photo = new ImageView(this);
        photo.setScaleType(ImageView.ScaleType.CENTER_CROP);

        GradientDrawable fondPhoto = new GradientDrawable();
        fondPhoto.setColor(Color.parseColor("#DDF3E8"));
        fondPhoto.setCornerRadius(dp(14));

        photo.setBackground(fondPhoto);
        photo.setClipToOutline(true);

        Bitmap bitmap = premierePhoto(annonceId);

        if (bitmap != null) {
            photo.setImageBitmap(bitmap);
        }

        carte.addView(photo, new LinearLayout.LayoutParams(dp(92), dp(92)));

        // ----- Informations -----
        LinearLayout infos = new LinearLayout(this);
        infos.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams infosParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );

        infosParams.setMargins(dp(12), 0, dp(6), 0);

        TextView tvTitre = new TextView(this);
        tvTitre.setText(titre.isEmpty() ? "Logement" : titre);
        tvTitre.setTextSize(16);
        tvTitre.setTextColor(NAVY);
        tvTitre.setTypeface(null, Typeface.BOLD);
        tvTitre.setSingleLine(true);
        tvTitre.setEllipsize(TextUtils.TruncateAt.END);

        infos.addView(tvTitre);

        String lieu = quartier;

        if (!ville.isEmpty()) {
            lieu = lieu.isEmpty() ? ville : lieu + ", " + ville;
        }

        if (!lieu.isEmpty()) {
            TextView tvLieu = new TextView(this);
            tvLieu.setText("📍 " + lieu);
            tvLieu.setTextSize(13);
            tvLieu.setTextColor(GREY);
            tvLieu.setSingleLine(true);
            tvLieu.setEllipsize(TextUtils.TruncateAt.END);
            tvLieu.setPadding(0, dp(3), 0, 0);
            infos.addView(tvLieu);
        }

        StringBuilder details = new StringBuilder();

        if (!type.isEmpty()) {
            details.append(type);
        }

        if (nbChambres > 0) {
            if (details.length() > 0) details.append(" • ");
            details.append(nbChambres).append(nbChambres > 1 ? " chambres" : " chambre");
        }

        if (surface > 0) {
            if (details.length() > 0) details.append(" • ");
            details.append(String.format(Locale.getDefault(), "%.0f m²", surface));
        }

        if (details.length() > 0) {
            TextView tvDetails = new TextView(this);
            tvDetails.setText(details.toString());
            tvDetails.setTextSize(12);
            tvDetails.setTextColor(GREY);
            tvDetails.setSingleLine(true);
            tvDetails.setEllipsize(TextUtils.TruncateAt.END);
            tvDetails.setPadding(0, dp(3), 0, 0);
            infos.addView(tvDetails);
        }

        if (prix > 0) {
            TextView tvPrix = new TextView(this);
            tvPrix.setText(
                    String.format(Locale.getDefault(), "%.0f DT / mois", prix)
            );
            tvPrix.setTextSize(15);
            tvPrix.setTextColor(GREEN);
            tvPrix.setTypeface(null, Typeface.BOLD);
            tvPrix.setPadding(0, dp(6), 0, 0);
            infos.addView(tvPrix);
        }

        carte.addView(infos, infosParams);

        // ----- Coeur : retirer des favoris -----
        TextView coeur = new TextView(this);
        coeur.setText("❤️");
        coeur.setTextSize(22);
        coeur.setGravity(Gravity.CENTER);
        coeur.setPadding(dp(6), dp(6), dp(6), dp(6));

        coeur.setOnClickListener(v -> {

            db.getWritableDatabase().delete(
                    "Favoris",
                    "LOWER(TRIM(emailEtudiant)) = ? AND annonce_id = ?",
                    new String[]{emailConnecte, String.valueOf(annonceId)}
            );

            listeLogements.removeView(carte);

            Toast.makeText(
                    Interface11.this,
                    "Retiré des favoris",
                    Toast.LENGTH_SHORT
            ).show();

            if (listeLogements.getChildCount() == 0) {
                afficherAucunLogement();
            }
        });

        carte.addView(coeur);

        listeLogements.addView(carte);
    }

    // Première photo de l'annonce (réduite pour économiser la mémoire)
    private Bitmap premierePhoto(long annonceId) {

        Cursor c = null;

        try {
            c = db.getPhotosAnnonce(annonceId);

            while (c != null && c.moveToNext()) {

                String chemin = lireTexte(c, "photoPath");

                if (chemin.isEmpty() || !new File(chemin).exists()) {
                    continue;
                }

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4;

                Bitmap bitmap = BitmapFactory.decodeFile(chemin, options);

                if (bitmap != null) {
                    return bitmap;
                }
            }

        } catch (Exception e) {
            Log.e("FAVORIS", "Erreur photo annonce", e);

        } finally {
            if (c != null) {
                c.close();
            }
        }

        return null;
    }

    private String lireTexte(Cursor cursor, String colonne) {

        int index = cursor.getColumnIndex(colonne);

        if (index == -1 || cursor.isNull(index)) {
            return "";
        }

        String valeur = cursor.getString(index);

        return valeur == null ? "" : valeur.trim();
    }

    private long lireLong(Cursor cursor, String colonne) {

        int index = cursor.getColumnIndex(colonne);

        if (index == -1 || cursor.isNull(index)) {
            return 0;
        }

        return cursor.getLong(index);
    }

    private double lireDouble(Cursor cursor, String colonne) {

        int index = cursor.getColumnIndex(colonne);

        if (index == -1 || cursor.isNull(index)) {
            return 0;
        }

        return cursor.getDouble(index);
    }

    // =========================================
    // COLOCATAIRES FAVORIS
    // =========================================

    private void chargerFavoris() {

        favorisList.clear();

        Cursor cursor = null;

        try {

            cursor = db.getFavorisColocataires(emailConnecte);

            if (cursor != null && cursor.moveToFirst()) {

                do {

                    Etudiant e = new Etudiant();

                    int index;

                    index = cursor.getColumnIndex("id");
                    if (index >= 0) {
                        e.setId(cursor.getLong(index));
                    }

                    index = cursor.getColumnIndex("nomComplet");
                    if (index >= 0) {
                        e.setNomComplet(cursor.getString(index));
                    }

                    index = cursor.getColumnIndex("email");
                    if (index >= 0) {
                        e.setEmail(cursor.getString(index));
                    }

                    index = cursor.getColumnIndex("universite");
                    if (index >= 0) {
                        e.setUniversite(cursor.getString(index));
                    }

                    index = cursor.getColumnIndex("age");
                    if (index >= 0) {
                        e.setAge(cursor.getInt(index));
                    }

                    index = cursor.getColumnIndex("budgetMax");
                    if (index >= 0) {
                        e.setBudgetMax(cursor.getDouble(index));
                    }

                    index = cursor.getColumnIndex("quartier");
                    if (index >= 0) {
                        e.setQuartier(cursor.getString(index));
                    }

                    index = cursor.getColumnIndex("modeDeVie");
                    if (index >= 0) {
                        e.setModeDeVie(cursor.getString(index));
                    }

                    index = cursor.getColumnIndex("nbColocataires");
                    if (index >= 0) {
                        e.setNbColocataires(cursor.getString(index));
                    }

                    index = cursor.getColumnIndex("animauxAcceptes");
                    if (index >= 0) {
                        e.setAnimauxAcceptes(cursor.getString(index));
                    }

                    index = cursor.getColumnIndex("proprete");
                    if (index >= 0) {
                        e.setProprete(cursor.getString(index));
                    }

                    index = cursor.getColumnIndex("photoUrl");
                    if (index >= 0) {
                        e.setPhotoUrl(cursor.getString(index));
                    }

                    e.setFavori(true);
                    e.setPourcentage(0);

                    favorisList.add(e);

                } while (cursor.moveToNext());
            }

        } catch (Exception ex) {

            Log.e("FAVORIS", "Erreur chargement favoris", ex);

            Toast.makeText(
                    this,
                    "Erreur SQLite : " + ex.getMessage(),
                    Toast.LENGTH_LONG
            ).show();

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        if (favorisList.isEmpty()) {

            layoutEmptyColocs.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);

        } else {

            layoutEmptyColocs.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        Log.d("FAVORIS", "Nombre de favoris : " + favorisList.size());
    }

    private void ouvrirProfil(Etudiant etudiant) {

        Intent intent = new Intent(
                Interface11.this,
                Interface13.class
        );

        intent.putExtra("USER_EMAIL", emailConnecte);
        intent.putExtra("COLOC_ID", etudiant.getId());

        intent.putExtra("NOM", etudiant.getNomComplet());
        intent.putExtra("POURCENTAGE", etudiant.getPourcentage());

        intent.putExtra("FAC", etudiant.getUniversite());
        intent.putExtra("UNIVERSITE", etudiant.getUniversite());

        intent.putExtra("AGE", etudiant.getAge());
        intent.putExtra("BUDGET", etudiant.getBudgetMax());

        intent.putExtra("VILLE", etudiant.getQuartier());
        intent.putExtra("QUARTIER", etudiant.getQuartier());

        intent.putExtra("MODE", etudiant.getModeDeVie());
        intent.putExtra("MODE_DE_VIE", etudiant.getModeDeVie());

        intent.putExtra("COLOCS", etudiant.getNbColocataires());
        intent.putExtra("ANIMAUX", etudiant.getAnimauxAcceptes());
        intent.putExtra("PROPRETE", etudiant.getProprete());

        intent.putExtra("PHOTO", etudiant.getPhotoUrl());
        intent.putExtra("PHOTO_URL", etudiant.getPhotoUrl());

        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Recharger les logements favoris à chaque retour sur la page
        if (listeLogements != null
                && scrollLogements != null
                && scrollLogements.getVisibility() == View.VISIBLE) {

            chargerLogementsFavoris();
        }

        if (layoutColocsFavoris != null &&
                layoutColocsFavoris.getVisibility() == View.VISIBLE) {

            chargerFavoris();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (db != null) {
            db.close();
        }
    }
}