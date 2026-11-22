
package com.example.diar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class AnnonceAdapter
        extends RecyclerView.Adapter<AnnonceAdapter.AnnonceViewHolder> {

    private static final String TAG = "DIAR_FAVORIS";

    private final Context context;
    private final String baseUrl;
    private final DatabaseHelper databaseHelper;
    private final String userEmail;

    private final ArrayList<JSONObject> annonces = new ArrayList<>();
    private final ArrayList<JSONObject> annoncesOriginales = new ArrayList<>();

    public AnnonceAdapter(Context context, String baseUrl) {

        this.context = context;
        this.baseUrl = baseUrl;

        databaseHelper = new DatabaseHelper(context);

        // Récupérer l'email de l'étudiant connecté
        String email = "";

        SessionManager session = new SessionManager(context);

        if (session.getEmail() != null) {
            email = session.getEmail().trim();
        }

        // Fallback SharedPreferences
        if (email.isEmpty()) {

            SharedPreferences prefs =
                    context.getSharedPreferences(
                            "DIAR_PREFS",
                            Context.MODE_PRIVATE
                    );

            email = prefs.getString("email_etudiant", "");

            if (email == null || email.trim().isEmpty()) {
                email = prefs.getString("USER_EMAIL", "");
            }
        }

        userEmail = email == null
                ? ""
                : email.trim().toLowerCase(Locale.ROOT);

        Log.d(TAG, "Email étudiant : " + userEmail);
    }

    // Mettre à jour les annonces
    public void setAnnonces(ArrayList<JSONObject> nouvelleListe) {

        annonces.clear();
        annoncesOriginales.clear();

        if (nouvelleListe != null) {
            annonces.addAll(nouvelleListe);
            annoncesOriginales.addAll(nouvelleListe);
        }

        notifyDataSetChanged();
    }

    // Recherche des annonces
    public void filter(String texte) {

        annonces.clear();

        if (texte == null || texte.trim().isEmpty()) {

            annonces.addAll(annoncesOriginales);

        } else {

            String recherche = texte.toLowerCase(Locale.ROOT).trim();

            for (JSONObject annonce : annoncesOriginales) {

                String titre = annonce.optString("titre", "");
                String ville = annonce.optString("ville", "");
                String quartier = annonce.optString("quartier", "");
                String description = annonce.optString("description", "");

                if (titre.toLowerCase(Locale.ROOT).contains(recherche)
                        || ville.toLowerCase(Locale.ROOT).contains(recherche)
                        || quartier.toLowerCase(Locale.ROOT).contains(recherche)
                        || description.toLowerCase(Locale.ROOT).contains(recherche)) {

                    annonces.add(annonce);
                }
            }
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AnnonceViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(context).inflate(
                R.layout.item_annonce,
                parent,
                false
        );

        return new AnnonceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull AnnonceViewHolder holder,
            int position) {

        JSONObject annonce = annonces.get(position);

        long idAnnonce = annonce.optLong("id", -1);

        String titre = annonce.optString("titre", "Sans titre");
        String description = annonce.optString("description", "");
        String ville = annonce.optString("ville", "");
        String quartier = annonce.optString("quartier", "");
        String prix = annonce.optString("prix", "0");

        holder.tvTitre.setText(titre);
        holder.tvPrix.setText(prix + " DT");
        holder.tvDescription.setText(description);

        String localisation;

        if (!quartier.isEmpty() && !ville.isEmpty()) {
            localisation = quartier + ", " + ville;
        } else if (!ville.isEmpty()) {
            localisation = ville;
        } else {
            localisation = quartier;
        }

        holder.tvLocalisation.setText("📍 " + localisation);

        // Afficher la photo
        afficherPhoto(holder.ivPhoto, annonce);

        // Afficher l'état du favori depuis SQLite
        boolean estFavori = verifierFavori(idAnnonce);

        actualiserBoutonFavori(holder.btnFavori, estFavori);

        // Ajouter ou retirer des favoris
        holder.btnFavori.setOnClickListener(v -> {

            if (idAnnonce <= 0) {

                Toast.makeText(
                        context,
                        "Identifiant annonce invalide",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            if (userEmail.isEmpty()) {

                Toast.makeText(
                        context,
                        "Email étudiant introuvable. Reconnectez-vous.",
                        Toast.LENGTH_LONG
                ).show();

                return;
            }

            boolean favoriActuel = verifierFavori(idAnnonce);
            boolean resultat;

            if (favoriActuel) {

                resultat = databaseHelper.supprimerFavori(
                        userEmail,
                        idAnnonce
                );

                if (resultat) {
                    Toast.makeText(
                            context,
                            "Annonce retirée des favoris",
                            Toast.LENGTH_SHORT
                    ).show();
                }

            } else {

                resultat = databaseHelper.ajouterFavori(
                        userEmail,
                        idAnnonce
                );

                if (resultat) {
                    Toast.makeText(
                            context,
                            "Annonce ajoutée aux favoris ❤️",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            if (!resultat) {

                Toast.makeText(
                        context,
                        "Impossible d'enregistrer le favori. Vérifiez que l'annonce existe dans SQLite.",
                        Toast.LENGTH_LONG
                ).show();

                Log.e(
                        TAG,
                        "Échec opération favori : annonceId=" + idAnnonce
                );
            }

            // Actualiser le coeur
            actualiserBoutonFavori(
                    holder.btnFavori,
                    verifierFavori(idAnnonce)
            );
        });

        // Bouton détails
        holder.btnDetails.setOnClickListener(v ->
                ouvrirDetails(idAnnonce)
        );

        // Cliquer sur la carte ouvre les détails
        holder.itemView.setOnClickListener(v ->
                ouvrirDetails(idAnnonce)
        );
    }

    // Vérifier si l'annonce est dans les favoris SQLite
    private boolean verifierFavori(long idAnnonce) {

        if (idAnnonce <= 0 || userEmail.isEmpty()) {
            return false;
        }

        Cursor cursor = null;

        try {

            cursor = databaseHelper.getReadableDatabase().rawQuery(
                    "SELECT id FROM Favoris " +
                            "WHERE LOWER(TRIM(emailEtudiant))=? " +
                            "AND annonce_id=?",
                    new String[]{
                            userEmail,
                            String.valueOf(idAnnonce)
                    }
            );

            return cursor.moveToFirst();

        } catch (Exception e) {

            Log.e(TAG, "Erreur vérification favori", e);
            return false;

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // Actualiser le coeur
    private void actualiserBoutonFavori(
            ImageView bouton,
            boolean estFavori) {

        if (estFavori) {

            bouton.setImageResource(
                    R.drawable.ic_favorite_filled
            );

            bouton.setColorFilter(0xFFEF4444);

        } else {

            bouton.setImageResource(
                    R.drawable.ic_favorite_border
            );

            bouton.setColorFilter(0xFF64748B);
        }
    }

    // Ouvrir la page détail
    private void ouvrirDetails(long idAnnonce) {

        if (idAnnonce <= 0) {

            Toast.makeText(
                    context,
                    "Identifiant annonce invalide",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Intent intent = new Intent(context, DetailAnnonce.class);

        intent.putExtra("idAnnonce", (int) idAnnonce);

        context.startActivity(intent);
    }

    // Afficher la photo de l'annonce
    private void afficherPhoto(
            ImageView imageView,
            JSONObject annonce) {

        imageView.setImageResource(R.drawable.appart);

        JSONArray photos = annonce.optJSONArray("photos");

        if (photos == null || photos.length() == 0) {
            return;
        }

        String photoPath = photos.optString(0, "");

        if (photoPath.trim().isEmpty()
                || "null".equals(photoPath)) {
            return;
        }

        try {

            File imageFile = new File(photoPath);

            if (imageFile.exists() && imageFile.isFile()) {

                Bitmap bitmap = BitmapFactory.decodeFile(
                        imageFile.getAbsolutePath()
                );

                if (bitmap != null) {

                    imageView.setImageBitmap(bitmap);

                } else {

                    Log.e(TAG, "Impossible de décoder la photo");
                }

            } else {

                Log.e(TAG, "Photo introuvable : " + photoPath);
            }

        } catch (Exception e) {

            Log.e(TAG, "Erreur affichage photo", e);
        }
    }

    @Override
    public int getItemCount() {
        return annonces.size();
    }

    // ViewHolder
    public static class AnnonceViewHolder
            extends RecyclerView.ViewHolder {

        ImageView ivPhoto;
        TextView tvTitre;
        TextView tvPrix;
        TextView tvDescription;
        TextView tvLocalisation;

        ImageView btnFavori;
        Button btnDetails;

        public AnnonceViewHolder(@NonNull View itemView) {

            super(itemView);

            ivPhoto = itemView.findViewById(
                    R.id.ivAnnoncePhoto
            );

            tvTitre = itemView.findViewById(
                    R.id.tvAnnonceTitre
            );

            tvPrix = itemView.findViewById(
                    R.id.tvAnnoncePrix
            );

            tvDescription = itemView.findViewById(
                    R.id.tvAnnonceDescription
            );

            tvLocalisation = itemView.findViewById(
                    R.id.tvAnnonceLocalisation
            );

            btnFavori = itemView.findViewById(
                    R.id.ivFavoriteAppart
            );

            btnDetails = itemView.findViewById(
                    R.id.btnDetails
            );
        }
    }
}
