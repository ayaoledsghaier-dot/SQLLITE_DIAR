package com.example.diar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class Interface5 extends AppCompatActivity {

    private AutoCompleteTextView spinnerGenre;
    private AutoCompleteTextView spinnerUniversite;
    private AutoCompleteTextView spinnerNiveau;
    private AutoCompleteTextView spinnerFaculte;
    private EditText etAge;
    private MaterialButton btnSuivant;
    private String userEmail;
    private String userName;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface5);

        databaseHelper = new DatabaseHelper(this);
        SessionManager sessionManager = new SessionManager(this);

        userEmail = sessionManager.getEmail();
        userName = sessionManager.getNom();

        if (userEmail == null || userEmail.trim().isEmpty()) {
            userEmail = getIntent().getStringExtra("USER_EMAIL");
        }
        if (userName == null || userName.trim().isEmpty()) {
            userName = getIntent().getStringExtra("USER_NAME");
        }

        spinnerGenre = findViewById(R.id.spinnerGenre);
        spinnerUniversite = findViewById(R.id.spinnerUniversite);
        spinnerNiveau = findViewById(R.id.spinnerNiveau);
        spinnerFaculte = findViewById(R.id.spinnerFaculte);
        etAge = findViewById(R.id.etAge);
        btnSuivant = findViewById(R.id.btnSuivant);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        if (userEmail == null || userEmail.trim().isEmpty()) {
            Toast.makeText(this, "Email étudiant introuvable. Reconnectez-vous.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setupSpinners();
        btnSuivant.setOnClickListener(v -> enregistrerProfil());
    }

    private void setupSpinners() {
        String[] genres = {"Sélectionnez le genre", "Homme", "Femme"};
        spinnerGenre.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, genres));

        String[] universites = {"Sélectionnez l'université", "Université de Sousse", "Université de Monastir", "Université de Sfax", "Université de Tunis"};
        spinnerUniversite.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, universites));

        String[] niveaux = {"Sélectionnez le niveau", "première année", "deuxième année", "troisième année", "Mastère"};
        spinnerNiveau.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, niveaux));

        spinnerFaculte.setEnabled(false);
        spinnerFaculte.setText("Choisissez d'abord une université", false);

        spinnerUniversite.setOnItemClickListener((parent, view, position, id) -> {
            String universite = parent.getItemAtPosition(position).toString();
            chargerFacultes(universite);
        });
    }

    private void chargerFacultes(String universite) {
        String[] facultes;
        if (universite.equals("Université de Sousse")) {
            facultes = new String[]{
                    "Sélectionnez votre établissement",
                    "Faculté de Médecine de Sousse",
                    "Faculté de Droit et des Sciences Politiques de Sousse",
                    "Faculté des Lettres et des Sciences Humaines de Sousse",
                    "Faculté des Sciences Économiques et de Gestion de Sousse",
                    "Institut Supérieur de Gestion de Sousse",
                    "Institut des Hautes Études Commerciales de Sousse",
                    "Institut Supérieur de Finance et de Fiscalité de Sousse",
                    "Institut Supérieur d'Informatique et des Technologies de Communication de Hammam Sousse",
                    "Institut Supérieur du Transport et de la Logistique de Sousse",
                    "Institut Supérieur des Beaux-Arts de Sousse",
                    "École Nationale d'Ingénieurs de Sousse",
                    "École Supérieure des Sciences et Techniques de la Santé de Sousse",
                    "Institut Supérieur de Musique de Sousse",
                    "Institut Supérieur des Sciences Appliquées et de Technologie de Sousse",
                    "Institut Supérieur Agronomique de Chott Meriem",
                    "Institut Supérieur des Sciences Infirmières de Sousse",
                    "École Supérieure des Sciences et des Technologies de Hammam Sousse"
            };
            spinnerFaculte.setEnabled(true);
        } else {
            facultes = new String[]{"Liste non disponible pour cette université"};
            spinnerFaculte.setEnabled(false);
        }
        spinnerFaculte.setAdapter(new ArrayAdapter<>(Interface5.this, android.R.layout.simple_dropdown_item_1line, facultes));
        spinnerFaculte.setText("", false);
    }

    private String obtenirVille(String faculte) {
        if (faculte == null) return "";
        if (faculte.contains("Hammam Sousse")) return "Hammam Sousse";
        if (faculte.contains("Chott Meriem")) return "Chott Meriem";
        if (faculte.contains("Sousse")) return "Sousse";
        return "";
    }

    private void enregistrerProfil() {
        String genre = spinnerGenre.getText().toString().trim();
        String universite = spinnerUniversite.getText().toString().trim();
        String niveau = spinnerNiveau.getText().toString().trim();
        String faculte = spinnerFaculte.getText().toString().trim();
        String ageText = etAge.getText().toString().trim();

        if (genre.isEmpty() || genre.equals("Sélectionnez le genre")) {
            spinnerGenre.setError("Choisissez votre genre");
            return;
        }
        if (universite.isEmpty() || universite.equals("Sélectionnez l'université")) {
            spinnerUniversite.setError("Choisissez votre université");
            return;
        }
        if (niveau.isEmpty() || niveau.equals("Sélectionnez le niveau")) {
            spinnerNiveau.setError("Choisissez votre niveau");
            return;
        }
        if (universite.equals("Université de Sousse")) {
            if (faculte.isEmpty() || faculte.equals("Sélectionnez votre établissement")) {
                spinnerFaculte.setError("Choisissez votre faculté ou institut");
                return;
            }
        } else {
            faculte = "";
        }

        if (ageText.isEmpty()) {
            etAge.setError("Âge obligatoire");
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageText);
        } catch (Exception e) {
            etAge.setError("Âge invalide");
            return;
        }

        if (age < 18 || age > 30) {
            etAge.setError("L'âge doit être entre 18 et 30");
            Toast.makeText(this, "L'âge doit être entre 18 et 30", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userEmail == null || userEmail.trim().isEmpty()) {
            Toast.makeText(this, "Email étudiant manquant", Toast.LENGTH_LONG).show();
            return;
        }

        String ville = obtenirVille(faculte);

        boolean success = databaseHelper.enregistrerProfilEtudiant(userEmail, genre, universite, niveau, faculte, ville, age);

        if (success) {
            Toast.makeText(Interface5.this, "Profil enregistré avec succès !", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Interface5.this, Interfaceverification.class);
            intent.putExtra("USER_EMAIL", userEmail);
            intent.putExtra("USER_NAME", userName);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(Interface5.this, "Erreur : impossible d'enregistrer le profil. Vérifiez le compte étudiant.", Toast.LENGTH_LONG).show();
        }
    }
}