
package com.example.diar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;

public class Interface6 extends AppCompatActivity {

    private ImageButton btnBack;
    private EditText etBudget;
    private MaterialButton btnEnregistrer;

    private MaterialCardView cardStudio, cardAppartement;
    private MaterialCardView cardChambre, cardMaison;

    private MaterialCardView cardIndividuelle, cardColocation;

    private MaterialCheckBox cbWifi, cbClim, cbChauffage;
    private MaterialCheckBox cbCuisine, cbMachineLaver;
    private MaterialCheckBox cbParking, cbSalleDeBain;

    private TextView chipSahloul, chipKhezema, chipBouhdima;

    private String selectedTypeLogement = null;
    private String selectedLocation = null;
    private String selectedQuartier = null;
    private String userEmail;

    private DatabaseHelper databaseHelper;
    private boolean isSaving = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface6);

        SessionManager sessionManager = new SessionManager(this);
        userEmail = sessionManager.getEmail();

        if (userEmail == null || userEmail.trim().isEmpty()) {
            userEmail = getIntent().getStringExtra("USER_EMAIL");
        }

        if (userEmail == null || userEmail.trim().isEmpty()) {
            Toast.makeText(this,
                    "Email étudiant introuvable. Reconnectez-vous.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        userEmail = userEmail.trim();
        databaseHelper = new DatabaseHelper(this);

        initViews();
        setupListeners();
    }

    private void initViews() {

        btnBack = findViewById(R.id.btnBack);
        etBudget = findViewById(R.id.etBudget);
        btnEnregistrer = findViewById(R.id.btnEnregistrer);

        cardStudio = findViewById(R.id.cardStudio);
        cardAppartement = findViewById(R.id.cardAppartement);
        cardChambre = findViewById(R.id.cardChambre);
        cardMaison = findViewById(R.id.cardMaison);

        cardIndividuelle = findViewById(R.id.cardIndividuelle);
        cardColocation = findViewById(R.id.cardColocation);

        cbWifi = findViewById(R.id.cbWifi);
        cbClim = findViewById(R.id.cbClim);
        cbChauffage = findViewById(R.id.cbChauffage);
        cbCuisine = findViewById(R.id.cbCuisine);
        cbMachineLaver = findViewById(R.id.cbMachineLaver);
        cbParking = findViewById(R.id.cbParking);
        cbSalleDeBain = findViewById(R.id.cbSalleDeBain);

        chipSahloul = findViewById(R.id.chipSahloul);
        chipKhezema = findViewById(R.id.chipKhezema);
        chipBouhdima = findViewById(R.id.chipBouhdima);
    }

    private void setupListeners() {

        btnBack.setOnClickListener(v -> finish());

        cardStudio.setOnClickListener(v ->
                selectTypeLogement(cardStudio, "Studio"));

        cardAppartement.setOnClickListener(v ->
                selectTypeLogement(cardAppartement, "Appartement"));

        cardChambre.setOnClickListener(v ->
                selectTypeLogement(cardChambre, "Chambre"));

        cardMaison.setOnClickListener(v ->
                selectTypeLogement(cardMaison, "Maison"));

        cardIndividuelle.setOnClickListener(v ->
                selectLocationType(cardIndividuelle, "Individuelle"));

        cardColocation.setOnClickListener(v ->
                selectLocationType(cardColocation, "Colocation"));

        chipSahloul.setOnClickListener(v ->
                selectQuartier(chipSahloul, "Sahloul"));

        chipKhezema.setOnClickListener(v ->
                selectQuartier(chipKhezema, "Khezema"));

        chipBouhdima.setOnClickListener(v ->
                selectQuartier(chipBouhdima, "Bouhdima"));

        btnEnregistrer.setOnClickListener(v -> validerEtEnregistrer());
    }

    private void selectTypeLogement(
            MaterialCardView selectedCard,
            String type) {

        selectedTypeLogement = type;

        MaterialCardView[] cards = {
                cardStudio, cardAppartement, cardChambre, cardMaison
        };

        for (MaterialCardView card : cards) {
            if (card == selectedCard) {
                card.setStrokeWidth((int) (2 * getResources()
                        .getDisplayMetrics().density));
                card.setStrokeColor(Color.parseColor("#86EFAC"));
                card.setCardBackgroundColor(Color.parseColor("#E6F4EA"));
            } else {
                card.setStrokeWidth(0);
                card.setCardBackgroundColor(Color.parseColor("#F8FAFC"));
            }
        }
    }

    private void selectLocationType(
            MaterialCardView selectedCard,
            String location) {

        selectedLocation = location;

        MaterialCardView[] cards = {
                cardIndividuelle, cardColocation
        };

        for (MaterialCardView card : cards) {
            if (card == selectedCard) {
                card.setStrokeWidth((int) (2 * getResources()
                        .getDisplayMetrics().density));
                card.setStrokeColor(Color.parseColor("#86EFAC"));
                card.setCardBackgroundColor(Color.parseColor("#E6F4EA"));
            } else {
                card.setStrokeWidth(0);
                card.setCardBackgroundColor(Color.parseColor("#F8FAFC"));
            }
        }
    }

    private void selectQuartier(TextView selectedChip, String quartier) {

        selectedQuartier = quartier;

        TextView[] chips = {
                chipSahloul, chipKhezema, chipBouhdima
        };

        for (TextView chip : chips) {
            if (chip == selectedChip) {
                chip.setBackgroundResource(R.drawable.bg_chip_selected);
                chip.setTextColor(Color.BLACK);
            } else {
                chip.setBackgroundResource(R.drawable.bg_chip_unselected);
                chip.setTextColor(Color.parseColor("#64748B"));
            }
        }
    }

    private void validerEtEnregistrer() {

        if (isSaving) return;

        if (selectedTypeLogement == null) {
            Toast.makeText(this,
                    "Veuillez sélectionner un type de logement",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (selectedLocation == null) {
            Toast.makeText(this,
                    "Veuillez sélectionner un type de location",
                    Toast.LENGTH_LONG).show();
            return;
        }

        String budgetStr = etBudget.getText().toString().trim();

        if (budgetStr.isEmpty()) {
            etBudget.setError("Veuillez saisir votre budget maximal");
            etBudget.requestFocus();
            return;
        }

        double budgetMax;

        try {
            budgetMax = Double.parseDouble(budgetStr.replace(",", "."));
        } catch (NumberFormatException e) {
            etBudget.setError("Format de budget invalide");
            etBudget.requestFocus();
            return;
        }

        if (budgetMax <= 0 || budgetMax > 500) {
            etBudget.setError("Le budget doit être entre 1 et 500 DT");
            etBudget.requestFocus();
            return;
        }

        StringBuilder equipements = new StringBuilder();

        if (cbWifi.isChecked()) equipements.append("Wi-Fi, ");
        if (cbClim.isChecked()) equipements.append("Climatisation, ");
        if (cbChauffage.isChecked()) equipements.append("Chauffage, ");
        if (cbCuisine.isChecked()) equipements.append("Cuisine équipée, ");
        if (cbMachineLaver.isChecked()) equipements.append("Machine à laver, ");
        if (cbParking.isChecked()) equipements.append("Parking, ");
        if (cbSalleDeBain.isChecked()) equipements.append("Salle de bain privée, ");

        if (equipements.length() == 0) {
            Toast.makeText(this,
                    "Veuillez sélectionner au moins un équipement",
                    Toast.LENGTH_LONG).show();
            return;
        }

        String equipementsFinal = equipements.toString()
                .replaceAll(", $", "");

        if (selectedQuartier == null) {
            Toast.makeText(this,
                    "Veuillez sélectionner un quartier préféré",
                    Toast.LENGTH_LONG).show();
            return;
        }

        enregistrerDansSQLite(
                userEmail,
                selectedTypeLogement,
                selectedLocation,
                budgetMax,
                equipementsFinal,
                selectedQuartier
        );
    }

    private void enregistrerDansSQLite(
            String email,
            String typeLogement,
            String location,
            double budget,
            String equipements,
            String quartier) {

        isSaving = true;
        btnEnregistrer.setEnabled(false);
        btnEnregistrer.setText("Enregistrement...");

        boolean success = false;

        try {
            success = databaseHelper.updatePreferencesEtudiant(
                    email,
                    typeLogement,
                    location,
                    budget,
                    equipements,
                    quartier
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        isSaving = false;
        btnEnregistrer.setEnabled(true);
        btnEnregistrer.setText("Enregistrer");

        if (success) {
            Toast.makeText(this,
                    "Préférences enregistrées avec succès !",
                    Toast.LENGTH_LONG).show();

            Intent intent = new Intent(
                    Interface6.this,
                    Interface7.class
            );

            intent.putExtra("USER_EMAIL", email);
            startActivity(intent);
            finish();

        } else {
            Toast.makeText(this,
                    "Erreur : étudiant introuvable dans SQLite.",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}
