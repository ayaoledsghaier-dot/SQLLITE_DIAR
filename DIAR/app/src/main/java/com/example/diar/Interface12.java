package com.example.diar;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Interface12 extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProfilAdapter adapter;
    private List<Etudiant> list = new ArrayList<>();
    private DatabaseHelper db;
    private String emailConnecte = "";
    private Etudiant moi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface12);

        NavigationEtudiant.setup(this, -1);

        db = new DatabaseHelper(this);
        emailConnecte = getIntent().getStringExtra("USER_EMAIL");
        if (emailConnecte == null || emailConnecte.isEmpty()) {
            emailConnecte = getIntent().getStringExtra("emailEtudiant");
        }
        if (emailConnecte == null || emailConnecte.isEmpty()) {
            emailConnecte = new SessionManager(this).getEmail();
        }
        if (emailConnecte == null) emailConnecte = "";
        emailConnecte = emailConnecte.trim().toLowerCase();

        recyclerView = findViewById(R.id.recyclerViewProfils);
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProfilAdapter(list, emailConnecte, e -> ouvrirProfil(e));
        recyclerView.setAdapter(adapter);

        moi = chargerMoi();
        adapter.setEtudiantConnecte(moi);
        chargerTous();
    }

    private void ouvrirProfil(Etudiant etudiant) {
        Intent intent = new Intent(Interface12.this, Interface13.class);
        intent.putExtra("USER_EMAIL", emailConnecte);
        intent.putExtra("emailEtudiant", emailConnecte);
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

    private Etudiant chargerMoi() {
        Etudiant m = new Etudiant();
        Cursor c = null;
        try {
            c = db.getReadableDatabase().query("Etudiant", null, "email=?", new String[]{emailConnecte}, null, null, null);
            if (c.moveToFirst()) {
                m.setQuartier(getString(c, "quartier"));
                m.setBudgetMax(getDouble(c, "budgetMax", 0));
                m.setModeDeVie(getString(c, "modeDeVie"));
                m.setAge(getInt(c, "age", 21));
                m.setFaculte(getString(c, "faculte"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        finally { if (c != null) c.close(); }
        return m;
    }

    private void chargerTous() {
        list.clear();
        Cursor c = null;
        try {
            c = db.getReadableDatabase().query("Etudiant", null, null, null, null, null, "id DESC");
            while (c.moveToNext()) {
                String email = getString(c, "email");
                if (email.equalsIgnoreCase(emailConnecte)) continue;
                Etudiant e = new Etudiant(getString(c, "nomComplet"), email, getString(c, "universite"), getInt(c, "age", 21), getDouble(c, "budgetMax", 0), getString(c, "modeDeVie"), getString(c, "nbColocataires"), getString(c, "proprete"), getString(c, "animauxAcceptes"));
                e.setId(getLong(c, "id", 0));
                e.setQuartier(getString(c, "quartier"));
                e.setFaculte(getString(c, "faculte"));
                e.setPhotoUrl(getString(c, "photoUrl"));
                e.setPourcentageCompatible(ProfilAdapter.calculerCompatibilite(moi, e));
                list.add(e);
            }
            Collections.sort(list, new Comparator<Etudiant>() {
                @Override public int compare(Etudiant a, Etudiant b) {
                    return Double.compare(b.getPourcentageCompatible(), a.getPourcentageCompatible());
                }
            });
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally { if (c != null) c.close(); }
    }

    private String getString(Cursor c, String col) {
        int i = c.getColumnIndex(col);
        if (i == -1 || c.isNull(i)) return "";
        return c.getString(i);
    }
    private int getInt(Cursor c, String col, int def) {
        int i = c.getColumnIndex(col);
        if (i == -1 || c.isNull(i)) return def;
        return c.getInt(i);
    }
    private long getLong(Cursor c, String col, long def) {
        int i = c.getColumnIndex(col);
        if (i == -1 || c.isNull(i)) return def;
        return c.getLong(i);
    }
    private double getDouble(Cursor c, String col, double def) {
        int i = c.getColumnIndex(col);
        if (i == -1 || c.isNull(i)) return def;
        return c.getDouble(i);
    }
    @Override protected void onDestroy() {
        if (db != null) db.close();
        super.onDestroy();
    }
}