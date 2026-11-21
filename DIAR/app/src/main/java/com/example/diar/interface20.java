package com.example.diar;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class interface20 extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    private LinearLayout layoutReservations;
    private TextView tvEmpty;

    private LinearLayout tabEnAttente, tabAcceptees, tabRefusees;
    private TextView tvTabEnAttente, tvTabAcceptees, tvTabRefusees;

    private String emailProprietaire;
    private String filtreActuel = "En attente";

    private final int NAVY = Color.rgb(30, 42, 90);
    private final int GREEN = Color.rgb(27, 138, 90);
    private final int RED = Color.rgb(210, 65, 65);
    private final int ORANGE = Color.rgb(220, 145, 35);
    private final int GREY = Color.rgb(100, 116, 139);
    private final int BORDER = Color.rgb(232, 237, 243);
    private final int SOFT_BG = Color.rgb(247, 249, 252);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface20);

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        layoutReservations = findViewById(R.id.layoutReservations);
        tvEmpty = findViewById(R.id.tvEmpty);

        tabEnAttente = findViewById(R.id.tabEnAttente);
        tabAcceptees = findViewById(R.id.tabAcceptees);
        tabRefusees = findViewById(R.id.tabRefusees);

        tvTabEnAttente = findViewById(R.id.tvTabEnAttente);
        tvTabAcceptees = findViewById(R.id.tvTabAcceptees);
        tvTabRefusees = findViewById(R.id.tvTabRefusees);

        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        NavigationProprietaire.setup(
                this,
                R.id.nav_prop_annonces
        );

        tabEnAttente.setOnClickListener(v -> changerFiltre("En attente"));
        tabAcceptees.setOnClickListener(v -> changerFiltre("Acceptée"));
        tabRefusees.setOnClickListener(v -> changerFiltre("Refusée"));

        actualiserOnglets();
        afficherReservations();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (databaseHelper != null && layoutReservations != null) {
            afficherReservations();
        }
    }

    private int dp(float valeur) {
        return (int) (valeur * getResources()
                .getDisplayMetrics().density);
    }

    private GradientDrawable fond(
            int couleur,
            int rayon,
            int couleurBordure
    ) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(couleur);
        drawable.setCornerRadius(dp(rayon));

        if (couleurBordure != Color.TRANSPARENT) {
            drawable.setStroke(dp(1), couleurBordure);
        }

        return drawable;
    }

    private GradientDrawable fondRond(int couleur) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(couleur);
        return drawable;
    }

    private void changerFiltre(String filtre) {
        filtreActuel = filtre;
        actualiserOnglets();
        afficherReservations();
    }

    private void actualiserOnglets() {

        mettreAJourOnglet(
                tabEnAttente,
                tvTabEnAttente,
                "En attente".equals(filtreActuel)
        );

        mettreAJourOnglet(
                tabAcceptees,
                tvTabAcceptees,
                "Acceptée".equals(filtreActuel)
        );

        mettreAJourOnglet(
                tabRefusees,
                tvTabRefusees,
                "Refusée".equals(filtreActuel)
        );
    }

    private void mettreAJourOnglet(
            LinearLayout tab,
            TextView texte,
            boolean actif
    ) {
        tab.setBackground(
                fond(
                        actif ? Color.WHITE : Color.TRANSPARENT,
                        12,
                        actif ? Color.rgb(226, 232, 240) : Color.TRANSPARENT
                )
        );

        tab.setElevation(actif ? dp(2) : 0);

        texte.setTextColor(actif ? GREEN : GREY);
        texte.setTypeface(
                null,
                actif ? Typeface.BOLD : Typeface.NORMAL
        );
    }

    private void afficherReservations() {

        layoutReservations.removeAllViews();

        emailProprietaire = SessionManager.normaliserEmail(
                sessionManager.getEmail()
        );

        if (emailProprietaire == null) {
            emailProprietaire = "";
        }

        if (!sessionManager.isLoggedIn()
                || emailProprietaire.isEmpty()) {

            afficherVide("Veuillez vous connecter.");
            return;
        }

        Cursor cursor = databaseHelper.getReservationsProprietaire(
                emailProprietaire
        );

        if (cursor == null) {
            afficherVide("Erreur lors du chargement des demandes.");
            return;
        }

        int nombre = 0;

        try {

            while (cursor.moveToNext()) {

                long reservationId = cursor.getLong(
                        cursor.getColumnIndexOrThrow("id")
                );

                long annonceId = cursor.getLong(
                        cursor.getColumnIndexOrThrow("annonce_id")
                );

                String emailEtudiant = cursor.getString(
                        cursor.getColumnIndexOrThrow("emailEtudiant")
                );

                String date = cursor.getString(
                        cursor.getColumnIndexOrThrow("dateReservation")
                );

                String statut = cursor.getString(
                        cursor.getColumnIndexOrThrow("statut")
                );

                if (statut == null) {
                    statut = "En attente";
                }

                if (!statut.equalsIgnoreCase(filtreActuel)) {
                    continue;
                }

                ajouterCarteReservation(
                        reservationId,
                        annonceId,
                        emailEtudiant,
                        date,
                        statut
                );

                nombre++;
            }

        } catch (Exception e) {

            afficherVide("Erreur : " + e.getMessage());

        } finally {
            cursor.close();
        }

        if (nombre == 0) {
            afficherVide(
                    "Aucune demande " + filtreActuel.toLowerCase()
                            + " pour le moment."
            );
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
    }

    private void afficherVide(String message) {

        layoutReservations.removeAllViews();

        tvEmpty.setText("📭\n\n" + message);
        tvEmpty.setTextColor(GREY);
        tvEmpty.setTextSize(15);
        tvEmpty.setGravity(Gravity.CENTER);
        tvEmpty.setPadding(dp(20), dp(60), dp(20), dp(35));
        tvEmpty.setVisibility(View.VISIBLE);
    }

    // =========================================
    // CARTE DEMANDE (DESIGN PRO)
    // =========================================

    private void ajouterCarteReservation(
            long reservationId,
            long annonceId,
            String emailEtudiant,
            String date,
            String statut
    ) {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));

        card.setBackground(
                fond(Color.WHITE, 20, BORDER)
        );

        card.setElevation(dp(3));

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(dp(2), dp(8), dp(2), dp(8));
        card.setLayoutParams(cardParams);

        // ----- En-tête : avatar + titre + statut -----
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        String initiale = "?";
        if (emailEtudiant != null && !emailEtudiant.trim().isEmpty()) {
            initiale = emailEtudiant.trim().substring(0, 1).toUpperCase();
        }

        TextView avatar = new TextView(this);
        avatar.setText(initiale);
        avatar.setTextSize(18);
        avatar.setTypeface(null, Typeface.BOLD);
        avatar.setTextColor(GREEN);
        avatar.setGravity(Gravity.CENTER);
        avatar.setBackground(fondRond(Color.rgb(226, 247, 235)));

        header.addView(avatar, new LinearLayout.LayoutParams(dp(46), dp(46)));

        LinearLayout titres = new LinearLayout(this);
        titres.setOrientation(LinearLayout.VERTICAL);

        TextView titre = new TextView(this);
        titre.setText("Demande de visite");
        titre.setTextSize(16);
        titre.setTypeface(null, Typeface.BOLD);
        titre.setTextColor(NAVY);

        TextView sousTitre = new TextView(this);
        sousTitre.setText(
                emailEtudiant == null ? "Étudiant" : emailEtudiant
        );
        sousTitre.setTextSize(12);
        sousTitre.setTextColor(GREY);
        sousTitre.setSingleLine(true);
        sousTitre.setEllipsize(TextUtils.TruncateAt.END);
        sousTitre.setPadding(0, dp(2), 0, 0);

        titres.addView(titre);
        titres.addView(sousTitre);

        LinearLayout.LayoutParams titresParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );
        titresParams.setMargins(dp(12), 0, dp(8), 0);

        header.addView(titres, titresParams);

        TextView badge = creerTextView(
                statut,
                11,
                getCouleurStatut(statut)
        );

        badge.setTypeface(null, Typeface.BOLD);
        badge.setPadding(dp(11), dp(6), dp(11), dp(6));
        badge.setBackground(
                fond(getFondStatut(statut), 20, Color.TRANSPARENT)
        );

        header.addView(badge);
        card.addView(header);

        // ----- Bloc informations -----
        LinearLayout blocInfos = new LinearLayout(this);
        blocInfos.setOrientation(LinearLayout.VERTICAL);
        blocInfos.setPadding(dp(12), dp(6), dp(12), dp(6));
        blocInfos.setBackground(fond(SOFT_BG, 14, Color.TRANSPARENT));

        LinearLayout.LayoutParams blocParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        blocParams.setMargins(0, dp(14), 0, 0);

        ajouterInformation(
                blocInfos,
                "🏠",
                "Annonce",
                "N° " + annonceId
        );

        ajouterInformation(
                blocInfos,
                "📅",
                "Date de visite",
                date == null ? "Non précisée" : date
        );

        card.addView(blocInfos, blocParams);

        // ----- Boutons -----
        if ("En attente".equalsIgnoreCase(statut)) {

            LinearLayout buttonsLayout = new LinearLayout(this);
            buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);
            buttonsLayout.setGravity(Gravity.CENTER);
            buttonsLayout.setPadding(0, dp(14), 0, 0);

            Button btnRefuser = creerBouton(
                    "✕  Refuser",
                    Color.WHITE,
                    RED,
                    RED
            );

            Button btnAccepter = creerBouton(
                    "✓  Accepter",
                    GREEN,
                    Color.WHITE,
                    Color.TRANSPARENT
            );

            LinearLayout.LayoutParams paramsRefuser =
                    new LinearLayout.LayoutParams(0, dp(46), 1);
            paramsRefuser.setMargins(0, 0, dp(6), 0);

            LinearLayout.LayoutParams paramsAccepter =
                    new LinearLayout.LayoutParams(0, dp(46), 1);
            paramsAccepter.setMargins(dp(6), 0, 0, 0);

            buttonsLayout.addView(btnRefuser, paramsRefuser);
            buttonsLayout.addView(btnAccepter, paramsAccepter);

            card.addView(buttonsLayout);

            btnAccepter.setOnClickListener(v ->
                    modifierStatut(reservationId, "Acceptée")
            );

            btnRefuser.setOnClickListener(v ->
                    modifierStatut(reservationId, "Refusée")
            );
        }

        layoutReservations.addView(card);
    }

    private void ajouterInformation(
            LinearLayout parent,
            String icone,
            String titre,
            String valeur
    ) {

        LinearLayout ligne = new LinearLayout(this);
        ligne.setOrientation(LinearLayout.HORIZONTAL);
        ligne.setGravity(Gravity.CENTER_VERTICAL);
        ligne.setPadding(0, dp(6), 0, dp(6));

        TextView icon = creerTextView(icone, 17, NAVY);
        icon.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams iconParams =
                new LinearLayout.LayoutParams(dp(32), dp(32));

        ligne.addView(icon, iconParams);

        LinearLayout contenu = new LinearLayout(this);
        contenu.setOrientation(LinearLayout.VERTICAL);

        TextView label = creerTextView(titre, 11, GREY);
        label.setPadding(0, 0, 0, 0);

        TextView valeurText = creerTextView(valeur, 14, NAVY);
        valeurText.setTypeface(null, Typeface.BOLD);
        valeurText.setPadding(0, dp(1), 0, 0);

        contenu.addView(label);
        contenu.addView(valeurText);

        ligne.addView(contenu);

        parent.addView(ligne);
    }

    private Button creerBouton(
            String texte,
            int couleurFond,
            int couleurTexte,
            int couleurBordure
    ) {

        Button bouton = new Button(this);

        bouton.setText(texte);
        bouton.setTextColor(couleurTexte);
        bouton.setTextSize(14);
        bouton.setTypeface(null, Typeface.BOLD);
        bouton.setAllCaps(false);
        bouton.setMinHeight(0);
        bouton.setMinimumHeight(0);
        bouton.setStateListAnimator(null);
        bouton.setPadding(dp(8), 0, dp(8), 0);

        bouton.setBackground(
                fond(couleurFond, 14, couleurBordure)
        );

        return bouton;
    }

    private int getCouleurStatut(String statut) {

        if (statut.equalsIgnoreCase("Acceptée")) {
            return GREEN;
        }

        if (statut.equalsIgnoreCase("Refusée")) {
            return RED;
        }

        return ORANGE;
    }

    private int getFondStatut(String statut) {

        if (statut.equalsIgnoreCase("Acceptée")) {
            return Color.rgb(226, 247, 235);
        }

        if (statut.equalsIgnoreCase("Refusée")) {
            return Color.rgb(255, 235, 235);
        }

        return Color.rgb(255, 245, 220);
    }

    private TextView creerTextView(
            String texte,
            int taille,
            int couleur
    ) {

        TextView textView = new TextView(this);

        textView.setText(texte);
        textView.setTextSize(taille);
        textView.setTextColor(couleur);
        textView.setPadding(0, dp(3), 0, dp(3));

        return textView;
    }

    private void modifierStatut(
            long reservationId,
            String nouveauStatut
    ) {

        boolean resultat =
                databaseHelper.modifierStatutReservation(
                        reservationId,
                        nouveauStatut
                );

        if (resultat) {

            Toast.makeText(
                    this,
                    "Demande " + nouveauStatut.toLowerCase()
                            + " avec succès",
                    Toast.LENGTH_SHORT
            ).show();

            afficherReservations();

        } else {

            Toast.makeText(
                    this,
                    "Erreur lors de la modification",
                    Toast.LENGTH_SHORT
            ).show();
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