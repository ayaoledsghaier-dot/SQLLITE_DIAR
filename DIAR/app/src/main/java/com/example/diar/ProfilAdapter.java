
package com.example.diar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class ProfilAdapter extends RecyclerView.Adapter<ProfilAdapter.ProfilViewHolder> {

    private final List<Etudiant> listeEtudiants;
    private final String emailConnecte;
    private final OnProfilClickListener listener;

    // Interface pour gérer le clic sur un profil
    public interface OnProfilClickListener {
        void onProfilClick(Etudiant etudiant);
    }

    // Constructeur avec les 3 paramètres
    public ProfilAdapter(
            List<Etudiant> listeEtudiants,
            String emailConnecte,
            OnProfilClickListener listener) {

        this.listeEtudiants = listeEtudiants;
        this.emailConnecte = emailConnecte;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProfilViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_profil_coloc, parent, false);

        return new ProfilViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ProfilViewHolder holder,
            int position) {

        Etudiant etudiant = listeEtudiants.get(position);

        // Nom et âge
        String nom = etudiant.getNomComplet() != null
                ? etudiant.getNomComplet()
                : "Étudiant";

        holder.txtNom.setText(
                nom + ", " + etudiant.getAge() + " ans"
        );

        // Compatibilité
        holder.txtCompatibilite.setText(
                etudiant.getPourcentage() + "% Compatible"
        );

        // Université
        String universite = etudiant.getUniversite() != null
                ? etudiant.getUniversite()
                : "Université de Sousse";

        holder.txtUniversiteAge.setText(universite);

        // Budget et propreté
        String proprete = etudiant.getProprete() != null
                && !etudiant.getProprete().isEmpty()
                ? " • Propreté : " + etudiant.getProprete()
                : "";

        holder.txtBudget.setText(
                "Budget : " + etudiant.getBudgetMax()
                        + " DT" + proprete
        );

        // Photo de profil
        if (etudiant.getPhotoUrl() != null
                && !etudiant.getPhotoUrl().isEmpty()) {

            Glide.with(holder.itemView.getContext())
                    .load(etudiant.getPhotoUrl())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(holder.imgProfil);

        } else {
            holder.imgProfil.setImageResource(
                    R.drawable.ic_launcher_foreground
            );
        }

        // Clic sur le profil
        holder.itemView.setOnClickListener(v -> {

            int currentPosition = holder.getAdapterPosition();

            if (currentPosition != RecyclerView.NO_POSITION) {

                Etudiant etudiantClique =
                        listeEtudiants.get(currentPosition);

                if (listener != null) {
                    listener.onProfilClick(etudiantClique);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return listeEtudiants == null
                ? 0
                : listeEtudiants.size();
    }

    public static class ProfilViewHolder
            extends RecyclerView.ViewHolder {

        ShapeableImageView imgProfil;

        TextView txtNom;
        TextView txtCompatibilite;
        TextView txtUniversiteAge;
        TextView txtBudget;

        public ProfilViewHolder(@NonNull View itemView) {
            super(itemView);

            imgProfil = itemView.findViewById(R.id.imgProfil);
            txtNom = itemView.findViewById(R.id.txtNom);
            txtCompatibilite = itemView.findViewById(R.id.txtCompatibilite);
            txtUniversiteAge = itemView.findViewById(R.id.txtUniversiteAge);
            txtBudget = itemView.findViewById(R.id.txtBudget);
        }
    }
}
