package com.example.diar;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Interface7 extends AppCompatActivity {

    // --- IDs qui existent dans ton XML interface7.xml ---
    private TextView tvBonjour, tvUniversite;
    private ImageView ivUserPhoto;

    // Notifications
    private View btnNotifications;
    private TextView tvNotificationCount;

    private EditText etSearch;
    private TextView tvSeeAllRecommanded;
    private RecyclerView rvAnnonces;
    private LinearLayout layoutColocataires;
    private TextView tvSeeAllColoc;
    private View cardColoc1, cardColoc2;
    private ImageView ivColoc1, ivColoc2;
    private TextView tvColoc1Nom, tvColoc1Pct, tvColoc2Nom, tvColoc2Pct;

    private DatabaseHelper databaseHelper;
    private String emailConnecte = "";
    private Etudiant moi;

    private List<AnnonceSimple> annonceList = new ArrayList<>();
    private List<AnnonceSimple> annonceListFiltree = new ArrayList<>();
    private AnnonceSimpleAdapter annonceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface7);
        NavigationEtudiant.setup(this, 0);

        databaseHelper = new DatabaseHelper(this);

        // --- Email connecté ---
        emailConnecte = getIntent().getStringExtra("USER_EMAIL");
        if (emailConnecte == null || emailConnecte.isEmpty()) {
            emailConnecte = getIntent().getStringExtra("emailEtudiant");
        }
        if (emailConnecte == null || emailConnecte.isEmpty()) {
            emailConnecte = new SessionManager(this).getEmail();
        }
        if (emailConnecte == null) emailConnecte = "";
        emailConnecte = emailConnecte.trim().toLowerCase();

        // --- findViewById = exactement les IDs de ton XML ---
        tvBonjour = findViewById(R.id.tvBonjour);
        tvUniversite = findViewById(R.id.tvUniversite);
        ivUserPhoto = findViewById(R.id.ivUserPhoto);

        // Notifications
        btnNotifications = findViewById(R.id.btnNotifications);
        tvNotificationCount = findViewById(R.id.tvNotificationCount);

        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v -> {
                Intent intent = new Intent(Interface7.this, NotificationsActivity.class);
                intent.putExtra("USER_EMAIL", emailConnecte);
                startActivity(intent);
            });
        }

        // Recherche et annonces
        etSearch = findViewById(R.id.etSearch);
        tvSeeAllRecommanded = findViewById(R.id.tvSeeAllRecommanded);
        rvAnnonces = findViewById(R.id.rvAnnonces);
        layoutColocataires = findViewById(R.id.layoutColocataires);
        tvSeeAllColoc = findViewById(R.id.tvSeeAllColoc);
        cardColoc1 = findViewById(R.id.cardColoc1);
        cardColoc2 = findViewById(R.id.cardColoc2);
        ivColoc1 = findViewById(R.id.ivColoc1);
        ivColoc2 = findViewById(R.id.ivColoc2);
        tvColoc1Nom = findViewById(R.id.tvColoc1Nom);
        tvColoc1Pct = findViewById(R.id.tvColoc1Pct);
        tvColoc2Nom = findViewById(R.id.tvColoc2Nom);
        tvColoc2Pct = findViewById(R.id.tvColoc2Pct);

        // --- RecyclerView Annonces ---
        annonceAdapter = new AnnonceSimpleAdapter(annonceListFiltree);
        rvAnnonces.setLayoutManager(new LinearLayoutManager(this));
        rvAnnonces.setNestedScrollingEnabled(false);
        rvAnnonces.setAdapter(annonceAdapter);

        // --- Voir tout annonces -> Interface12 ---
        tvSeeAllRecommanded.setOnClickListener(v -> {
            Intent i = new Intent(Interface7.this, Interface12.class);
            i.putExtra("USER_EMAIL", emailConnecte);
            startActivity(i);
        });

        // --- Voir tout colocs -> Interface12 ---
        tvSeeAllColoc.setOnClickListener(v -> {
            Intent i = new Intent(Interface7.this, Interface12.class);
            i.putExtra("USER_EMAIL", emailConnecte);
            startActivity(i);
        });

        // --- Recherche ---
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filtrerAnnonces(s.toString());
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        moi = chargerMoi();
        chargerAnnonces();
        chargerTop2Colocs();
    }

    // =========================================
    // ACTUALISATION DES DONNEES
    // =========================================

    @Override
    protected void onResume() {
        super.onResume();

        if (databaseHelper != null && !emailConnecte.isEmpty()) {
            moi = chargerMoi();
            mettreAJourBadgeNotifications();
            chargerAnnonces();
            if (etSearch != null) {
                filtrerAnnonces(etSearch.getText().toString());
            }
            chargerTop2Colocs();
        }
    }

    private void filtrerAnnonces(String query) {
        annonceListFiltree.clear();
        if (query == null || query.trim().isEmpty()) {
            annonceListFiltree.addAll(annonceList);
        } else {
            String q = query.toLowerCase().trim();
            for (AnnonceSimple a : annonceList) {
                if (a.titre.toLowerCase().contains(q)
                        || a.quartier.toLowerCase().contains(q)
                        || a.ville.toLowerCase().contains(q)
                        || a.description.toLowerCase().contains(q)) {
                    annonceListFiltree.add(a);
                }
            }
        }
        annonceAdapter.notifyDataSetChanged();
    }

    // =========================================
    // BADGE NOTIFICATIONS
    // =========================================

    private void mettreAJourBadgeNotifications() {

        if (tvNotificationCount == null) return;

        int nombre = databaseHelper.compterNotificationsNonLues(emailConnecte);

        if (nombre > 0) {
            tvNotificationCount.setText(nombre > 9 ? "9+" : String.valueOf(nombre));
            tvNotificationCount.setVisibility(View.VISIBLE);
        } else {
            tvNotificationCount.setVisibility(View.GONE);
        }
    }

    // =========================================
    // PROFIL ETUDIANT SQLITE
    // =========================================

    private Etudiant chargerMoi() {
        Etudiant m = new Etudiant();
        Cursor c = null;
        try {
            c = databaseHelper.getReadableDatabase().query("Etudiant", null, "email=?", new String[]{emailConnecte}, null, null, null);
            if (c.moveToFirst()) {
                m.setQuartier(getString(c, "quartier"));
                m.setBudgetMax(getDouble(c, "budgetMax", 0));
                m.setModeDeVie(getString(c, "modeDeVie"));
                m.setAge(getInt(c, "age", 21));
                m.setFaculte(getString(c, "faculte"));
                String nom = getString(c, "nomComplet");
                if (!nom.isEmpty()) tvBonjour.setText("Bonjour " + nom);
                String uni = getString(c, "universite");
                if (!uni.isEmpty()) tvUniversite.setText(uni);
                String photo = getString(c, "photoUrl");
                if (!photo.isEmpty()) Glide.with(this).load(photo).into(ivUserPhoto);
            }
        } catch (Exception e) { e.printStackTrace(); }
        finally { if (c != null) c.close(); }
        return m;
    }

    private void chargerAnnonces() {
        annonceList.clear();
        annonceListFiltree.clear();
        Cursor c = null;
        try {
            c = databaseHelper.getToutesLesAnnonces();
            while (c.moveToNext()) {
                AnnonceSimple a = new AnnonceSimple();
                a.id = getLong(c, "id", 0);
                a.titre = getString(c, "titre");
                a.description = getString(c, "description");
                a.quartier = getString(c, "quartier");
                a.ville = getString(c, "ville");
                a.prix = getDouble(c, "prix", 0);
                a.photo = getPremierePhoto(a.id);
                annonceList.add(a);
            }
            annonceListFiltree.addAll(annonceList);
            annonceAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Toast.makeText(this, "Erreur annonces : " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally { if (c != null) c.close(); }
    }

    private String getPremierePhoto(long id) {
        Cursor c = null;
        try {
            c = databaseHelper.getPhotosAnnonce(id);
            if (c.moveToFirst()) return getString(c, "photoPath");
        } catch (Exception ignored) {}
        finally { if (c != null) c.close(); }
        return "";
    }

    private static class AnnonceSimple {
        long id; String titre = ""; String description = ""; String quartier = ""; String ville = ""; double prix; String photo = "";
    }

    private class AnnonceSimpleAdapter extends RecyclerView.Adapter<AnnonceSimpleAdapter.Holder> {
        private final List<AnnonceSimple> list;
        AnnonceSimpleAdapter(List<AnnonceSimple> list) { this.list = list; }
        @NonNull @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_annonce, parent, false);
            return new Holder(v);
        }
        @Override
        public void onBindViewHolder(@NonNull Holder h, int position) {
            AnnonceSimple a = list.get(position);
            h.tvTitre.setText(a.titre);
            h.tvPrix.setText((int) a.prix + " DT / mois");
            h.tvDescription.setText(a.description);
            h.tvLocalisation.setText(a.quartier + " - " + a.ville);
            if (!a.photo.isEmpty()) {
                Glide.with(Interface7.this).load(a.photo).into(h.ivPhoto);
            } else {
                h.ivPhoto.setImageResource(R.drawable.appart);
            }

            // VOIR DETAILS -> DetailAnnonce
            h.btnDetails.setOnClickListener(v -> {
                Intent i = new Intent(Interface7.this, DetailAnnonce.class);
                i.putExtra("USER_EMAIL", emailConnecte);
                i.putExtra("emailEtudiant", emailConnecte);
                i.putExtra("idAnnonce", (int) a.id);
                i.putExtra("annonceId", a.id);
                i.putExtra("etudiantNom", tvBonjour.getText().toString().replace("Bonjour ", ""));
                startActivity(i);
            });
            h.itemView.setOnClickListener(v -> h.btnDetails.performClick());

            // FAVORI (logement) -> estFavori, pas estFavoriColocataire
            boolean isFav = databaseHelper.estFavori(emailConnecte, a.id);
            h.ivFavorite.setImageResource(isFav ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
            h.ivFavorite.setOnClickListener(v -> {
                if (databaseHelper.estFavori(emailConnecte, a.id)) {
                    databaseHelper.supprimerFavori(emailConnecte, a.id);
                    h.ivFavorite.setImageResource(R.drawable.ic_heart_outline);
                    Toast.makeText(Interface7.this, "Retiré des favoris", Toast.LENGTH_SHORT).show();
                } else {
                    databaseHelper.ajouterFavori(emailConnecte, a.id);
                    h.ivFavorite.setImageResource(R.drawable.ic_heart_filled);
                    Toast.makeText(Interface7.this, "Ajouté aux favoris", Toast.LENGTH_SHORT).show();
                }
            });
        }
        @Override public int getItemCount() { return list.size(); }
        class Holder extends RecyclerView.ViewHolder {
            ImageView ivPhoto, ivFavorite;
            TextView tvTitre, tvPrix, tvDescription, tvLocalisation;
            View btnDetails;
            Holder(@NonNull View v) {
                super(v);
                ivPhoto = v.findViewById(R.id.ivAnnoncePhoto);
                ivFavorite = v.findViewById(R.id.ivFavoriteAppart);
                tvTitre = v.findViewById(R.id.tvAnnonceTitre);
                tvPrix = v.findViewById(R.id.tvAnnoncePrix);
                tvDescription = v.findViewById(R.id.tvAnnonceDescription);
                tvLocalisation = v.findViewById(R.id.tvAnnonceLocalisation);
                btnDetails = v.findViewById(R.id.btnDetails);
            }
        }
    }

    private void chargerTop2Colocs() {
        List<Etudiant> tous = new ArrayList<>();
        Cursor c = null;
        try {
            c = databaseHelper.getReadableDatabase().query("Etudiant", null, null, null, null, null, "id DESC");
            while (c.moveToNext()) {
                String email = getString(c, "email");
                if (email.equalsIgnoreCase(emailConnecte)) continue;
                Etudiant e = new Etudiant(getString(c, "nomComplet"), email, getString(c, "universite"), getInt(c, "age", 21), getDouble(c, "budgetMax", 0), getString(c, "modeDeVie"), getString(c, "nbColocataires"), getString(c, "proprete"), getString(c, "animauxAcceptes"));
                e.setId(getLong(c, "id", 0));
                e.setQuartier(getString(c, "quartier"));
                e.setFaculte(getString(c, "faculte"));
                e.setPhotoUrl(getString(c, "photoUrl"));
                int score = ProfilAdapter.calculerCompatibilite(moi, e);
                e.setPourcentageCompatible(score);
                tous.add(e);
            }
            Collections.sort(tous, new Comparator<Etudiant>() {
                @Override public int compare(Etudiant a, Etudiant b) {
                    return Double.compare(b.getPourcentageCompatible(), a.getPourcentageCompatible());
                }
            });
            if (tous.isEmpty()) { layoutColocataires.setVisibility(View.GONE); return; }
            layoutColocataires.setVisibility(View.VISIBLE);
            Etudiant c1 = tous.get(0);
            tvColoc1Nom.setText(c1.getNomComplet());
            tvColoc1Pct.setText((int) c1.getPourcentageCompatible() + "% Compatible");
            if (!c1.getPhotoUrl().isEmpty()) Glide.with(this).load(c1.getPhotoUrl()).into(ivColoc1);
            if (tous.size() >= 2) {
                Etudiant c2 = tous.get(1);
                cardColoc2.setVisibility(View.VISIBLE);
                tvColoc2Nom.setText(c2.getNomComplet());
                tvColoc2Pct.setText((int) c2.getPourcentageCompatible() + "% Compatible");
                if (!c2.getPhotoUrl().isEmpty()) Glide.with(this).load(c2.getPhotoUrl()).into(ivColoc2);
            } else {
                cardColoc2.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Erreur colocs : " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        if (databaseHelper != null) databaseHelper.close();
        super.onDestroy();
    }
}