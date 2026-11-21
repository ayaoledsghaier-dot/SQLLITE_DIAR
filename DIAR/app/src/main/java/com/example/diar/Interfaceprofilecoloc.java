package com.example.diar;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import android.content.Intent;

public class Interfaceprofilecoloc extends AppCompatActivity {

    private ChipGroup chipGroupLifestyle, chipGroupRoommates;
    private ChipGroup chipGroupPets, chipGroupProprete;

    private MaterialButton btnSave;
    private ImageButton btnBack;

    private DatabaseHelper databaseHelper;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interfacecoloc);

        databaseHelper = new DatabaseHelper(this);

        email = getIntent().getStringExtra("USER_EMAIL");

        if (email == null || email.trim().isEmpty()) {
            email = new SessionManager(this).getEmail();
        }

        if (email == null || email.trim().isEmpty()) {
            Toast.makeText(this, "E-mail introuvable", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        chipGroupLifestyle = findViewById(R.id.chipGroupLifestyle);
        chipGroupRoommates = findViewById(R.id.chipGroupRoommates);
        chipGroupPets = findViewById(R.id.chipGroupPets);
        chipGroupProprete = findViewById(R.id.chipGroupProprete);

        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> enregistrerPreferences());
    }

    private String recupererChoixMultiples(ChipGroup group) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < group.getChildCount(); i++) {
            View view = group.getChildAt(i);

            if (view instanceof Chip) {
                Chip chip = (Chip) view;

                if (chip.isChecked()) {
                    if (result.length() > 0) {
                        result.append(", ");
                    }

                    result.append(chip.getText().toString());
                }
            }
        }

        return result.toString();
    }

    private String recupererChoixUnique(ChipGroup group) {
        int selectedId = group.getCheckedChipId();

        if (selectedId == View.NO_ID) {
            return "";
        }

        Chip selectedChip = findViewById(selectedId);

        return selectedChip != null
                ? selectedChip.getText().toString()
                : "";
    }

    private void enregistrerPreferences() {

        String modeDeVie = recupererChoixMultiples(chipGroupLifestyle);
        String nbColocataires = recupererChoixUnique(chipGroupRoommates);
        String animauxAcceptes = recupererChoixUnique(chipGroupPets);
        String proprete = recupererChoixUnique(chipGroupProprete);

        if (modeDeVie.isEmpty()
                && nbColocataires.isEmpty()
                && animauxAcceptes.isEmpty()
                && proprete.isEmpty()) {

            Toast.makeText(
                    this,
                    "Veuillez sélectionner au moins une préférence",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("modeDeVie", modeDeVie);
        values.put("nbColocataires", nbColocataires);
        values.put("animauxAcceptes", animauxAcceptes);
        values.put("proprete", proprete);

        int lignes = db.update(
                "Etudiant",
                values,
                "email = ?",
                new String[]{email}
        );

        if (lignes > 0) {
            Toast.makeText(
                    this,
                    "Préférences enregistrées avec succès !",
                    Toast.LENGTH_LONG
            ).show();

            Intent intent = new Intent(
                    Interfaceprofilecoloc.this,
                    Interface7.class
            );

            intent.putExtra("USER_EMAIL", email);
            startActivity(intent);
            finish();

        } else {
            Toast.makeText(
                    this,
                    "Erreur : étudiant introuvable",
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}