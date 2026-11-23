package com.example.diar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class ProfilAdapter extends RecyclerView.Adapter<ProfilAdapter.ProfilViewHolder> {

    private final List<Etudiant> listeEtudiants;
    private final String emailConnecte;
    private final OnProfilClickListener listener;
    private Etudiant moi;
    private boolean pageFavoris = false;

    public interface OnProfilClickListener {
        void onProfilClick(Etudiant etudiant);
    }

    public ProfilAdapter(List<Etudiant> listeEtudiants, String emailConnecte, OnProfilClickListener listener) {
        this.listeEtudiants = listeEtudiants;
        this.emailConnecte = emailConnecte;
        this.listener = listener;
    }

    public void setEtudiantConnecte(Etudiant moi) {
        this.moi = moi;
    }

    public void setPageFavoris(boolean pageFavoris) {
        this.pageFavoris = pageFavoris;
    }

    @NonNull
    @Override
    public ProfilViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profil_coloc, parent, false);
        return new ProfilViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfilViewHolder holder, int position) {
        Etudiant etudiant = listeEtudiants.get(position);
        holder.txtNom.setText(etudiant.getNomComplet() + ", " + etudiant.getAge() + " ans");

        int score = 0;
        if (moi != null) {
            score = calculerCompatibilite(moi, etudiant);
        } else {
            score = (int) Math.round(etudiant.getPourcentageCompatible());
        }
        holder.txtCompatibilite.setText(score + "% Compatible");
        holder.txtUniversiteAge.setText(etudiant.getUniversite());
        holder.txtBudget.setText("Budget : " + etudiant.getBudgetMax() + " DT");

        if (etudiant.getPhotoUrl() != null && !etudiant.getPhotoUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext()).load(etudiant.getPhotoUrl()).into(holder.imgProfil);
        } else {
            holder.imgProfil.setImageResource(R.drawable.ic_profile);
        }

        DatabaseHelper db = new DatabaseHelper(holder.itemView.getContext());
        boolean isFavori = db.estFavoriColocataire(emailConnecte, etudiant.getId());
        holder.ivFavori.setImageResource(isFavori ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);

        holder.ivFavori.setOnClickListener(v -> {
            if (etudiant.getId() == 0) {
                Toast.makeText(holder.itemView.getContext(), "Erreur id", Toast.LENGTH_SHORT).show();
                return;
            }
            if (db.estFavoriColocataire(emailConnecte, etudiant.getId())) {
                db.supprimerFavoriColocataire(emailConnecte, etudiant.getId());
                holder.ivFavori.setImageResource(R.drawable.ic_heart_outline);
                Toast.makeText(holder.itemView.getContext(), "Retiré des favoris", Toast.LENGTH_SHORT).show();
                if (pageFavoris) {
                    int pos = holder.getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        listeEtudiants.remove(pos);
                        notifyItemRemoved(pos);
                    }
                }
            } else {
                db.ajouterFavoriColocataire(emailConnecte, etudiant.getId());
                holder.ivFavori.setImageResource(R.drawable.ic_heart_filled);
                Toast.makeText(holder.itemView.getContext(), "Ajouté aux favoris", Toast.LENGTH_SHORT).show();
            }
            db.close();
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onProfilClick(etudiant);
        });
    }

    @Override
    public int getItemCount() {
        return listeEtudiants == null ? 0 : listeEtudiants.size();
    }

    public static int calculerCompatibilite(Etudiant moi, Etudiant autre) {
        int score = 0;
        if (moi.getQuartier() != null && moi.getQuartier().equals(autre.getQuartier())) score += 30;
        if (Math.abs(moi.getBudgetMax() - autre.getBudgetMax()) <= 50) score += 20;
        if (moi.getModeDeVie() != null && moi.getModeDeVie().equals(autre.getModeDeVie())) score += 20;
        if (Math.abs(moi.getAge() - autre.getAge()) <= 2) score += 15;
        if (moi.getFaculte() != null && moi.getFaculte().equals(autre.getFaculte())) score += 15;
        return score;
    }

    public static class ProfilViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProfil;
        ImageView ivFavori;
        TextView txtNom;
        TextView txtCompatibilite;
        TextView txtUniversiteAge;
        TextView txtBudget;

        public ProfilViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProfil = itemView.findViewById(R.id.imgProfil);
            ivFavori = itemView.findViewById(R.id.ivFavori);
            txtNom = itemView.findViewById(R.id.txtNom);
            txtCompatibilite = itemView.findViewById(R.id.txtCompatibilite);
            txtUniversiteAge = itemView.findViewById(R.id.txtUniversiteAge);
            txtBudget = itemView.findViewById(R.id.txtBudget);
        }
    }
}