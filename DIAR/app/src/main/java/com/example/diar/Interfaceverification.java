package com.example.diar;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.googlecode.tesseract.android.TessBaseAPI;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Interfaceverification extends AppCompatActivity {

    private ImageView ivPreview;
    private MaterialButton btnVerify;
    private Uri documentUri;
    private Bitmap selectedBitmap;
    private String userEmail;
    private DatabaseHelper db;

    private final ActivityResultLauncher<String> picker = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null) return;
                try {
                    InputStream in = getContentResolver().openInputStream(uri);
                    Bitmap bmp = BitmapFactory.decodeStream(in);
                    in.close();
                    documentUri = uri;
                    selectedBitmap = bmp;
                    ivPreview.setImageBitmap(bmp);
                    ivPreview.setVisibility(ImageView.VISIBLE);
                } catch (Exception e) {
                    Toast.makeText(this, "Erreur image", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface_verif_etudiant);

        userEmail = getIntent().getStringExtra("USER_EMAIL");
        if (userEmail == null) userEmail = new SessionManager(this).getEmail();

        db = new DatabaseHelper(this);
        ivPreview = findViewById(R.id.ivPreview);
        btnVerify = findViewById(R.id.btnVerify);
        MaterialCardView card = findViewById(R.id.cardUploadBox);
        TextView back = findViewById(R.id.btnBack);

        back.setOnClickListener(v -> finish());
        card.setOnClickListener(v -> picker.launch("image/*"));

        btnVerify.setOnClickListener(v -> {
            if (selectedBitmap == null) {
                Toast.makeText(this, "Choisissez une attestation", Toast.LENGTH_SHORT).show();
                return;
            }
            btnVerify.setEnabled(false);
            btnVerify.setText("Verification...");
            new Thread(this::lireTexte).start();
        });
    }

    private void lireTexte() {
        try {
            String path = getFilesDir() + "/tesseract/";
            File dir = new File(path + "tessdata/");
            if (!dir.exists()) dir.mkdirs();
            File fra = new File(dir, "fra.traineddata");
            if (!fra.exists()) {
                InputStream in = getAssets().open("tessdata/fra.traineddata");
                OutputStream out = new FileOutputStream(fra);
                byte[] b = new byte[1024];
                int n;
                while ((n = in.read(b)) > 0) out.write(b, 0, n);
                in.close();
                out.close();
            }

            TessBaseAPI tess = new TessBaseAPI();
            tess.init(path, "fra");
            tess.setImage(selectedBitmap);
            String ocr = tess.getUTF8Text();
            tess.end();
            if (ocr == null) ocr = "";

            Log.d("OCR_TEST", "Texte lu : " + ocr);

            String texteMaj = normaliser(ocr);
            final String texteFinal = texteMaj;
            runOnUiThread(() -> verifier(texteFinal));

        } catch (Exception e) {
            runOnUiThread(() -> {
                btnVerify.setEnabled(true);
                btnVerify.setText("Verifier");
                Toast.makeText(this, "Erreur OCR", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private String normaliser(String s) {
        if (s == null) return "";
        return s.toUpperCase()
                .replace("É", "E").replace("È", "E").replace("Ê", "E")
                .replace("À", "A").replace("Ç", "C")
                .replace("Î", "I").replace("Ô", "O").replace("Ù", "U");
    }

    private boolean ressemble(String motCherche, String texte) {
        if (motCherche == null || motCherche.length() < 3) return false;
        motCherche = normaliser(motCherche);
        if (texte.contains(motCherche)) return true;
        String[] mots = texte.split("[^A-Z0-9]+");
        for (String mot : mots) {
            if (mot.length() < 3) continue;
            int diff = 0;
            int min = Math.min(motCherche.length(), mot.length());
            for (int i = 0; i < min; i++) {
                if (motCherche.charAt(i) != mot.charAt(i)) diff++;
            }
            diff += Math.abs(motCherche.length() - mot.length());
            int tolerance = motCherche.length() <= 4 ? 1 : 2;
            if (diff <= tolerance) return true;
        }
        return false;
    }

    private void verifier(String texteMaj) {
        Cursor c = db.getProfilPourVerification(userEmail);
        if (c == null || !c.moveToFirst()) {
            erreur("Profil introuvable");
            return;
        }
        String nomEtudiant = c.getString(0) != null ? c.getString(0) : "";
        String universiteEtudiante = c.getString(1) != null ? c.getString(1) : "";
        String niveauEtudiant = c.getString(3) != null ? c.getString(3) : "";
        c.close();

        boolean contientMotsCles =
                texteMaj.contains("ATTEST") ||
                        texteMaj.contains("ATION") ||
                        texteMaj.contains("INSC") ||
                        texteMaj.contains("UNIVERSITE") ||
                        texteMaj.contains("INSTITUT") ||
                        texteMaj.contains("SUPERIEUR") ||
                        texteMaj.contains("FACULTE") ||
                        texteMaj.contains("ETUDIANT") ||
                        texteMaj.contains("SCOLARITE");

        if (!contientMotsCles) {
            erreur("Il ne s'agit pas d'une attestation d'inscription.");
            return;
        }

        List<String> erreurs = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
        int anneeActuelle = cal.get(Calendar.YEAR);
        int moisActuel = cal.get(Calendar.MONTH) + 1;
        int anneeDebut, anneeFin;
        if (moisActuel >= 9) {
            anneeDebut = anneeActuelle;
            anneeFin = anneeActuelle + 1;
        } else {
            anneeDebut = anneeActuelle - 1;
            anneeFin = anneeActuelle;
        }
        boolean anneePresente = texteMaj.contains(String.valueOf(anneeDebut)) && texteMaj.contains(String.valueOf(anneeFin));
        if (!anneePresente) {
            erreurs.add("L'attestation n'est pas valide pour l'annee universitaire en cours (" + anneeDebut + "-" + anneeFin + ").");
        }

        boolean nomPresent = false;
        if (!nomEtudiant.trim().isEmpty()) {
            String[] motsNom = normaliser(nomEtudiant).split(" ");
            int motsTrouves = 0;
            int totalMots = 0;
            for (String mot : motsNom) {
                mot = mot.trim();
                if (mot.length() >= 3) {
                    totalMots++;
                    if (ressemble(mot, texteMaj)) motsTrouves++;
                }
            }
            if (totalMots == 0 || motsTrouves >= 1) nomPresent = true;
        } else {
            nomPresent = true;
        }
        if (!nomPresent) erreurs.add("Le nom complet ne correspond pas au document.");

        boolean niveauPresent = true;
        if (!niveauEtudiant.trim().isEmpty()) {
            String[] motsNiveau = normaliser(niveauEtudiant).split(" ");
            int motsTotal = 0;
            int motsTrouves = 0;
            for (String mot : motsNiveau) {
                mot = mot.trim();
                if (mot.length() >= 3 && !mot.equals("DE") && !mot.equals("LA") && !mot.equals("ET") && !mot.equals("EN") && !mot.equals("DU") && !mot.equals("DES")) {
                    motsTotal++;
                    if (ressemble(mot, texteMaj)) motsTrouves++;
                }
            }
            if (motsTotal > 0 && motsTrouves * 2 < motsTotal) niveauPresent = false;
        }
        if (!niveauPresent) erreurs.add("Le niveau d'etude ne correspond pas au document.");

        boolean universitePresente = true;
        if (!universiteEtudiante.trim().isEmpty()) {
            String[] motsUniv = normaliser(universiteEtudiante).split(" ");
            boolean trouveUniv = false;
            for (String mot : motsUniv) {
                mot = mot.trim();
                if (mot.length() > 3 && !mot.equals("UNIVERSITE") && !mot.equals("INSTITUT")) {
                    if (ressemble(mot, texteMaj)) {
                        trouveUniv = true;
                        break;
                    }
                }
            }
            universitePresente = trouveUniv;
        }
        if (!universitePresente) erreurs.add("L'universite ne correspond pas au document.");

        if (!erreurs.isEmpty()) {
            String message = "Echec de verification : ";
            for (int i = 0; i < erreurs.size(); i++) {
                if (i > 0) message += " | ";
                message += erreurs.get(i);
            }
            erreur(message);
            return;
        }

        Toast.makeText(this, "Verification automatique reussie ! Compte valide.", Toast.LENGTH_SHORT).show();
        enregistrer();
    }

    private void erreur(String msg) {
        btnVerify.setEnabled(true);
        btnVerify.setText("Verifier");
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private void enregistrer() {
        try {
            File dossier = new File(getFilesDir(), "attestations");
            if (!dossier.exists()) dossier.mkdirs();
            File f = new File(dossier, "attestation_" + System.currentTimeMillis() + ".jpg");
            InputStream in = getContentResolver().openInputStream(documentUri);
            FileOutputStream out = new FileOutputStream(f);
            byte[] b = new byte[4096];
            int n;
            while ((n = in.read(b)) != -1) out.write(b, 0, n);
            in.close();
            out.close();

            db.enregistrerAttestation(userEmail, f.getAbsolutePath());
            db.modifierStatutVerification(userEmail, "Verifiee");

            Intent i = new Intent(this, Interface6.class);
            i.putExtra("USER_EMAIL", userEmail);
            startActivity(i);
            finish();
        } catch (Exception e) {
            erreur("Erreur enregistrement");
        }
    }
}