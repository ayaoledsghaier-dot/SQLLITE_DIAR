
package com.example.diar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.util.HashSet;
import java.util.Set;

public class AnnonceAdapter extends RecyclerView.Adapter<AnnonceAdapter.AnnonceViewHolder> {

    private final Context context;
    private final String baseUrl;

    private final ArrayList<JSONObject> annonces = new ArrayList<>();
    private final ArrayList<JSONObject> annoncesOriginales = new ArrayList<>();

    private final SharedPreferences preferences;

    public AnnonceAdapter(Context context, String baseUrl) {
        this.context = context;
        this.baseUrl = baseUrl;

        preferences = context.getSharedPreferences(
                "DIAR_FAVORIS",
                Context.MODE_PRIVATE
        );
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

            String recherche = texte.toLowerCase().trim();

            for (JSONObject annonce : annoncesOriginales) {

                String titre = annonce.optString("titre", "");
                String ville = annonce.optString("ville", "");
                String quartier = annonce.optString("quartier", "");
                String description = annonce.optString("description", "");

                if (titre.toLowerCase().contains(recherche)
                        || ville.toLowerCase().contains(recherche)
                        || quartier.toLowerCase().contains(recherche)
                        || description.toLowerCase().contains(recherche)) {

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

        // Photo de l'annonce
        afficherPhoto(holder.ivPhoto, annonce);

        // Etat du bouton favoris
        actualiserBoutonFavori(holder.btnFavori, idAnnonce);

        // Ajouter ou retirer des favoris
        holder.btnFavori.setOnClickListener(v -> {

            if (idAnnonce == -1) {

                Toast.makeText(
                        context,
                        "Identifiant annonce invalide",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            Set<String> favoris = new HashSet<>(
                    preferences.getStringSet(
                            "annonces_favorites",
                            new HashSet<>()
                    )
            );

            String id = String.valueOf(idAnnonce);

            if (favoris.contains(id)) {

                favoris.remove(id);

                Toast.makeText(
                        context,
                        "Annonce retirée des favoris",
                        Toast.LENGTH_SHORT
                ).show();

            } else {

                favoris.add(id);

                Toast.makeText(
                        context,
                        "Annonce ajoutée aux favoris ❤️",
                        Toast.LENGTH_SHORT
                ).show();
            }

            preferences.edit()
                    .putStringSet("annonces_favorites", favoris)
                    .apply();

            actualiserBoutonFavori(holder.btnFavori, idAnnonce);
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

    // Afficher le coeur selon l'état du favori
    private void actualiserBoutonFavori(
            ImageView bouton,
            long idAnnonce) {

        if (idAnnonce == -1) return;

        Set<String> favoris = preferences.getStringSet(
                "annonces_favorites",
                new HashSet<>()
        );

        boolean estFavori = favoris != null
                && favoris.contains(String.valueOf(idAnnonce));

        if (estFavori) {

            bouton.setImageResource(
                    R.drawable.ic_favorite_filled
            );

        } else {

            bouton.setImageResource(
                    R.drawable.ic_favorite_border
            );
        }

        bouton.setColorFilter(0xFFEF4444);
    }

    // Ouvrir la page détail
    private void ouvrirDetails(long idAnnonce) {

        if (idAnnonce == -1) {

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

    // Charger la photo enregistrée dans le téléphone
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

                    Log.e(
                            "DIAR_IMAGE",
                            "Impossible de décoder la photo"
                    );
                }

            } else {

                Log.e(
                        "DIAR_IMAGE",
                        "Photo introuvable : " + photoPath
                );
            }

        } catch (Exception e) {

            Log.e(
                    "DIAR_IMAGE",
                    "Erreur affichage photo",
                    e
            );
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

        // Correction : le bouton favoris est un ImageView
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
