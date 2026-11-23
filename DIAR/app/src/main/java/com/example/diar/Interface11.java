package com.example.diar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class Interface11 extends AppCompatActivity {

    private String emailConnecte;
    private DatabaseHelper db;
    private Etudiant moi;

    private RecyclerView recyclerView;
    private ProfilAdapter adapter;
    private List<Etudiant> favorisList = new ArrayList<>();

    private LinearLayout tabLogements, tabColocataires;
    private TextView tvTabLogements, tvTabColocataires;
    private View underlineLogements, underlineColocataires;
    private View scrollLogements;
    private View layoutColocsFavoris;
    private View layoutEmptyColocs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface11);

        NavigationEtudiant.setup(this, 2);

        db = new DatabaseHelper(this);

        emailConnecte = getIntent().getStringExtra("emailEtudiant");
        if (emailConnecte == null || emailConnecte.trim().isEmpty()) {
            emailConnecte = getIntent().getStringExtra("USER_EMAIL");
        }
        if (emailConnecte == null || emailConnecte.trim().isEmpty()) {
            SessionManager session = new SessionManager(this);
            emailConnecte = session.getEmail();
        }
        if (emailConnecte == null || emailConnecte.trim().isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("DIAR_PREFS", MODE_PRIVATE);
            emailConnecte = prefs.getString("email_etudiant", "");
            if (emailConnecte == null || emailConnecte.trim().isEmpty()) {
                emailConnecte = prefs.getString("USER_EMAIL", "");
            }
        }
        if (emailConnecte == null || emailConnecte.trim().isEmpty()) {
            Toast.makeText(this, "Email étudiant introuvable. Reconnectez-vous.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        emailConnecte = emailConnecte.trim().toLowerCase();
        Log.d("FAVORIS", "Email connecté : " + emailConnecte);

        initialiserVues();

        if (recyclerView == null || tabLogements == null || tabColocataires == null || tvTabLogements == null || tvTabColocataires == null || underlineLogements == null || underlineColocataires == null || scrollLogements == null || layoutColocsFavoris == null || layoutEmptyColocs == null) {
            Log.e("FAVORIS", "Une ou plusieurs vues sont introuvables");
            Toast.makeText(this, "Erreur dans interface11.xml", Toast.LENGTH_LONG).show();
            return;
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ProfilAdapter(favorisList, emailConnecte, etudiant -> ouvrirProfil(etudiant));
        adapter.setPageFavoris(true);
        recyclerView.setAdapter(adapter);

        moi = chargerMoi();
        adapter.setEtudiantConnecte(moi);

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        tabLogements.setOnClickListener(v -> afficherLogements());
        tabColocataires.setOnClickListener(v -> afficherColocataires());

        afficherLogements();
    }

    private Etudiant chargerMoi() {
        Etudiant m = new Etudiant();
        Cursor c = null;
        try {
            c = db.getReadableDatabase().query("Etudiant", null, "email=?", new String[]{emailConnecte}, null, null, null);
            if (c.moveToFirst()) {
                int i = c.getColumnIndex("quartier");
                if (i >= 0) m.setQuartier(c.getString(i));
                i = c.getColumnIndex("budgetMax");
                if (i >= 0 && !c.isNull(i)) m.setBudgetMax(c.getDouble(i));
                i = c.getColumnIndex("modeDeVie");
                if (i >= 0) m.setModeDeVie(c.getString(i));
                i = c.getColumnIndex("age");
                if (i >= 0 && !c.isNull(i)) m.setAge(c.getInt(i));
                i = c.getColumnIndex("faculte");
                if (i >= 0) m.setFaculte(c.getString(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) c.close();
        }
        return m;
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

    private void afficherLogements() {
        tvTabLogements.setTextColor(Color.parseColor("#00796B"));
        tvTabLogements.setTypeface(null, Typeface.BOLD);
        tvTabColocataires.setTextColor(Color.parseColor("#A0AEC0"));
        tvTabColocataires.setTypeface(null, Typeface.NORMAL);
        underlineLogements.setBackgroundColor(Color.parseColor("#00796B"));
        underlineColocataires.setBackgroundColor(Color.WHITE);
        scrollLogements.setVisibility(View.VISIBLE);
        layoutColocsFavoris.setVisibility(View.GONE);
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
                    if (index >= 0) e.setId(cursor.getLong(index));

                    index = cursor.getColumnIndex("nomComplet");
                    if (index >= 0) e.setNomComplet(cursor.getString(index));

                    index = cursor.getColumnIndex("email");
                    if (index >= 0) e.setEmail(cursor.getString(index));

                    index = cursor.getColumnIndex("universite");
                    if (index >= 0) e.setUniversite(cursor.getString(index));

                    index = cursor.getColumnIndex("age");
                    if (index >= 0) e.setAge(cursor.getInt(index));

                    index = cursor.getColumnIndex("budgetMax");
                    if (index >= 0) e.setBudgetMax(cursor.getDouble(index));

                    index = cursor.getColumnIndex("quartier");
                    if (index >= 0) e.setQuartier(cursor.getString(index));

                    index = cursor.getColumnIndex("modeDeVie");
                    if (index >= 0) e.setModeDeVie(cursor.getString(index));

                    index = cursor.getColumnIndex("faculte");
                    if (index >= 0) e.setFaculte(cursor.getString(index));

                    index = cursor.getColumnIndex("nbColocataires");
                    if (index >= 0) e.setNbColocataires(cursor.getString(index));

                    index = cursor.getColumnIndex("animauxAcceptes");
                    if (index >= 0) e.setAnimauxAcceptes(cursor.getString(index));

                    index = cursor.getColumnIndex("proprete");
                    if (index >= 0) e.setProprete(cursor.getString(index));

                    index = cursor.getColumnIndex("photoUrl");
                    if (index >= 0) e.setPhotoUrl(cursor.getString(index));

                    e.setFavori(true);
                    if (moi != null) {
                        e.setPourcentageCompatible(ProfilAdapter.calculerCompatibilite(moi, e));
                    } else {
                        e.setPourcentageCompatible(0);
                    }

                    favorisList.add(e);
                } while (cursor.moveToNext());
            }
        } catch (Exception ex) {
            Log.e("FAVORIS", "Erreur chargement favoris", ex);
            Toast.makeText(this, "Erreur SQLite : " + ex.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            if (cursor != null) cursor.close();
        }

        if (adapter != null) adapter.notifyDataSetChanged();

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
        Intent intent = new Intent(Interface11.this, Interface13.class);
        intent.putExtra("USER_EMAIL", emailConnecte);
        intent.putExtra("COLOC_ID", etudiant.getId());
        intent.putExtra("NOM", etudiant.getNomComplet());
        intent.putExtra("POURCENTAGE", etudiant.getPourcentageCompatible());
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
        if (layoutColocsFavoris != null && layoutColocsFavoris.getVisibility() == View.VISIBLE) {
            chargerFavoris();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) db.close();
    }
}