
package com.example.diar;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.File;

public class Interface13 extends AppCompatActivity {

    private ImageButton btnBack;
    private ShapeableImageView imgProfile;

    private TextView tvName;
    private TextView tvMatchPercentage;
    private TextView tvUniversiteAge;
    private TextView tvBudget;
    private TextView tvLocalisation;

    private ChipGroup chipGroupModeDeVie;
    private ChipGroup chipGroupPreferences;

    private MaterialButton btnContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface13);

        btnBack = findViewById(R.id.btnBack);
        imgProfile = findViewById(R.id.imgProfile);

        tvName = findViewById(R.id.tvName);
        tvMatchPercentage = findViewById(R.id.tvMatchPercentage);
        tvUniversiteAge = findViewById(R.id.tvUniversiteAge);
        tvBudget = findViewById(R.id.tvBudget);
        tvLocalisation = findViewById(R.id.tvLocalisation);

        chipGroupModeDeVie = findViewById(R.id.chipGroupModeDeVie);
        chipGroupPreferences = findViewById(R.id.chipGroupPreferences);

        btnContact = findViewById(R.id.btnContact);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Récupération des données envoyées par ProfilAdapter
        String nom = getIntent().getStringExtra("NOM");
        int pourcentage = getIntent().getIntExtra("POURCENTAGE", 0);
        String universite = getIntent().getStringExtra("UNIVERSITE");
        int age = getIntent().getIntExtra("AGE", 21);
        double budget = getIntent().getDoubleExtra("BUDGET", 0.0);

        String quartier = getIntent().getStringExtra("QUARTIER");
        String modeDeVie = getIntent().getStringExtra("MODE_DE_VIE");
        String proprete = getIntent().getStringExtra("PROPRETE");
        String animaux = getIntent().getStringExtra("ANIMAUX");

        // PHOTO_URL contient maintenant le chemin local SQLite
        String photoPath = getIntent().getStringExtra("PHOTO_URL");

        tvName.setText(
                nom != null ? nom : "Nom indisponible"
        );

        tvMatchPercentage.setText(
                "✿ " + pourcentage + "% compatible"
        );

        String univ = (
                universite != null && !universite.isEmpty()
        ) ? universite : "Université";

        tvUniversiteAge.setText(
                univ + " • " + age + " ans"
        );

        tvBudget.setText(
                "Budget : " + budget + " DT"
        );

        tvLocalisation.setText(
                quartier != null && !quartier.isEmpty()
                        ? quartier
                        : "Non spécifié"
        );

        afficherPhotoLocale(photoPath);

        // Mode de vie
        chipGroupModeDeVie.removeAllViews();

        if (modeDeVie != null && !modeDeVie.trim().isEmpty()) {
            String[] elements = modeDeVie.split(",");

            for (String element : elements) {
                ajouterChip(
                        chipGroupModeDeVie,
                        element.trim()
                );
            }
        }

        // Préférences
        chipGroupPreferences.removeAllViews();

        if (proprete != null && !proprete.trim().isEmpty()) {
            ajouterChip(
                    chipGroupPreferences,
                    "Propreté : " + proprete.trim()
            );
        }

        if (animaux != null && !animaux.trim().isEmpty()) {
            ajouterChip(
                    chipGroupPreferences,
                    "Animaux : " + animaux.trim()
            );
        }

        // Bouton contacter
        btnContact.setOnClickListener(v -> {
            Toast.makeText(
                    this,
                    "Profil de " + (nom != null ? nom : "cet étudiant"),
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    private void afficherPhotoLocale(String photoPath) {
        imgProfile.setImageResource(R.drawable.coloc);

        if (photoPath == null ||
                photoPath.trim().isEmpty() ||
                "null".equals(photoPath)) {
            return;
        }

        try {
            File imageFile = new File(photoPath);

            if (imageFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(
                        imageFile.getAbsolutePath()
                );

                if (bitmap != null) {
                    imgProfile.setImageBitmap(bitmap);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ajouterChip(ChipGroup group, String texte) {
        Chip chip = new Chip(this);

        chip.setText(texte);

        chip.setChipBackgroundColor(
                ColorStateList.valueOf(
                        Color.parseColor("#F1F5F9")
                )
        );

        chip.setTextColor(
                Color.parseColor("#475569")
        );

        chip.setChipCornerRadius(
                getResources().getDisplayMetrics().density * 18
        );

        group.addView(chip);
    }
}
